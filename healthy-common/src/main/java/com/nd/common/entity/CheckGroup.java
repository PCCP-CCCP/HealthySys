package com.nd.common.entity;

import java.sql.Timestamp;

/**
 * 检查组实体类：对应数据库 checkgroup 表。
 *
 * <p>所属模块：healthy-common（公共实体层）。</p>
 *
 * <p>将若干检查项打包为一个体检套餐（如"入职体检套餐"），
 * 通过 {@code itemCount} 展示组内包含的检查项数量（由 DAO 联表统计注入）。</p>
 *
 * <p>关键依赖：被 CheckGroupDao（检查组 CRUD 与组项关联维护）使用；
 * 组内检查项通过中间表 checkgroup_item 关联，CheckItem 为被引用实体。</p>
 *
 * @author HealthySys 公共模块
 */
public class CheckGroup {

    /** 主键 ID（数据库自增） */
    private Integer id;
    /** 检查组名称 */
    private String name;
    /** 创建时间（数据库 timestamp） */
    private Timestamp createTime;
    /** 组内包含的检查项数量（冗余展示字段，由 DAO 联表 count 统计注入） */
    private int itemCount;

    /**
     * 无参构造器。
     *
     * <p>供 DAO 结果集逐字段 setXxx 映射使用。</p>
     */
    public CheckGroup() {
    }

    /**
     * 便捷构造器：仅设置名称（新增时使用，id/createTime 由数据库生成）。
     *
     * @param name 检查组名称
     */
    public CheckGroup(String name) {
        // 仅设置名称，id 与 createTime 由数据库自增/默认值生成
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
        // Swing JComboBox 等组件调用 toString() 作为显示文本，返回名称即可
        return name;
    }
}
