package com.nd.dao;

import com.nd.view.utils.JdbcUitl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {//数据访问层

    /**
     * 登录校验，成功返回该账号角色（doctor/patient），失败返回 null
     */
    public String login(String tel, String pwd) throws SQLException {
        JdbcUitl jdbcUitl = new JdbcUitl();
        String sql = "select role from users where tel = ? and pwd = ?";
        Object[] param = {tel, pwd};
        ResultSet rs = jdbcUitl.querySql(sql, param);
        String role = null;
        if (rs.next()) {
            role = rs.getString("role");
            if (role == null || role.trim().isEmpty()) {
                role = "patient";
            }
        }
        jdbcUitl.close();
        return role;
    }

    /**
     * 判断手机号是否已存在
     */
    public boolean checkTelExists(String tel) throws SQLException {
        JdbcUitl jdbcUitl = new JdbcUitl();
        String sql = "select id from users where tel = ?";
        Object[] param = {tel};
        ResultSet rs = jdbcUitl.querySql(sql, param);
        boolean exists = rs.next();
        jdbcUitl.close();
        return exists;
    }

    /**
     * 新增用户，指定角色：patient=仅患者，doctor=双重角色（医生+患者）
     */
    public boolean register(String tel, String pwd, String name, String role) throws SQLException {
        JdbcUitl jdbcUitl = new JdbcUitl();
        String sql = "insert into users(tel, pwd, name, role) values (?,?,?,?)";
        Object[] param = {tel, pwd, name, role};
        int num = jdbcUitl.iudSql(sql, param);
        jdbcUitl.close();
        return num > 0;
    }

}
