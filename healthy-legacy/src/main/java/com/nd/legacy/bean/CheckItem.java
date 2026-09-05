package com.nd.legacy.bean;

/**
 * 【遗留模块】旧版检查项实体类（JavaBean / POJO）。
 *
 * <p>所属模块：healthy-legacy（遗留模块，包含早期版本的代码，与新模块 healthy-common 等相互独立）。</p>
 *
 * <p>类的职责：对应数据库旧版 {@code checkitem} 表的旧字段结构，封装一条检查项目的完整信息，
 * 在 DAO、Service、View 三层之间传递数据。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>封装检查项的 ID、编号、名称、单位、参考范围、状态等属性；</li>
 *   <li>提供各属性的 getter/setter 方法供外部读写。</li>
 * </ul>
 *
 * <p>关键说明：与新版 {@code com.nd.common.entity.CheckItem}（字段为 id/name/category/price/description）
 * 结构不同，本类为旧版独立版本，不依赖新模块。若后续不再需要，可安全删除本类。</p>
 *
 * @author HealthySys 遗留模块
 */
public class CheckItem {

    /** 检查项 ID（对应数据库 checkitem 表的 cid 字段，String 类型主键） */
    private String cid;

    /** 检查项编号（对应数据库 bh 字段，业务编号） */
    private String bh;

    /** 检查项名称（对应数据库 cname 字段，如"血常规"、"肝功能"等） */
    private String cname;

    /** 计量单位（对应数据库 dw 字段，如"mmol/L"、"mg/dL"等） */
    private String dw;

    /** 参考范围（对应数据库 ckfw 字段，如"3.5-5.5"等正常值区间） */
    private String ckfw;

    /** 状态标记（对应数据库 status 字段，int 类型，0=启用，非 0=停用） */
    private int status;

    /**
     * 获取检查项 ID。
     *
     * @return 检查项 ID（cid）
     */
    public String getCid() {
        return cid;
    }

    /**
     * 设置检查项 ID。
     *
     * @param cid 检查项 ID
     */
    public void setCid(String cid) {
        this.cid = cid;
    }

    /**
     * 获取检查项编号。
     *
     * @return 检查项编号（bh）
     */
    public String getBh() {
        return bh;
    }

    /**
     * 设置检查项编号。
     *
     * @param bh 检查项编号
     */
    public void setBh(String bh) {
        this.bh = bh;
    }

    /**
     * 获取检查项名称。
     *
     * @return 检查项名称（cname）
     */
    public String getCname() {
        return cname;
    }

    /**
     * 设置检查项名称。
     *
     * @param cname 检查项名称
     */
    public void setCname(String cname) {
        this.cname = cname;
    }

    /**
     * 获取计量单位。
     *
     * @return 计量单位（dw）
     */
    public String getDw() {
        return dw;
    }

    /**
     * 设置计量单位。
     *
     * @param dw 计量单位
     */
    public void setDw(String dw) {
        this.dw = dw;
    }

    /**
     * 获取参考范围。
     *
     * @return 参考范围（ckfw）
     */
    public String getCkfw() {
        return ckfw;
    }

    /**
     * 设置参考范围。
     *
     * @param ckfw 参考范围
     */
    public void setCkfw(String ckfw) {
        this.ckfw = ckfw;
    }

    /**
     * 获取状态标记。
     *
     * @return 状态值（0=启用）
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置状态标记。
     *
     * @param status 状态值（0=启用，非 0=停用）
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
