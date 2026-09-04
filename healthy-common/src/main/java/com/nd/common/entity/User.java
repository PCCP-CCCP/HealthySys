package com.nd.common.entity;

import java.math.BigDecimal;

/**
 * 用户实体：对应 users 表。
 *
 * <p>除账号、姓名、角色外，携带密码密文与盐（仅供 DAO / 业务层使用，界面层不展示），
 * 以及个人健康信息（出生日期、性别、身高、体重），供「个人信息管理」模块读写。</p>
 *
 * <p>安全约定：{@link #pwd} 为 SHA-256 密文，{@link #salt} 为加盐随机值，
 * 任何界面代码不得展示或回填这两个字段的明文。</p>
 *
 * @author HealthySys 公共模块
 */
public class User {

    /** 用户主键 */
    private int id;
    /** 账号（手机号），登录唯一标识 */
    private String tel;
    /** 密码密文（SHA-256(salt+password) hex），仅业务层使用 */
    private String pwd;
    /** 密码盐（随机 hex），仅业务层使用 */
    private String salt;
    /** 姓名 */
    private String name;
    /** 角色：doctor=双重角色（医生+患者），patient=仅患者角色 */
    private String role;
    /** 出生日期（yyyy-MM-dd） */
    private String birthDate;
    /** 性别：男 / 女 */
    private String gender;
    /** 身高（cm） */
    private BigDecimal height;
    /** 体重（kg） */
    private BigDecimal weight;

    /** 默认构造 */
    public User() {
    }

    /**
     * 全字段构造（供 DAO 行映射使用）。
     *
     * @param id        主键
     * @param tel       账号
     * @param pwd       密码密文
     * @param salt      密码盐
     * @param name      姓名
     * @param role      角色
     * @param birthDate 出生日期
     * @param gender    性别
     * @param height    身高
     * @param weight    体重
     */
    public User(int id, String tel, String pwd, String salt, String name, String role,
                String birthDate, String gender, BigDecimal height, BigDecimal weight) {
        this.id = id;
        this.tel = tel;
        this.pwd = pwd;
        this.salt = salt;
        this.name = name;
        this.role = role;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    /** @return 用户主键 */
    public int getId() {
        return id;
    }

    /** @param id 用户主键 */
    public void setId(int id) {
        this.id = id;
    }

    /** @return 账号（手机号） */
    public String getTel() {
        return tel;
    }

    /** @param tel 账号（手机号） */
    public void setTel(String tel) {
        this.tel = tel;
    }

    /** @return 密码密文（SHA-256 hex），界面层禁止展示 */
    public String getPwd() {
        return pwd;
    }

    /** @param pwd 密码密文 */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    /** @return 密码盐 */
    public String getSalt() {
        return salt;
    }

    /** @param salt 密码盐 */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /** @return 姓名 */
    public String getName() {
        return name;
    }

    /** @param name 姓名 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 角色：doctor / patient */
    public String getRole() {
        return role;
    }

    /** @param role 角色：doctor / patient */
    public void setRole(String role) {
        this.role = role;
    }

    /** @return 出生日期（yyyy-MM-dd） */
    public String getBirthDate() {
        return birthDate;
    }

    /** @param birthDate 出生日期（yyyy-MM-dd） */
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /** @return 性别（男 / 女） */
    public String getGender() {
        return gender;
    }

    /** @param gender 性别（男 / 女） */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /** @return 身高（cm） */
    public BigDecimal getHeight() {
        return height;
    }

    /** @param height 身高（cm） */
    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    /** @return 体重（kg） */
    public BigDecimal getWeight() {
        return weight;
    }

    /** @param weight 体重（kg） */
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    /**
     * 展示用性别文案：兼容空值（未填写时返回空串）。
     *
     * @return 性别或空串
     */
    public String genderText() {
        return gender == null ? "" : gender;
    }
}
