package com.nd.common.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 检查项实体类：对应数据库 checkitem 表。
 *
 * <p>描述一次可独立收费的体检检查项目，如"血常规""胸部X光"等，
 * 含分类、价格与描述信息，可被一个或多个检查组引用。</p>
 *
 * @author HealthySys 公共模块
 */
public class CheckItem {

    /** 主键 ID */
    private Integer id;
    /** 检查项名称 */
    private String name;
    /** 分类（如：检验、影像等） */
    private String category;
    /** 价格（元） */
    private BigDecimal price;
    /** 描述说明 */
    private String description;
    /** 创建时间 */
    private Timestamp createTime;

    /** 无参构造器（框架/反射与 DAO 映射使用） */
    public CheckItem() {
    }

    /**
     * 带业务字段的便捷构造器。
     *
     * @param name        检查项名称
     * @param category    分类
     * @param price       价格（元）
     * @param description 描述说明
     */
    public CheckItem(String name, String category, BigDecimal price, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
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
        return name;
    }
}
