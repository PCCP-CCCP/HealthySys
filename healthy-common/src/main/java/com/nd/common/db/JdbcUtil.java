package com.nd.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC 通用工具类：统一封装数据库连接、查询、增删改与资源关闭。
 *
 * <p>提供两种使用方式：</p>
 * <ul>
 *   <li>静态无状态工具方法：{@link #getConnection()}、{@link #close(Connection, PreparedStatement, ResultSet)}
 *       供各功能 DAO 使用（自己管理连接与关闭）；</li>
 *   <li>兼容旧接口的查询方法：{@link #querySql(String, Object[])} 返回 ResultSet 后调用
 *       {@link #close()} 关闭本次查询打开的资源（供 UserDao 等保留历史调用习惯）。</li>
 * </ul>
 *
 * @author HealthySys 公共模块
 */
public final class JdbcUtil {

    /** 用于兼容旧查询接口：保存"本次查询"打开的连接、语句与结果集 */
    private static Connection holderCon;
    /** 用于兼容旧查询接口：保存"本次查询"打开的预处理语句 */
    private static PreparedStatement holderPstm;
    /** 用于兼容旧查询接口：保存"本次查询"打开的结果集 */
    private static ResultSet holderRs;

    /** 工具类私有构造，禁止实例化 */
    private JdbcUtil() {
    }

    /** 静态块：加载 MySQL 驱动（类加载时执行一次） */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("未找到 MySQL 驱动，请检查 lib 下的驱动 jar", e);
        }
    }

    /**
     * 建立一个新的数据库连接。
     *
     * @return 已打开的 Connection，调用方负责关闭
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将参数数组依次绑定到预处理语句上。
     *
     * @param pstm   预处理语句
     * @param param  可变参数数组（可为 null）
     * @throws SQLException 绑定失败时抛出
     */
    private static void setParams(PreparedStatement pstm, Object[] param) throws SQLException {
        if (param != null) {
            for (int i = 0; i < param.length; i++) {
                pstm.setObject(i + 1, param[i]);
            }
        }
    }

    /**
     * 执行查询 SQL 并返回结果集（兼容旧接口的"查询后统一 close()"调用方式）。
     *
     * <p>注意：返回的 ResultSet 对应的连接与语句被保存在静态持有器中，
     * 读取完结果后必须调用 {@link #close()} 释放资源。</p>
     *
     * @param sql   查询 SQL，支持 ? 占位符
     * @param param 占位符参数数组（可为 null）
     * @return 查询结果集
     */
    public static ResultSet querySql(String sql, Object[] param) {
        try {
            holderCon = getConnection();
            holderPstm = holderCon.prepareStatement(sql);
            setParams(holderPstm, param);
            holderRs = holderPstm.executeQuery();
            return holderRs;
        } catch (SQLException e) {
            throw new RuntimeException("查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行新增、修改、删除 SQL 并返回受影响行数（方法内自动关闭资源）。
     *
     * @param sql   DML SQL，支持 ? 占位符
     * @param param 占位符参数数组（可为 null）
     * @return 受影响的行数
     */
    public static int iudSql(String sql, Object[] param) {
        try (Connection con = getConnection();
             PreparedStatement pstm = con.prepareStatement(sql)) {
            setParams(pstm, param);
            return pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("执行失败：" + e.getMessage(), e);
        }
    }

    /**
     * 关闭"兼容旧查询接口"持有器中的连接、语句与结果集（调用 {@link #querySql} 后调用）。
     */
    public static void close() {
        try {
            if (holderRs != null) {
                holderRs.close();
            }
            if (holderPstm != null) {
                holderPstm.close();
            }
            if (holderCon != null) {
                holderCon.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            holderRs = null;
            holderPstm = null;
            holderCon = null;
        }
    }

    /**
     * 通用资源关闭工具：关闭结果集、语句、连接（任一为 null 时安全跳过）。
     *
     * @param con 数据库连接（可为 null）
     * @param ps  预处理语句（可为 null）
     * @param rs  结果集（可为 null）
     */
    public static void close(Connection con, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ignored) {
            // 关闭资源失败不影响主流程，静默忽略
        }
    }

    /**
     * 事务失败时回滚连接上的未提交操作（连接为 null 或回滚失败时安全跳过）。
     *
     * @param con 数据库连接（可为 null）
     */
    public static void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                // 回滚失败仅记录，不向上抛出
            }
        }
    }
}
