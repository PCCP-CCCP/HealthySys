package com.nd.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.common.entity.CheckItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查项数据访问对象（DAO）：负责 checkitem 表的增删改查。
 *
 * <p>所属模块：healthy-dao（数据访问层）。</p>
 *
 * <p>对应"检查项管理"大功能，供检查项管理面板与检查组编辑对话框使用。</p>
 *
 * <p>关键依赖：使用 {@link JdbcUtil} 获取连接/关闭资源；操作 {@link CheckItem} 实体；
 * 被检查项管理 UI 界面调用。</p>
 *
 * @author HealthySys 数据访问层
 */
public class CheckItemDao {

    /**
     * 查询全部检查项列表。
     *
     * @return 检查项列表（按创建时间倒序）
     */
    public List<CheckItem> queryAll() {
        // 结果列表容器，初始为空
        List<CheckItem> list = new ArrayList<CheckItem>();
        // 声明数据库资源引用，初始为 null，以便 finally 中安全关闭
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            // JdbcUtil.getConnection()：获取新数据库连接
            conn = JdbcUtil.getConnection();
            // 构造查询 SQL：按创建时间倒序排列，最新创建的排最前
            String sql = "select id, name, category, price, description, create_time "
                    + "from checkitem order by create_time desc";
            // prepareStatement：创建预处理语句（本查询无参数占位符）
            ps = conn.prepareStatement(sql);
            // executeQuery：执行 SELECT，返回结果集
            r = ps.executeQuery();
            // r.next()：逐行遍历结果集，直到无更多行返回 false
            while (r.next()) {
                // 将当前行映射为 CheckItem 对象并加入列表
                list.add(mapRow(r));
            }
        } catch (SQLException e) {
            // 查询失败统一包装为运行时异常，附带原始错误信息
            throw new RuntimeException("查询检查项失败：" + e.getMessage(), e);
        } finally {
            // JdbcUtil.close：确保无论成功或异常都关闭连接/语句/结果集
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 新增检查项。
     *
     * @param item 检查项对象（name/category/price/description）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int insert(CheckItem item) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // INSERT SQL：? 占位符依次对应 name/category/price/description/create_time
            String sql = "insert into checkitem(name, category, price, description, create_time) values (?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            // ps.setString：将检查项名称绑定到第 1 个 ?
            ps.setString(1, item.getName());
            // ps.setString：将分类绑定到第 2 个 ?
            ps.setString(2, item.getCategory());
            // ps.setBigDecimal：将价格绑定到第 3 个 ?（高精度小数）
            ps.setBigDecimal(3, item.getPrice());
            // ps.setString：将描述绑定到第 4 个 ?
            ps.setString(4, item.getDescription());
            // ps.setTimestamp：将当前时间作为创建时间绑定到第 5 个 ?
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            // executeUpdate：执行 INSERT，返回受影响行数（成功为 1）
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("新增检查项失败：" + e.getMessage(), e);
        } finally {
            // 查询无结果集，rs 传 null
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 修改检查项。
     *
     * @param item 检查项对象（需携带 id）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int update(CheckItem item) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // UPDATE SQL：按 id 定位，更新 name/category/price/description
            String sql = "update checkitem set name=?, category=?, price=?, description=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getName());       // 名称 → 第 1 个 ?
            ps.setString(2, item.getCategory());   // 分类 → 第 2 个 ?
            ps.setBigDecimal(3, item.getPrice());  // 价格 → 第 3 个 ?
            ps.setString(4, item.getDescription()); // 描述 → 第 4 个 ?
            ps.setInt(5, item.getId());            // 主键 id → WHERE 条件
            // executeUpdate：执行 UPDATE，返回受影响行数
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("修改检查项失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 删除检查项。
     *
     * @param id 检查项主键 ID
     * @return 受影响行数，&gt;0 表示成功
     */
    public int delete(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // DELETE SQL：按 id 删除
            String sql = "delete from checkitem where id = ?";
            ps = conn.prepareStatement(sql);
            // ps.setInt：将主键 id 绑定到 ? 占位符
            ps.setInt(1, id);
            // executeUpdate：执行 DELETE，返回受影响行数
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除检查项失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 将一行结果集映射为检查项对象。
     *
     * @param r 结果集（已定位到当前行）
     * @return CheckItem 对象
     * @throws SQLException 读取列失败时抛出
     */
    private CheckItem mapRow(ResultSet r) throws SQLException {
        // 创建空对象，通过 setter 逐字段填充
        CheckItem item = new CheckItem();
        item.setId(r.getInt("id"));                  // 主键
        item.setName(r.getString("name"));           // 名称
        item.setCategory(r.getString("category"));   // 分类
        item.setPrice(r.getBigDecimal("price"));     // 价格
        item.setDescription(r.getString("description")); // 描述
        item.setCreateTime(r.getTimestamp("create_time")); // 创建时间
        return item;
    }
}
