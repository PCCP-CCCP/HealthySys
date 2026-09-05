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
 * <p>所属模块：healthy-service（业务逻辑层）。</p>
 *
 * <p>安全约定：本层是密码加密的唯一入口——注册时生成盐与密文、登录时用盐重算校验、
 * 改密时先验旧密码再换新盐新密文；明文密码只在本层内存中短暂存在，不落库、不入 Session。</p>
 *
 * <p>调用方（登录界面/注册对话框/个人信息面板）只与本服务交互，不直接接触 DAO。</p>
 *
 * <p>关键依赖：调用 {@link UserDao} 执行数据库操作；使用 {@link PasswordUtil} 生成盐/计算摘要/校验密码；
 * 使用 {@link Session} 写入当前用户姓名；操作 {@link User} 实体。</p>
 *
 * @author HealthySys 业务逻辑层
 */
public class UserService {

    /**
     * 登录：校验入参后，用「盐 + SHA-256」重算比对数据库密文。
     *
     * <p>登录成功后把当前用户姓名写入 {@link Session#currentName}，供主界面头部展示。</p>
     *
     * <p>安全细节：账号不存在或密码错误都返回相同提示"账号或密码输入有误"，
     * 防止攻击者通过不同提示枚举哪些手机号已注册。</p>
     *
     * @param tel 手机号（账号）
     * @param pwd 明文密码
     * @return 登录成功返回该账号角色（doctor/patient）
     * @throws SQLException      数据库访问异常
     * @throws RuntimeException 账号为空 / 密码为空 / 账号或密码错误
     */
    public String login(String tel, String pwd) throws SQLException {
        // 第一步：校验账号非空
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("账号不能为空！");
        }
        // 第二步：校验密码非空
        if (pwd == null || pwd.equals("")) {
            throw new RuntimeException("密码不能为空！");
        }
        // 第三步：创建 DAO 实例，按账号查询用户完整信息（含密文和盐）
        UserDao userDao = new UserDao();
        User user = userDao.findByTel(tel);
        // 第四步：账号不存在或密码密文校验不通过均视为登录失败，避免泄露账号是否存在
        // PasswordUtil.verify：用用户存储的盐重算输入密码的摘要，与存储密文做恒定时间比较
        if (user == null || !PasswordUtil.verify(pwd, user.getSalt(), user.getPwd())) {
            throw new RuntimeException("账号或密码输入有误！");
        }
        // 第五步：读取用户角色，若数据库中角色为空则默认按 patient 处理
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "patient";
        }
        // 第六步：将姓名写入会话，供主界面显示（不存在时回退为空串）
        Session.currentName = user.getName() == null ? "" : user.getName();
        // 返回角色供调用方决定后续界面展示（doctor 可切换视角，patient 只能患者视角）
        return role;
    }

    /**
     * 注册新用户：逐项校验入参合法性，生成随机盐与密码密文后写入数据库。
     *
     * <p>校验顺序：手机号非空 → 姓名非空 → 密码非空 → 两次密码一致 → 密码长度≥6 →
     * 角色合法 → 手机号未被注册 → 生成盐和密文 → 写入数据库。</p>
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
        // 校验手机号非空
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("手机号不能为空！");
        }
        // 校验姓名非空
        if (name == null || name.equals("")) {
            throw new RuntimeException("姓名不能为空！");
        }
        // 校验密码非空
        if (pwd == null || pwd.equals("")) {
            throw new RuntimeException("密码不能为空！");
        }
        // 校验两次输入密码一致
        if (!pwd.equals(confirmPwd)) {
            throw new RuntimeException("两次输入的密码不一致！");
        }
        // 校验密码长度不少于 6 位
        if (pwd.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位！");
        }
        // 校验角色必须是 doctor 或 patient 之一
        if (role == null || (!"doctor".equals(role) && !"patient".equals(role))) {
            throw new RuntimeException("请选择注册角色！");
        }
        // 检查手机号是否已被注册（唯一性约束）
        UserDao userDao = new UserDao();
        if (userDao.checkTelExists(tel)) {
            throw new RuntimeException("该手机号已被注册！");
        }
        // 密码加密：随机盐 + SHA-256，仅密文入库，明文不写入数据库
        // PasswordUtil.generateSalt：生成 16 字节随机盐
        String salt = PasswordUtil.generateSalt();
        // PasswordUtil.hash：计算 SHA-256(salt + password) 摘要
        String pwdHash = PasswordUtil.hash(pwd, salt);
        // 将密文和盐（不含明文）写入数据库
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
     * @throws RuntimeException 账号为空（未登录）时抛出
     * @throws SQLException      数据库访问异常
     */
    public User getProfile(String tel) throws SQLException {
        // 校验已登录（账号非空）
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("请先登录！");
        }
        // 直接委托 DAO 查询完整用户信息
        return new UserDao().findByTel(tel);
    }

    /**
     * 修改个人资料：校验姓名非空后更新出生日期、性别、身高、体重。
     *
     * <p>身高/体重为可空数值字段：非空时必须是合法数字，空串视为 null。</p>
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
        // 校验已登录
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("请先登录！");
        }
        // 校验姓名非空且去除首尾空白后不为空
        if (name == null || name.trim().equals("")) {
            throw new RuntimeException("姓名不能为空！");
        }
        // 数值字段可空；非空时必须是合法数字（parseNullableDecimal 内部处理空串→null）
        BigDecimal h = parseNullableDecimal(height, "身高");
        BigDecimal w = parseNullableDecimal(weight, "体重");
        // 调用 DAO 更新个人资料（name.trim() 去除首尾空白）
        boolean success = new UserDao().updateProfile(tel, name.trim(), birthDate, gender, h, w);
        if (!success) {
            throw new RuntimeException("资料更新失败，请稍后重试！");
        }
        // 资料更新成功后同步刷新 Session 中的当前用户姓名
        Session.currentName = name.trim();
    }

    /**
     * 修改密码：校验旧密码正确后，生成新盐与新密文更新数据库。
     *
     * <p>安全流程：校验入参 → 查用户 → 验旧密码 → 生成新盐 → 计算新密文 → 更新数据库。
     * 改密必须换新盐，避免新旧密码因相同盐而产生可关联的密文。</p>
     *
     * @param tel         账号
     * @param oldPwd      原密码（明文）
     * @param newPwd      新密码（明文）
     * @param confirmPwd  确认新密码
     * @throws SQLException      数据库访问异常
     * @throws RuntimeException 入参校验失败 / 原密码错误 / 更新失败
     */
    public void changePassword(String tel, String oldPwd, String newPwd, String confirmPwd) throws SQLException {
        // 校验已登录
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("请先登录！");
        }
        // 校验原密码非空
        if (oldPwd == null || oldPwd.equals("")) {
            throw new RuntimeException("原密码不能为空！");
        }
        // 校验新密码非空
        if (newPwd == null || newPwd.equals("")) {
            throw new RuntimeException("新密码不能为空！");
        }
        // 校验两次新密码一致
        if (!newPwd.equals(confirmPwd)) {
            throw new RuntimeException("两次输入的新密码不一致！");
        }
        // 校验新密码长度不少于 6 位
        if (newPwd.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位！");
        }
        // 校验新密码不能与原密码相同
        if (newPwd.equals(oldPwd)) {
            throw new RuntimeException("新密码不能与原密码相同！");
        }
        // 查询用户信息以获取存储的盐和密文，用于校验旧密码
        UserDao userDao = new UserDao();
        User user = userDao.findByTel(tel);
        // PasswordUtil.verify：用存储的盐重算旧密码摘要，与存储密文比对
        if (user == null || !PasswordUtil.verify(oldPwd, user.getSalt(), user.getPwd())) {
            throw new RuntimeException("原密码输入有误！");
        }
        // 换新盐新密文，避免与历史密文可关联（即使新旧密码相同也应换盐，此处已额外禁止相同）
        // PasswordUtil.generateSalt：生成新的随机盐
        String newSalt = PasswordUtil.generateSalt();
        // PasswordUtil.hash：用新盐计算新密码的摘要
        String newHash = PasswordUtil.hash(newPwd, newSalt);
        // 更新数据库中的密文和盐
        boolean success = userDao.updatePassword(tel, newHash, newSalt);
        if (!success) {
            throw new RuntimeException("密码修改失败，请稍后重试！");
        }
    }

    /**
     * 解析可空的十进制数值字段（供身高、体重使用）。
     *
     * <p>空白文本返回 null（允许不填写）；非空文本必须能解析为合法 BigDecimal，
     * 否则抛出带字段名的错误提示。</p>
     *
     * @param text 输入文本
     * @param name 字段中文名（用于错误提示）
     * @return 合法数值；空白文本返回 null
     * @throws RuntimeException 文本非空但无法解析为数字时抛出
     */
    private BigDecimal parseNullableDecimal(String text, String name) {
        // 空白文本视为未填写，返回 null
        if (text == null || text.trim().equals("")) {
            return null;
        }
        try {
            // 尝试将去除空白后的文本解析为 BigDecimal
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            // 解析失败，提示用户该字段必须是数字
            throw new RuntimeException(name + "必须是数字！");
        }
    }
}
