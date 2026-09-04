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
 * <p>安全约定：本 DAO 只读写密码密文与盐（由 {@code com.nd.common.util.PasswordUtil} 生成），
 * 数据库不保存也不经手明文密码。登录校验由业务层 {@code UserService} 用盐重算摘要完成。</p>
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
        String sql = "select id, tel, pwd, salt, name, role, birth_date, gender, height, weight "
                + "from users where tel = ?";
        Object[] param = {tel};
        ResultSet rs = JdbcUtil.querySql(sql, param);
        User user = null;
        if (rs.next()) {
            user = mapRow(rs);
        }
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
        String sql = "select id from users where tel = ?";
        Object[] param = {tel};
        ResultSet rs = JdbcUtil.querySql(sql, param);
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
        String sql = "insert into users(tel, pwd, salt, name, role) values (?,?,?,?,?)";
        Object[] param = {tel, pwdHash, salt, name, role};
        int num = JdbcUtil.iudSql(sql, param);
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
        String sql = "update users set name = ?, birth_date = ?, gender = ?, height = ?, weight = ? where tel = ?";
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
        String sql = "update users set pwd = ?, salt = ? where tel = ?";
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
        int id = rs.getInt("id");
        String tel = rs.getString("tel");
        String pwd = rs.getString("pwd");
        String salt = rs.getString("salt");
        String name = rs.getString("name");
        String role = rs.getString("role");
        String birthDate = rs.getString("birth_date");
        String gender = rs.getString("gender");
        BigDecimal height = rs.getBigDecimal("height");
        BigDecimal weight = rs.getBigDecimal("weight");
        return new User(id, tel, pwd, salt, name, role, birthDate, gender, height, weight);
    }
}
