package com.nd.common.util;

/**
 * 会话工具类：保存"当前登录用户"的全局登录态（静态字段，供各界面读取）。
 *
 * <p>角色相关字段含义：</p>
 * <ul>
 *   <li>{@link #userRole}：账号角色，登录时从数据库读取并写入。
 *       doctor = 拥有医生角色的双重账号（可切换医生/患者视角）；patient = 仅患者角色。</li>
 *   <li>{@link #currentRole}：当前界面视角。doctor = 医生视角（检查项管理/检查组管理/录入结果/查看患者结果）；
 *       patient = 患者视角（预约/跟踪管理）。</li>
 * </ul>
 *
 * @author HealthySys 公共模块
 */
public final class Session {

    /** 当前登录用户账号（手机号），登出后清空 */
    public static String currentTel = "";

    /** 当前登录用户姓名，登出后清空 */
    public static String currentName = "";

    /** 账号角色类型：doctor=拥有医生角色（可切换医生/患者视角），patient=仅患者角色 */
    public static String userRole = "patient";

    /** 当前界面视角角色：doctor=医生视角，patient=患者视角 */
    public static String currentRole = "patient";

    /** 工具类私有构造，禁止实例化 */
    private Session() {
    }
}
