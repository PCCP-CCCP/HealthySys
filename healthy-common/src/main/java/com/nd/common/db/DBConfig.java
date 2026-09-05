package com.nd.common.db;

/**
 * 数据库连接配置常量类。
 *
 * <p>所属模块：healthy-common（公共数据库层）。</p>
 *
 * <p>集中管理 MySQL 的连接地址、用户名与密码，避免在多个 DAO 中重复硬编码。
 * 修改数据库连接信息时，只需改动本类。</p>
 *
 * <p>关键依赖：被 {@link JdbcUtil#getConnection()} 读取 URL/USER/PASSWORD 以建立数据库连接。</p>
 *
 * @author HealthySys 公共模块
 */
public final class DBConfig {

    /**
     * MySQL 连接 JDBC 地址。
     *
     * <p>含以下参数：characterEncoding=utf-8 保证中文正常读写；
     * serverTimezone=Asia/Shanghai 指定时区；useSSL=false 关闭 SSL（本地开发环境）。</p>
     */
    public static final String URL =
            "jdbc:mysql://127.0.0.1:3306/newcenter"
                    + "?characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";

    /** 数据库用户名 */
    public static final String USER = "root";

    /** 数据库密码 */
    public static final String PASSWORD = "Youlhsj19.";

    /**
     * 工具类私有构造，禁止实例化。
     *
     * <p>本类全部为静态常量，不应通过 new 创建对象。</p>
     */
    private DBConfig() {
    }
}
