package com.nd.common.entity;

import java.sql.Timestamp;

/**
 * 检查组实体类：对应数据库 checkgroup 表。
 *
 * <p>将若干检查项打包为一个体检套餐（如"入职体检套餐"），
 * 通过 {@code itemCount} 展示组内包含的检查项数量（由 DAO 联表统计）。</p>
 *
 * @author HealthySys 公共模块
 */
public class CheckGroup {

    /** 主键 ID */
    private Integer id;
    /** 检查组名称 */
    private String name;
    /** 创建时间 */
    private Timestamp createTime;
    /** 组内包含的检查项数量（冗余展示字段，由 DAO 统计注入） */
    private int itemCount;

    /** 无参构造器（DAO 映射使用） */
    public CheckGroup() {
    }

    /**
     * 便捷构造器。
     *
     * @param name 检查组名称
     */
    public CheckGroup(String name) {
        this.name = name;
    }

    /** @return 检查组主键 ID */
    public Integer getId() {
        return id;
    }

    /** @param id 检查组主键 ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** @return 检查组名称 */
    public String getName() {
        return name;
    }

    /** @param name 检查组名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 检查组创建时间 */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /** @param createTime 检查组创建时间 */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /** @return 组内包含的检查项数量 */
    public int getItemCount() {
        return itemCount;
    }

    /** @param itemCount 组内包含的检查项数量 */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * 供下拉框直接显示名称。
     *
     * @return 检查组名称
     */
    @Override
    public String toString() {
        return name;
    }
}
