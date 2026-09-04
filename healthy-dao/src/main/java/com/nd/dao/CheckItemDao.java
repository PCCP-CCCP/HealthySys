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
 * <p>对应"检查项管理"大功能，供检查项管理面板与检查组编辑对话框使用。</p>
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
        List<CheckItem> list = new ArrayList<CheckItem>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select id, name, category, price, description, create_time "
                    + "from checkitem order by create_time desc";
            ps = conn.prepareStatement(sql);
            r = ps.executeQuery();
            while (r.next()) {
                list.add(mapRow(r));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询检查项失败：" + e.getMessage(), e);
        } finally {
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
            String sql = "insert into checkitem(name, category, price, description, create_time) values (?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getDescription());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("新增检查项失败：" + e.getMessage(), e);
        } finally {
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
            String sql = "update checkitem set name=?, category=?, price=?, description=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getDescription());
            ps.setInt(5, item.getId());
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
            String sql = "delete from checkitem where id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
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
        CheckItem item = new CheckItem();
        item.setId(r.getInt("id"));
        item.setName(r.getString("name"));
        item.setCategory(r.getString("category"));
        item.setPrice(r.getBigDecimal("price"));
        item.setDescription(r.getString("description"));
        item.setCreateTime(r.getTimestamp("create_time"));
        return item;
    }
}
