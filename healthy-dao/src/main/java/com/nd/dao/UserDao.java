package com.nd.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.common.entity.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问对象（DAO）：负责 users 表的账号查询、手机号查重、注册写入与个人信息维护。
 *
 * <p>所属模块：healthy-dao（数据访问层）。</p>
 *
 * <p>安全约定：本 DAO 只读写密码密文与盐（由 {@code com.nd.common.util.PasswordUtil} 生成），
 * 数据库不保存也不经手明文密码。登录校验由业务层 {@code com.nd.service.UserService} 用盐重算摘要完成。</p>
 *
 * <p>关键依赖：使用 {@link JdbcUtil} 获取连接/执行 SQL/关闭资源；操作 {@link User} 实体；
 * 被 {@code UserService} 调用。</p>
 *
 * @author HealthySys 数据访问层
 */
public class UserDao {

    /**
     * 按账号查询用户完整信息（含密码密文、盐与个人健康信息）。
     *
     * @param tel 手机号（账号）
     * @return 用户实体；账号不存在返回 null
     * @throws SQLException 数据库访问异常
     */
    public User findByTel(String tel) throws SQLException {
        // 构造参数化查询 SQL，? 为占位符防止 SQL 注入
        String sql = "select id, tel, pwd, salt, name, role, birth_date, gender, height, weight "
                + "from users where tel = ?";
        // 参数数组，依次填入 ? 占位符
        Object[] param = {tel};
        // JdbcUtil.querySql：执行查询并返回结果集（连接/语句存入静态持有器）
        ResultSet rs = JdbcUtil.querySql(sql, param);
        User user = null;
        // rs.next()：将游标移动到下一行，首次调用定位到第一行；有数据返回 true
        if (rs.next()) {
            // 有结果行则映射为 User 实体
            user = mapRow(rs);
        }
        // 关闭本次查询打开的连接/语句/结果集
        JdbcUtil.close();
        return user;
    }

    /**
     * 判断手机号是否已被注册。
     *
     * @param tel 手机号
     * @return true=已存在，false=不存在
     * @throws SQLException 数据库访问异常
     */
    public boolean checkTelExists(String tel) throws SQLException {
        // 仅查 id 列即可判断是否存在，减少数据传输
        String sql = "select id from users where tel = ?";
        Object[] param = {tel};
        ResultSet rs = JdbcUtil.querySql(sql, param);
        // rs.next()：若有任意一行说明手机号已存在；不移动到下一行，仅判断是否存在结果
        boolean exists = rs.next();
        JdbcUtil.close();
        return exists;
    }

    /**
     * 新增用户（注册）。调用方传入已加密的密码密文与盐，本方法不接触明文。
     *
     * @param tel      手机号（账号）
     * @param pwdHash  密码密文（SHA-256 hex）
     * @param salt     密码盐（随机 hex）
     * @param name     姓名
     * @param role     角色：patient=仅患者角色，doctor=双重角色（医生+患者）
     * @return true=注册成功，false=注册失败
     * @throws SQLException 数据库访问异常
     */
    public boolean register(String tel, String pwdHash, String salt, String name, String role) throws SQLException {
        // 构造 INSERT SQL，? 占位符依次对应 tel/pwd/salt/name/role
        String sql = "insert into users(tel, pwd, salt, name, role) values (?,?,?,?,?)";
        // 参数数组按 SQL 占位符顺序排列
        Object[] param = {tel, pwdHash, salt, name, role};
        // JdbcUtil.iudSql：执行增删改并自动关闭资源，返回受影响行数
        int num = JdbcUtil.iudSql(sql, param);
        // 受影响行数 > 0 表示插入成功
        return num > 0;
    }

    /**
     * 更新用户个人资料（姓名、出生日期、性别、身高、体重）。
     *
     * @param tel       账号（定位用户）
     * @param name      姓名
     * @param birthDate 出生日期（yyyy-MM-dd，可空）
     * @param gender    性别（男 / 女，可空）
     * @param height    身高 cm（可空）
     * @param weight    体重 kg（可空）
     * @return true=更新成功（至少影响 1 行），false=未更新到任何行
     * @throws SQLException 数据库访问异常
     */
    public boolean updateProfile(String tel, String name, String birthDate, String gender,
                                 BigDecimal height, BigDecimal weight) throws SQLException {
        // UPDATE SQL：按 tel 定位用户，更新 5 个资料字段
        String sql = "update users set name = ?, birth_date = ?, gender = ?, height = ?, weight = ? where tel = ?";
        // 参数顺序：name, birth_date, gender, height, weight, tel（WHERE 条件放最后）
        Object[] param = {name, birthDate, gender, height, weight, tel};
        int num = JdbcUtil.iudSql(sql, param);
        return num > 0;
    }

    /**
     * 更新用户密码（仅更新密文与盐，由业务层负责旧密码校验）。
     *
     * @param tel     账号（定位用户）
     * @param pwdHash 新密码密文（SHA-256 hex）
     * @param salt    新密码盐（随机 hex）
     * @return true=更新成功（至少影响 1 行），false=未更新到任何行
     * @throws SQLException 数据库访问异常
     */
    public boolean updatePassword(String tel, String pwdHash, String salt) throws SQLException {
        // UPDATE SQL：按 tel 定位用户，同时更新密文和新盐
        String sql = "update users set pwd = ?, salt = ? where tel = ?";
        // 参数顺序：新密文, 新盐, 定位用的 tel
        Object[] param = {pwdHash, salt, tel};
        int num = JdbcUtil.iudSql(sql, param);
        return num > 0;
    }

    /**
     * 将结果集当前行映射为用户实体。
     *
     * @param rs 已定位到目标行的结果集
     * @return 用户实体
     * @throws SQLException 列读取异常
     */
    private User mapRow(ResultSet rs) throws SQLException {
        // rs.getInt/getString/getBigDecimal：按列名从当前行读取各字段值
        int id = rs.getInt("id");                    // 主键
        String tel = rs.getString("tel");            // 账号
        String pwd = rs.getString("pwd");            // 密码密文
        String salt = rs.getString("salt");           // 密码盐
        String name = rs.getString("name");           // 姓名
        String role = rs.getString("role");           // 角色
        String birthDate = rs.getString("birth_date"); // 出生日期（数据库下划线命名转 Java 驼峰）
        String gender = rs.getString("gender");       // 性别
        BigDecimal height = rs.getBigDecimal("height"); // 身高（高精度小数）
        BigDecimal weight = rs.getBigDecimal("weight");  // 体重（高精度小数）
        // 调用全字段构造器组装 User 对象
        return new User(id, tel, pwd, salt, name, role, birthDate, gender, height, weight);
    }
}
