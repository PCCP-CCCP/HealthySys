package com.nd.common.db;

/**
 * 数据库连接配置常量类。
 *
 * <p>集中管理 MySQL 的连接地址、用户名与密码，避免在多个 DAO 中重复硬编码。
 * 修改数据库连接信息时，只需改动本类。</p>
 *
 * @author HealthySys 公共模块
 */
public final class DBConfig {

    /** MySQL 连接 JDBC 地址（含编码与时区参数，保证中文正常读写） */
    public static final String URL =
            "jdbc:mysql://127.0.0.1:3306/newcenter"
                    + "?characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";

    /** 数据库用户名 */
    public static final String USER = "root";

    /** 数据库密码 */
    public static final String PASSWORD = "Youlhsj19.";

    /** 工具类私有构造，禁止实例化 */
    private DBConfig() {
    }
}
