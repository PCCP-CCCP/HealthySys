package com.nd.legacy.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.legacy.bean.CheckItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 【遗留模块】旧版检查项数据访问对象（DAO 层）。
 *
 * <p>所属模块：healthy-legacy（遗留模块，包含早期版本的代码，与新模块 healthy-common 等相互独立）。</p>
 *
 * <p>类的职责：封装对数据库旧版 {@code checkitem} 表的查询操作，使用 JDBC 直接执行 SQL，
 * 将查询结果集映射为 {@link CheckItem} 实体对象列表。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>查询状态为启用（status=0）的全部旧版检查项；</li>
 *   <li>将 ResultSet 中的每行数据映射为 CheckItem 对象并封装为 List 返回。</li>
 * </ul>
 *
 * <p>关键依赖：</p>
 * <ul>
 *   <li>{@link JdbcUtil}：来自公共模块 healthy-common 的 JDBC 工具类，负责数据库连接与 SQL 执行；</li>
 *   <li>{@link CheckItem}：遗留模块的检查项实体类，用于承载查询结果。</li>
 * </ul>
 *
 * <p>注意：本类未被当前主程序引用，仅保留参考。</p>
 *
 * @author HealthySys 遗留模块
 */
public class CheckItemDao {

    /**
     * 查询状态为启用（status=0）的全部旧版检查项。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>构造参数化 SQL 查询语句，条件为 status=0（启用状态）；</li>
     *   <li>调用 {@link JdbcUtil#querySql(String, Object[])} 执行查询，获取 ResultSet 结果集；</li>
     *   <li>遍历结果集，逐行将字段值映射到 CheckItem 对象并加入列表；</li>
     *   <li>调用 {@link JdbcUtil#close()} 关闭数据库连接资源；</li>
     *   <li>返回封装好的检查项列表。</li>
     * </ol>
     *
     * @return 旧版检查项列表，若数据库中无启用状态的检查项则返回空列表
     * @throws SQLException 数据库访问异常，由 JDBC 操作过程中发生的 SQL 错误或连接失败触发
     */
    public List<CheckItem> getCheckItemData() throws SQLException {
        // 构造参数化 SQL：查询 checkitem 表中指定 status 状态的所有记录
        String sql = "select * from checkitem where status = ?";
        // 绑定参数：status = 0 表示启用状态
        Object[] param = {0};
        // 调用公共模块 JdbcUtil 执行查询 SQL，返回 ResultSet 结果集
        ResultSet rs = JdbcUtil.querySql(sql, param);
        // 初始化 List 用于存储映射后的 CheckItem 对象
        List<CheckItem> list = new ArrayList<CheckItem>();
        // 遍历结果集：逐行读取数据库记录
        while (rs.next()) {
            // 创建新的 CheckItem 实体对象，用于承载当前行数据
            CheckItem checkItem = new CheckItem();
            // 从结果集中读取 cid 字段（检查项 ID）并设置到实体对象
            checkItem.setCid(rs.getString("cid"));
            // 从结果集中读取 bh 字段（编号）并设置到实体对象
            checkItem.setBh(rs.getString("bh"));
            // 从结果集中读取 dw 字段（单位）并设置到实体对象
            checkItem.setDw(rs.getString("dw"));
            // 从结果集中读取 cname 字段（检查项名称）并设置到实体对象
            checkItem.setCname(rs.getString("cname"));
            // 从结果集中读取 ckfw 字段（参考范围）并设置到实体对象
            checkItem.setCkfw(rs.getString("ckfw"));
            // 从结果集中读取 status 字段（状态）并设置到实体对象（int 类型）
            checkItem.setStatus(rs.getInt("status"));
            // 将当前行映射完成的 CheckItem 对象加入结果列表
            list.add(checkItem);
        }
        // 调用 JdbcUtil 关闭数据库连接及相关资源（Statement、Connection 等）
        JdbcUtil.close();
        // 返回封装好的检查项列表
        return list;
    }
}
