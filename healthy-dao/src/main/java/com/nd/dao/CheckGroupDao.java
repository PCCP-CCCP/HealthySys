package com.nd.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.common.entity.CheckGroup;
import com.nd.common.entity.CheckItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查组数据访问对象（DAO）：负责 checkgroup / checkgroup_item 表的增删改查。
 *
 * <p>对应"检查组管理"大功能。检查组由若干检查项打包组成，涉及两张表，
 * 新增/修改/删除均在事务中完成（同时维护组内关联）。</p>
 *
 * @author HealthySys 数据访问层
 */
public class CheckGroupDao {

    /**
     * 查询全部检查组列表（含组内检查项数量）。
     *
     * @return 检查组列表（按创建时间倒序）
     */
    public List<CheckGroup> queryAll() {
        List<CheckGroup> list = new ArrayList<CheckGroup>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select g.id, g.name, g.create_time, count(i.item_id) as item_count "
                    + "from checkgroup g left join checkgroup_item i on g.id = i.group_id "
                    + "group by g.id, g.name, g.create_time order by g.create_time desc";
            ps = conn.prepareStatement(sql);
            r = ps.executeQuery();
            while (r.next()) {
                CheckGroup g = new CheckGroup();
                g.setId(r.getInt("id"));
                g.setName(r.getString("name"));
                g.setCreateTime(r.getTimestamp("create_time"));
                g.setItemCount(r.getInt("item_count"));
                list.add(g);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询检查组失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 查询某检查组包含的检查项列表。
     *
     * @param groupId 检查组 ID
     * @return 检查项列表（按名称排序）
     */
    public List<CheckItem> queryGroupItems(int groupId) {
        List<CheckItem> list = new ArrayList<CheckItem>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select c.id, c.name, c.category, c.price, c.description, c.create_time "
                    + "from checkitem c join checkgroup_item gi on c.id = gi.item_id "
                    + "where gi.group_id = ? order by c.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            r = ps.executeQuery();
            while (r.next()) {
                CheckItem item = new CheckItem();
                item.setId(r.getInt("id"));
                item.setName(r.getString("name"));
                item.setCategory(r.getString("category"));
                item.setPrice(r.getBigDecimal("price"));
                item.setDescription(r.getString("description"));
                item.setCreateTime(r.getTimestamp("create_time"));
                list.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询组内检查项失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 查询某检查组包含的检查项 ID 集合（用于编辑对话框预勾选）。
     *
     * @param groupId 检查组 ID
     * @return 检查项 ID 列表
     */
    public List<Integer> queryGroupItemIds(int groupId) {
        List<Integer> ids = new ArrayList<Integer>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select item_id from checkgroup_item where group_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            r = ps.executeQuery();
            while (r.next()) {
                ids.add(r.getInt("item_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询组内检查项ID失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return ids;
    }

    /**
     * 创建检查组（事务内：写入检查组 + 批量写入组内检查项关联）。
     *
     * @param name    检查组名称
     * @param itemIds 组内检查项 ID 列表（可为空）
     * @return 新建检查组的 ID，失败返回 -1
     */
    public int create(String name, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(
                    "insert into checkgroup(name, create_time) values(?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            int groupId = -1;
            if (keys.next()) {
                groupId = keys.getInt(1);
            }
            ps.close();
            if (groupId > 0 && itemIds != null && !itemIds.isEmpty()) {
                ps = conn.prepareStatement("insert into checkgroup_item(group_id, item_id) values(?,?)");
                for (Integer id : itemIds) {
                    ps.setInt(1, groupId);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return groupId;
        } catch (SQLException e) {
            JdbcUtil.rollback(conn);
            throw new RuntimeException("创建检查组失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, keys);
        }
    }

    /**
     * 修改检查组（事务内：更新名称 + 先清空原关联再重写勾选的检查项）。
     *
     * @param groupId 检查组 ID
     * @param name    新的检查组名称
     * @param itemIds 新的组内检查项 ID 列表（可为空）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int update(int groupId, String name, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("update checkgroup set name=? where id=?");
            ps.setString(1, name);
            ps.setInt(2, groupId);
            ps.executeUpdate();
            ps.close();
            // 分离：清空原有组内检查项
            ps = conn.prepareStatement("delete from checkgroup_item where group_id=?");
            ps.setInt(1, groupId);
            ps.executeUpdate();
            ps.close();
            // 加入：重新写入勾选的检查项
            ps = conn.prepareStatement("insert into checkgroup_item(group_id, item_id) values(?,?)");
            if (itemIds != null && !itemIds.isEmpty()) {
                for (Integer id : itemIds) {
                    ps.setInt(1, groupId);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return 1;
        } catch (SQLException e) {
            JdbcUtil.rollback(conn);
            throw new RuntimeException("修改检查组失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 删除检查组（事务内：先删组内关联，再删检查组本体）。
     *
     * @param groupId 检查组 ID
     * @return 受影响行数，&gt;0 表示成功
     */
    public int delete(int groupId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("delete from checkgroup_item where group_id=?");
            ps.setInt(1, groupId);
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("delete from checkgroup where id=?");
            ps.setInt(1, groupId);
            int num = ps.executeUpdate();
            conn.commit();
            return num;
        } catch (SQLException e) {
            JdbcUtil.rollback(conn);
            throw new RuntimeException("删除检查组失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }
}
