package com.nd.common.entity;

/**
 * 预约实体类：对应数据库 appointment 表。
 *
 * <p>所属模块：healthy-common（公共实体层）。</p>
 *
 * <p>记录某个用户（手机号）对某个检查组在某一天进行的体检预约，
 * 状态流转：已预约 →（医生录入结果后）已完成，或（患者取消后）已取消。</p>
 *
 * <p>字段说明：前 6 个字段为 appointment 表原生列；groupName/userName 为联表冗余展示字段。</p>
 *
 * <p>关键依赖：被 AppointmentDao（预约 CRUD）、ExamResultDao（结果联表查询）使用。</p>
 *
 * @author HealthySys 公共模块
 */
public class Appointment {

    /** 预约主键 ID（数据库自增） */
    private Integer id;
    /** 预约人手机号（对应用户账号 users.tel） */
    private String userTel;
    /** 预约的检查组 ID（外键关联 checkgroup 表） */
    private Integer groupId;
    /** 体检日期（yyyy-MM-dd 字符串） */
    private String examDate;
    /** 预约状态：已预约 / 已完成 / 已取消 */
    private String status;
    /** 检查组名称（联表冗余展示字段，非本表列） */
    private String groupName;
    /** 患者姓名（联表冗余展示字段，供医生录入结果时显示） */
    private String userName;

    /**
     * 无参构造器。
     *
     * <p>供 DAO 结果集逐字段 setXxx 映射使用。</p>
     */
    public Appointment() {
    }

    /** @return 预约主键 ID */
    public Integer getId() {
        return id;
    }

    /** @param id 预约主键 ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** @return 预约人手机号 */
    public String getUserTel() {
        return userTel;
    }

    /** @param userTel 预约人手机号 */
    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    /** @return 预约的检查组 ID */
    public Integer getGroupId() {
        return groupId;
    }

    /** @param groupId 预约的检查组 ID */
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /** @return 体检日期（yyyy-MM-dd） */
    public String getExamDate() {
        return examDate;
    }

    /** @param examDate 体检日期（yyyy-MM-dd） */
    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    /** @return 预约状态（已预约 / 已完成 / 已取消） */
    public String getStatus() {
        return status;
    }

    /** @param status 预约状态（已预约 / 已完成 / 已取消） */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return 检查组名称（联表字段） */
    public String getGroupName() {
        return groupName;
    }

    /** @param groupName 检查组名称（联表字段） */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /** @return 患者姓名（联表字段，医生视角使用） */
    public String getUserName() {
        return userName;
    }

    /** @param userName 患者姓名（联表字段，医生视角使用） */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
