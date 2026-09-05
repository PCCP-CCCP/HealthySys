package com.nd.common.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 检查项实体类：对应数据库 checkitem 表。
 *
 * <p>所属模块：healthy-common（公共实体层）。</p>
 *
 * <p>描述一次可独立收费的体检检查项目，如"血常规""胸部X光"等，
 * 含分类、价格与描述信息，可被一个或多个检查组引用。</p>
 *
 * <p>关键依赖：被 CheckItemDao（检查项 CRUD）、CheckGroupDao（组内检查项查询）、
 * ExamResultDao（结果联表带出检查项名称）使用。</p>
 *
 * @author HealthySys 公共模块
 */
public class CheckItem {

    /** 主键 ID（数据库自增） */
    private Integer id;
    /** 检查项名称 */
    private String name;
    /** 分类（如：检验、影像等） */
    private String category;
    /** 价格（元，高精度小数用 BigDecimal） */
    private BigDecimal price;
    /** 描述说明 */
    private String description;
    /** 创建时间（数据库 timestamp） */
    private Timestamp createTime;

    /**
     * 无参构造器。
     *
     * <p>供框架/反射与 DAO 结果集逐字段 setXxx 映射使用。</p>
     */
    public CheckItem() {
    }

    /**
     * 带业务字段的便捷构造器（新增时使用，id/createTime 由数据库生成）。
     *
     * @param name        检查项名称
     * @param category    分类
     * @param price       价格（元）
     * @param description 描述说明
     */
    public CheckItem(String name, String category, BigDecimal price, String description) {
        this.name = name;           // 检查项名称
        this.category = category;   // 分类
        this.price = price;         // 价格
        this.description = description; // 描述
    }

    /** @return 检查项主键 ID */
    public Integer getId() {
        return id;
    }

    /** @param id 检查项主键 ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** @return 检查项名称 */
    public String getName() {
        return name;
    }

    /** @param name 检查项名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 检查项分类 */
    public String getCategory() {
        return category;
    }

    /** @param category 检查项分类 */
    public void setCategory(String category) {
        this.category = category;
    }

    /** @return 检查项价格（元） */
    public BigDecimal getPrice() {
        return price;
    }

    /** @param price 检查项价格（元） */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /** @return 检查项描述说明 */
    public String getDescription() {
        return description;
    }

    /** @param description 检查项描述说明 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 检查项创建时间 */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /** @param createTime 检查项创建时间 */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * 供下拉框直接显示名称。
     *
     * @return 检查项名称
     */
    @Override
    public String toString() {
        // Swing JComboBox 等组件调用 toString() 作为显示文本，返回名称即可
        return name;
    }
}
