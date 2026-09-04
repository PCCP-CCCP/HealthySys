package com.nd.view.utils;

public class Session {
    // 当前登录用户账号
    public static String currentTel = "";
    public static String currentName = "";

    // 账号角色类型：doctor=拥有医生角色（可切换医生/患者视角），patient=仅患者角色
    public static String userRole = "patient";

    // 当前界面视角角色：doctor=医生视角（检查项管理/检查组管理），patient=患者视角（预约/跟踪管理）
    public static String currentRole = "patient";
}
