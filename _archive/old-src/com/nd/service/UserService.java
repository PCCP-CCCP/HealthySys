package com.nd.service;

import com.nd.dao.UserDao;

import java.sql.SQLException;

public class UserService {//业务逻辑层

    /**
     * 登录，成功返回该账号角色（doctor/patient），失败抛异常
     */
    public String login(String tel, String pwd) throws SQLException {
        if (tel == null || tel.equals("")) {
            throw new RuntimeException("账号不能为空！");
        }
        if (pwd == null || pwd.equals("")) {
            throw new RuntimeException("密码不能为空！");
        }
        UserDao userDao = new UserDao();
        String role = userDao.login(tel, pwd);
        if (role == null) {
            throw new RuntimeException("账号或密码输入有误！");
        }
        return role;
    }

    /**
     * 注册新用户，role: patient=仅患者，doctor=双重角色（医生+患者）
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
        boolean success = userDao.register(tel, pwd, name, role);
        if (!success) {
            throw new RuntimeException("注册失败，请稍后重试！");
        }
    }


}
