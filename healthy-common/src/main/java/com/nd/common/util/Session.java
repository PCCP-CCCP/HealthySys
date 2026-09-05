package com.nd.common.util;

/**
 * 会话工具类：保存"当前登录用户"的全局登录态（静态字段，供各界面读取）。
 *
 * <p>所属模块：healthy-common（公共工具层）。</p>
 *
 * <p>角色相关字段含义：</p>
 * <ul>
 *   <li>{@link #userRole}：账号角色，登录时从数据库读取并写入。
 *       doctor = 拥有医生角色的双重账号（可切换医生/患者视角）；patient = 仅患者角色。</li>
 *   <li>{@link #currentRole}：当前界面视角。doctor = 医生视角（检查项管理/检查组管理/录入结果/查看患者结果）；
 *       patient = 患者视角（预约/跟踪管理）。</li>
 * </ul>
 *
 * <p>双角色设计要点：userRole 与 currentRole 分离——userRole 标识账号能力（医生账号可切换），
 * currentRole 标识当前界面显示哪个功能菜单。医生账号可"一键切换角色"，仅患者账号不能切换。</p>
 *
 * <p>关键依赖：被 UserService（登录写入姓名/角色）、各 UI 界面（读取当前用户态与角色视角）使用。</p>
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

    /**
     * 工具类私有构造，禁止实例化。
     *
     * <p>本类全部为静态字段，不应通过 new 创建对象。</p>
     */
    private Session() {
    }
}
