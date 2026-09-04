package com.nd.legacy.view;

import com.nd.common.db.JdbcUtil;

import java.sql.SQLException;

/**
 * 【遗留】JDBC 测试入口（未被当前主程序引用，仅保留参考）。
 *
 * <p>用于演示 {@link JdbcUtil} 的查询与增删改用法，不参与主流程。</p>
 *
 * @author HealthySys 遗留模块
 */
public class Demo {

    /**
     * 测试入口：演示修改用户密码的增删改操作。
     *
     * @param args 命令行参数（未使用）
     * @throws SQLException 数据库访问异常
     */
    public static void main(String[] args) throws SQLException {
        Object param[] = {"888", "133"};
        String sql = "update users set pwd = ? where tel = ?";
        int num = JdbcUtil.iudSql(sql, param);

        if (num == 0) {
            System.out.println("0000");
        } else {
            System.out.println("111");
        }
    }
}
