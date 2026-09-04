package com.nd.legacy.bean;

/**
 * 【遗留】旧版检查项实体类（未被当前主程序引用，仅保留参考）。
 *
 * <p>对应旧版 checkitem 表的旧字段结构（cid/bh/cname/dw/ckfw/status），
 * 与新版 {@code com.nd.common.entity.CheckItem}（id/name/category/price/description）不同。
 * 若后续不再需要，可安全删除本类。</p>
 *
 * @author HealthySys 遗留模块
 */
public class CheckItem {

    /** 检查项 ID */
    private String cid;
    /** 编号 */
    private String bh;
    /** 检查项名称 */
    private String cname;
    /** 单位 */
    private String dw;
    /** 参考范围 */
    private String ckfw;
    /** 状态（0=启用） */
    private int status;

    /** @return 检查项 ID */
    public String getCid() {
        return cid;
    }

    /** @param cid 检查项 ID */
    public void setCid(String cid) {
        this.cid = cid;
    }

    /** @return 编号 */
    public String getBh() {
        return bh;
    }

    /** @param bh 编号 */
    public void setBh(String bh) {
        this.bh = bh;
    }

    /** @return 检查项名称 */
    public String getCname() {
        return cname;
    }

    /** @param cname 检查项名称 */
    public void setCname(String cname) {
        this.cname = cname;
    }

    /** @return 单位 */
    public String getDw() {
        return dw;
    }

    /** @param dw 单位 */
    public void setDw(String dw) {
        this.dw = dw;
    }

    /** @return 参考范围 */
    public String getCkfw() {
        return ckfw;
    }

    /** @param ckfw 参考范围 */
    public void setCkfw(String ckfw) {
        this.ckfw = ckfw;
    }

    /** @return 状态（0=启用） */
    public int getStatus() {
        return status;
    }

    /** @param status 状态（0=启用） */
    public void setStatus(int status) {
        this.status = status;
    }
}
