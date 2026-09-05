package com.nd.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC 通用工具类：统一封装数据库连接、查询、增删改与资源关闭。
 *
 * <p>所属模块：healthy-common（公共数据库层）。</p>
 *
 * <p>提供两种使用方式：</p>
 * <ul>
 *   <li>静态无状态工具方法：{@link #getConnection()}、{@link #close(Connection, PreparedStatement, ResultSet)}
 *       供各功能 DAO 使用（自己管理连接与关闭）；</li>
 *   <li>兼容旧接口的查询方法：{@link #querySql(String, Object[])} 返回 ResultSet 后调用
 *       {@link #close()} 关闭本次查询打开的资源（供 UserDao 等保留历史调用习惯）。</li>
 * </ul>
 *
 * <p>关键依赖：读取 {@link DBConfig} 的连接配置；被所有 DAO 类（UserDao/CheckItemDao/CheckGroupDao/
 * ExamResultDao/AppointmentDao）调用。</p>
 *
 * @author HealthySys 公共模块
 */
public final class JdbcUtil {

    // ====== 兼容旧查询接口的静态持有器（非线程安全，仅用于单线程桌面应用）======

    /** 用于兼容旧查询接口：保存"本次查询"打开的连接 */
    private static Connection holderCon;
    /** 用于兼容旧查询接口：保存"本次查询"打开的预处理语句 */
    private static PreparedStatement holderPstm;
    /** 用于兼容旧查询接口：保存"本次查询"打开的结果集 */
    private static ResultSet holderRs;

    /**
     * 工具类私有构造，禁止实例化。
     *
     * <p>本类全部为静态方法，不应通过 new 创建对象。</p>
     */
    private JdbcUtil() {
    }

    /**
     * 静态块：加载 MySQL 驱动（类加载时执行一次）。
     *
     * <p>Class.forName 触发 com.mysql.cj.jdbc.Driver 类的静态注册，
     * 使 DriverManager 能识别该驱动。</p>
     */
    static {
        try {
            // Class.forName：加载并初始化 MySQL JDBC 驱动类，触发驱动自动注册
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // 找不到驱动类通常是因为 pom 中缺少 mysql-connector-java 依赖
            throw new RuntimeException("未找到 MySQL 驱动，请检查 lib 下的驱动 jar", e);
        }
    }

    /**
     * 建立一个新的数据库连接。
     *
     * <p>从 {@link DBConfig} 读取 URL/用户名/密码，通过 DriverManager 获取物理连接。
     * 每次调用返回新连接，调用方负责在 finally 中关闭。</p>
     *
     * @return 已打开的 Connection，调用方负责关闭
     * @throws RuntimeException 连接失败时抛出（包装 SQLException）
     */
    public static Connection getConnection() {
        try {
            // DriverManager.getConnection：根据 URL/用户名/密码建立到 MySQL 的物理连接
            return DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASSWORD);
        } catch (SQLException e) {
            // 连接失败（网络不通/数据库未启动/账号密码错误等）统一包装为运行时异常
            throw new RuntimeException("数据库连接失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将参数数组依次绑定到预处理语句上。
     *
     * <p>使用 PreparedStatement 的 ? 占位符实现参数化查询，防止 SQL 注入。
     * 参数下标从 1 开始。</p>
     *
     * @param pstm   预处理语句
     * @param param  可变参数数组（可为 null，表示无参数）
     * @throws SQLException 绑定失败时抛出
     */
    private static void setParams(PreparedStatement pstm, Object[] param) throws SQLException {
        // 仅当参数数组非空时才执行绑定
        if (param != null) {
            // 遍历参数数组，下标 i 从 0 开始，对应 PreparedStatement 的占位符序号 i+1
            for (int i = 0; i < param.length; i++) {
                // setObject：按参数类型自动映射到数据库类型，依次填入 ? 占位符
                pstm.setObject(i + 1, param[i]);
            }
        }
    }

    /**
     * 执行查询 SQL 并返回结果集（兼容旧接口的"查询后统一 close()"调用方式）。
     *
     * <p>注意：返回的 ResultSet 对应的连接与语句被保存在静态持有器中，
     * 读取完结果后必须调用 {@link #close()} 释放资源。由于使用静态字段持有，
     * 该接口非线程安全，本项目为单线程 Swing 桌面应用，可安全使用。</p>
     *
     * @param sql   查询 SQL，支持 ? 占位符
     * @param param 占位符参数数组（可为 null）
     * @return 查询结果集（ResultSet），调用方遍历后须调用 close()
     * @throws RuntimeException 查询失败时抛出（包装 SQLException）
     */
    public static ResultSet querySql(String sql, Object[] param) {
        try {
            // 获取新数据库连接，存入静态持有器供 close() 使用
            holderCon = getConnection();
            // prepareStatement：创建预处理语句，? 占位符由后续参数绑定填充
            holderPstm = holderCon.prepareStatement(sql);
            // 将参数数组依次绑定到 ? 占位符上
            setParams(holderPstm, param);
            // executeQuery：执行 SELECT，返回结果集
            holderRs = holderPstm.executeQuery();
            return holderRs;
        } catch (SQLException e) {
            throw new RuntimeException("查询失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行新增、修改、删除 SQL 并返回受影响行数（方法内自动关闭资源）。
     *
     * <p>使用 try-with-resources 自动关闭连接和语句，无需调用方手动 close。</p>
     *
     * @param sql   DML SQL（INSERT/UPDATE/DELETE），支持 ? 占位符
     * @param param 占位符参数数组（可为 null）
     * @return 受影响的行数
     * @throws RuntimeException 执行失败时抛出（包装 SQLException）
     */
    public static int iudSql(String sql, Object[] param) {
        // try-with-resources：连接和语句在 try 块结束后自动关闭，防止资源泄漏
        try (Connection con = getConnection();
             PreparedStatement pstm = con.prepareStatement(sql)) {
            // 将参数数组依次绑定到 ? 占位符上
            setParams(pstm, param);
            // executeUpdate：执行 INSERT/UPDATE/DELETE，返回受影响行数
            return pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("执行失败：" + e.getMessage(), e);
        }
    }

    /**
     * 关闭"兼容旧查询接口"持有器中的连接、语句与结果集（调用 {@link #querySql} 后调用）。
     *
     * <p>关闭顺序：先结果集、再语句、最后连接；关闭后置空静态持有器，避免重复关闭。</p>
     *
     * @throws RuntimeException 关闭过程中发生 SQLException 时抛出
     */
    public static void close() {
        try {
            // 按依赖逆序关闭：先关闭结果集
            if (holderRs != null) {
                holderRs.close();
            }
            // 再关闭预处理语句
            if (holderPstm != null) {
                holderPstm.close();
            }
            // 最后关闭数据库连接
            if (holderCon != null) {
                holderCon.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // 无论成功失败，都将静态持有器置空，防止下次查询复用旧资源
            holderRs = null;
            holderPstm = null;
            holderCon = null;
        }
    }

    /**
     * 通用资源关闭工具：关闭结果集、语句、连接（任一为 null 时安全跳过）。
     *
     * <p>供 DAO 层在 finally 块中调用，确保数据库资源被释放。</p>
     *
     * @param con 数据库连接（可为 null）
     * @param ps  预处理语句（可为 null）
     * @param rs  结果集（可为 null）
     */
    public static void close(Connection con, PreparedStatement ps, ResultSet rs) {
        try {
            // 关闭结果集（先关）
            if (rs != null) {
                rs.close();
            }
            // 关闭预处理语句
            if (ps != null) {
                ps.close();
            }
            // 关闭连接（最后关）
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
     * <p>在 DAO 事务方法的 catch 块中调用，确保 setAutoCommit(false) 后
     * 发生异常时未提交的更改被撤销，保持数据一致性。</p>
     *
     * @param con 数据库连接（可为 null）
     */
    public static void rollback(Connection con) {
        // 仅当连接非空时才尝试回滚，避免 NullPointerException
        if (con != null) {
            try {
                // con.rollback()：撤销当前事务中所有未提交的更改
                con.rollback();
            } catch (SQLException ignored) {
                // 回滚失败仅记录，不向上抛出（主异常已在 catch 块抛出）
            }
        }
    }
}
