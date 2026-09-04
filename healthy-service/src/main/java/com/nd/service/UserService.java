package com.nd.service;

import com.nd.common.entity.User;
import com.nd.common.util.PasswordUtil;
import com.nd.common.util.Session;
import com.nd.dao.UserDao;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * 用户业务逻辑层：封装登录、注册、个人信息维护与密码修改的入参校验、业务规则与流程编排。
 *
 * <p>安全约定：本层是密码加密的唯一入口——注册时生成盐与密文、登录时用盐重算校验、
 * 改密时先验旧密码再换新盐新密文；明文密码只在本层内存中短暂存在，不落库、不入 Session。</p>
 *
 * <p>调用方（登录界面/注册对话框/个人信息面板）只与本服务交互，不直接接触 DAO。</p>
 *
 * @author HealthySys 业务逻辑层
 */
public class UserService {

    /**
     * 登录：校验入参后，用「盐 + SHA-256」重算比对数据库密文。
     *
     * <p>登录成功后把当前用户姓名写入 {@link Session#currentName}，供主界面头部展示。</p>
     *
     * @param tel 手机号（账号）
     * @param pwd 明文密码
     * @return 登录成功返回该账号角色（doctor/patient）
     * @throws SQLException      数据库访问异常
     * @throws RuntimeException 账号为空 / 密码为空 / 账号或密码错误
     */
    public String login(String tel, String pwd) throws SQLException {
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("账号不能为空！");
        }
        if (pwd == null || pwd.equals("")) {
            throw new RuntimeException("密码不能为空！");
        }
        UserDao userDao = new UserDao();
        User user = userDao.findByTel(tel);
        // 账号不存在或密码密文校验不通过均视为登录失败，避免泄露账号是否存在
        if (user == null || !PasswordUtil.verify(pwd, user.getSalt(), user.getPwd())) {
            throw new RuntimeException("账号或密码输入有误！");
        }
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "patient";
        }
        // 将姓名写入会话，供主界面显示（不存在时回退为空串）
        Session.currentName = user.getName() == null ? "" : user.getName();
        return role;
    }

    /**
     * 注册新用户：逐项校验入参合法性，生成随机盐与密码密文后写入数据库。
     *
     * @param tel        手机号（账号）
     * @param pwd        明文密码
     * @param confirmPwd 确认密码
     * @param name       姓名
     * @param role       角色：patient=仅患者角色，doctor=双重角色（医生+患者）
     * @throws SQLException      数据库访问异常
     * @throws RuntimeException 各项入参校验失败时抛出，message 可直接展示给用户
     */
    public void register(String tel, String pwd, String confirmPwd, String name, String role) throws SQLException {
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("手机号不能为空！");
        }
        if (name == null || name.equals("")) {
            throw new RuntimeException("姓名不能为空！");
        }
        if (pwd == null || pwd.equals("")) {
            throw new RuntimeException("密码不能为空！");
        }
        if (!pwd.equals(confirmPwd)) {
            throw new RuntimeException("两次输入的密码不一致！");
        }
        if (pwd.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位！");
        }
        if (role == null || (!"doctor".equals(role) && !"patient".equals(role))) {
            throw new RuntimeException("请选择注册角色！");
        }
        UserDao userDao = new UserDao();
        if (userDao.checkTelExists(tel)) {
            throw new RuntimeException("该手机号已被注册！");
        }
        // 密码加密：随机盐 + SHA-256，仅密文入库
        String salt = PasswordUtil.generateSalt();
        String pwdHash = PasswordUtil.hash(pwd, salt);
        boolean success = userDao.register(tel, pwdHash, salt, name, role);
        if (!success) {
            throw new RuntimeException("注册失败，请稍后重试！");
        }
    }

    /**
     * 查询当前用户个人资料（含出生日期、性别、身高、体重）。
     *
     * @param tel 账号
     * @return 用户实体；账号不存在返回 null
     * @throws SQLException 数据库访问异常
     */
    public User getProfile(String tel) throws SQLException {
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("请先登录！");
        }
        return new UserDao().findByTel(tel);
    }

    /**
     * 修改个人资料：校验姓名非空后更新出生日期、性别、身高、体重。
     *
     * @param tel       账号
     * @param name      姓名
     * @param birthDate 出生日期（yyyy-MM-dd，可空）
     * @param gender    性别（男 / 女，可空）
     * @param height    身高 cm（可空）
     * @param weight    体重 kg（可空）
     * @throws SQLException      数据库访问异常
     * @throws RuntimeException 姓名为空 / 身高体重非法 / 更新失败
     */
    public void updateProfile(String tel, String name, String birthDate, String gender,
                              String height, String weight) throws SQLException {
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("请先登录！");
        }
        if (name == null || name.trim().equals("")) {
            throw new RuntimeException("姓名不能为空！");
        }
        // 数值字段可空；非空时必须是合法数字
        BigDecimal h = parseNullableDecimal(height, "身高");
        BigDecimal w = parseNullableDecimal(weight, "体重");
        boolean success = new UserDao().updateProfile(tel, name.trim(), birthDate, gender, h, w);
        if (!success) {
            throw new RuntimeException("资料更新失败，请稍后重试！");
        }
        Session.currentName = name.trim();
    }

    /**
     * 修改密码：校验旧密码正确后，生成新盐与新密文更新数据库。
     *
     * @param tel       账号
     * @param oldPwd    原密码（明文）
     * @param newPwd    新密码（明文）
     * @param confirmPwd 确认新密码
     * @throws SQLException      数据库访问异常
     * @throws RuntimeException 入参校验失败 / 原密码错误 / 更新失败
     */
    public void changePassword(String tel, String oldPwd, String newPwd, String confirmPwd) throws SQLException {
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("请先登录！");
        }
        if (oldPwd == null || oldPwd.equals("")) {
            throw new RuntimeException("原密码不能为空！");
        }
        if (newPwd == null || newPwd.equals("")) {
            throw new RuntimeException("新密码不能为空！");
        }
        if (!newPwd.equals(confirmPwd)) {
            throw new RuntimeException("两次输入的新密码不一致！");
        }
        if (newPwd.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位！");
        }
        if (newPwd.equals(oldPwd)) {
            throw new RuntimeException("新密码不能与原密码相同！");
        }
        UserDao userDao = new UserDao();
        User user = userDao.findByTel(tel);
        if (user == null || !PasswordUtil.verify(oldPwd, user.getSalt(), user.getPwd())) {
            throw new RuntimeException("原密码输入有误！");
        }
        // 换新盐新密文，避免与历史密文可关联
        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hash(newPwd, newSalt);
        boolean success = userDao.updatePassword(tel, newHash, newSalt);
        if (!success) {
            throw new RuntimeException("密码修改失败，请稍后重试！");
        }
    }

    /**
     * 解析可空的十进制数值字段（供身高、体重使用）。
     *
     * @param text 输入文本
     * @param name 字段中文名（用于错误提示）
     * @return 合法数值；空白文本返回 null
     * @throws RuntimeException 文本非空但无法解析为数字时抛出
     */
    private BigDecimal parseNullableDecimal(String text, String name) {
        if (text == null || text.trim().equals("")) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException(name + "必须是数字！");
        }
    }
}
