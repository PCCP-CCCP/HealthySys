package com.nd.common.entity;

/**
 * 检查结果实体类：对应数据库 exam_result 表。
 *
 * <p>所属模块：healthy-common（公共实体层）。</p>
 *
 * <p>记录某次预约下、每个检查项的实际检测数值与判定结果（正常/异常），
 * 通过预约与检查项可追溯到具体患者与检查项目。</p>
 *
 * <p>字段说明：前 5 个字段（id~resultStatus）为 exam_result 表原生列；
 * 后 4 个（itemName/examDate/groupName/userName）为 DAO 联表查询时冗余带出的展示字段，不持久化。</p>
 *
 * <p>关键依赖：被 ExamResultDao（结果录入与多维查询）使用。</p>
 *
 * @author HealthySys 公共模块
 */
public class ExamResult {

    /** 结果主键 ID（数据库自增） */
    private Integer id;
    /** 所属预约 ID（外键关联 appointment 表） */
    private Integer appointmentId;
    /** 检查项 ID（外键关联 checkitem 表） */
    private Integer itemId;
    /** 检测数值（字符串存储，便于不同单位/类型展示，如 "5.5"、"阳性"） */
    private String itemValue;
    /** 判定结果：正常 / 异常 */
    private String resultStatus;
    /** 检查项名称（联表冗余展示字段，非本表列） */
    private String itemName;
    /** 体检日期（联表冗余展示字段，取自 appointment.exam_date） */
    private String examDate;
    /** 检查组名称（联表冗余展示字段，非本表列） */
    private String groupName;
    /** 患者姓名（联表冗余展示字段，医生视角使用，非本表列） */
    private String userName;

    /**
     * 无参构造器。
     *
     * <p>供 DAO 结果集逐字段 setXxx 映射使用。</p>
     */
    public ExamResult() {
    }

    /** @return 结果主键 ID */
    public Integer getId() {
        return id;
    }

    /** @param id 结果主键 ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** @return 所属预约 ID */
    public Integer getAppointmentId() {
        return appointmentId;
    }

    /** @param appointmentId 所属预约 ID */
    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    /** @return 检查项 ID */
    public Integer getItemId() {
        return itemId;
    }

    /** @param itemId 检查项 ID */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /** @return 检测数值 */
    public String getItemValue() {
        return itemValue;
    }

    /** @param itemValue 检测数值 */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    /** @return 判定结果（正常 / 异常） */
    public String getResultStatus() {
        return resultStatus;
    }

    /** @param resultStatus 判定结果（正常 / 异常） */
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    /** @return 检查项名称（联表字段） */
    public String getItemName() {
        return itemName;
    }

    /** @param itemName 检查项名称（联表字段） */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /** @return 体检日期（联表字段） */
    public String getExamDate() {
        return examDate;
    }

    /** @param examDate 体检日期（联表字段） */
    public void setExamDate(String examDate) {
        this.examDate = examDate;
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

    /**
     * 供下拉框/日志直接展示"数值（结果）"。
     *
     * @return 形如 "5.5 (正常)" 的展示文本
     */
    @Override
    public String toString() {
        // 拼接为 "数值 (判定结果)" 格式，便于 Swing 下拉框/列表直接显示
        return itemValue + " (" + resultStatus + ")";
    }
}
