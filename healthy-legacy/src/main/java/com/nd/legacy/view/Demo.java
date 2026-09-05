package com.nd.legacy.view;

import com.nd.common.db.JdbcUtil;

import java.sql.SQLException;

/**
 * 【遗留模块】JDBC 测试入口类（演示/测试类）。
 *
 * <p>所属模块：healthy-legacy（遗留模块，包含早期版本的代码，与新模块 healthy-common 等相互独立）。</p>
 *
 * <p>类的职责：作为早期版本的 JDBC 功能演示与测试入口，用于验证 {@link JdbcUtil}
 * 的增删改（IUD）操作是否正常工作。本类不参与主程序业务流程，仅保留供历史参考。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>演示如何使用 {@link JdbcUtil#iudSql(String, Object[])} 执行 UPDATE 语句；</li>
 *   <li>根据 SQL 执行影响的行数，在控制台输出操作结果标识。</li>
 * </ul>
 *
 * <p>关键依赖：</p>
 * <ul>
 *   <li>{@link JdbcUtil}：来自公共模块 healthy-common 的 JDBC 工具类，封装了数据库连接和 SQL 执行。</li>
 * </ul>
 *
 * <p>注意：本类未被当前主程序引用，仅保留参考。运行此类会直接修改数据库 users 表中的密码数据。</p>
 *
 * @author HealthySys 遗留模块
 */
public class Demo {

    /**
     * 测试主入口方法：演示通过 JDBC 修改用户密码的增删改操作。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>准备 SQL 参数数组：新密码和用户手机号；</li>
     *   <li>构造 UPDATE SQL 语句：根据手机号更新 users 表中的密码；</li>
     *   <li>调用 {@link JdbcUtil#iudSql(String, Object[])} 执行增删改 SQL；</li>
     *   <li>根据返回的影响行数判断操作结果：
     *     <ul>
     *       <li>若影响行数为 0（num==0），说明未匹配到记录，输出"0000"；</li>
     *       <li>否则说明更新成功，输出"111"。</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param args 命令行参数（本方法未使用）
     * @throws SQLException 数据库访问异常，由 JdbcUtil 执行 SQL 时发生的连接错误或 SQL 语法错误触发
     */
    public static void main(String[] args) throws SQLException {
        // 准备 SQL 绑定参数：第一个元素为新密码"888"，第二个元素为用户手机号"133"
        Object param[] = {"888", "133"};
        // 构造 UPDATE SQL 语句：根据手机号（tel）更新 users 表中的密码（pwd）
        // 使用 ? 占位符防止 SQL 注入，参数通过 Object 数组绑定
        String sql = "update users set pwd = ? where tel = ?";
        // 调用 JdbcUtil 的 iudSql 方法执行增删改 SQL，返回受影响的行数
        int num = JdbcUtil.iudSql(sql, param);

        // 判断 SQL 执行结果：num 为受影响的行数
        if (num == 0) {
            // num == 0 表示没有任何记录被更新（可能手机号不存在），输出"0000"标识失败
            System.out.println("0000");
        } else {
            // num > 0 表示至少有一条记录被成功更新，输出"111"标识成功
            System.out.println("111");
        }
    }
}
