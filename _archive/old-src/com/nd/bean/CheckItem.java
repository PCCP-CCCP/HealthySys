package com.nd.bean;

public class CheckItem {//实体类
    private String cid;
    private String bh;
    private String cname;
    private String dw;
    private String ckfw;
    private int status;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getBh() {
        return bh;
    }

    public void setBh(String bh) {
        this.bh = bh;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getDw() {
        return dw;
    }

    public void setDw(String dw) {
        this.dw = dw;
    }

    public String getCkfw() {
        return ckfw;
    }

    public void setCkfw(String ckfw) {
        this.ckfw = ckfw;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
