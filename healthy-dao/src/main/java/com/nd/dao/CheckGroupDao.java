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
 * <p>所属模块：healthy-dao（数据访问层）。</p>
 *
 * <p>对应"检查组管理"大功能。检查组由若干检查项打包组成，涉及两张表（checkgroup 主表 +
 * checkgroup_item 中间关联表），新增/修改/删除均在事务中完成（同时维护组内关联）。</p>
 *
 * <p>关键依赖：使用 {@link JdbcUtil} 获取连接/回滚/关闭资源；操作 {@link CheckGroup} 与
 * {@link CheckItem} 实体；被检查组管理 UI 界面调用。</p>
 *
 * @author HealthySys 数据访问层
 */
public class CheckGroupDao {

    /**
     * 查询全部检查组列表（含组内检查项数量）。
     *
     * <p>使用 LEFT JOIN + GROUP BY 联表统计每个组包含的检查项数量。</p>
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
            // LEFT JOIN：左表为 checkgroup，右表为关联表 checkgroup_item；
            // count(i.item_id)：统计每个组关联的检查项数量（0 也会被统计出来，因 LEFT JOIN 保留左表全部行）
            // GROUP BY：按组分组聚合，order by 按创建时间倒序
            String sql = "select g.id, g.name, g.create_time, count(i.item_id) as item_count "
                    + "from checkgroup g left join checkgroup_item i on g.id = i.group_id "
                    + "group by g.id, g.name, g.create_time order by g.create_time desc";
            ps = conn.prepareStatement(sql);
            r = ps.executeQuery();
            while (r.next()) {
                // 逐行映射为 CheckGroup 对象
                CheckGroup g = new CheckGroup();
                g.setId(r.getInt("id"));                    // 组主键
                g.setName(r.getString("name"));             // 组名称
                g.setCreateTime(r.getTimestamp("create_time")); // 创建时间
                g.setItemCount(r.getInt("item_count"));     // 联表统计的检查项数量
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
     * <p>通过 checkgroup_item 中间表 JOIN checkitem 查出组内所有检查项。</p>
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
            // JOIN checkgroup_item gi：通过中间表关联，WHERE gi.group_id = ? 过滤指定组
            String sql = "select c.id, c.name, c.category, c.price, c.description, c.create_time "
                    + "from checkitem c join checkgroup_item gi on c.id = gi.item_id "
                    + "where gi.group_id = ? order by c.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);  // 绑定检查组 ID 到 ?
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
     * <p>仅查 item_id 列，不查完整检查项信息，供编辑对话框初始化复选框选中状态。</p>
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
            // 仅查询关联表中的 item_id 列，数据量小、查询快
            String sql = "select item_id from checkgroup_item where group_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            r = ps.executeQuery();
            while (r.next()) {
                // 仅取出 item_id 加入 ID 列表
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
     * <p>事务流程：关闭自动提交 → 插入检查组主表并获取自增 ID →
     * 批量插入中间表关联 → 全部成功后 commit。任一步骤失败则 rollback 回滚。</p>
     *
     * @param name    检查组名称
     * @param itemIds 组内检查项 ID 列表（可为空，表示创建空组）
     * @return 新建检查组的 ID，失败返回 -1
     */
    public int create(String name, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = JdbcUtil.getConnection();
            // conn.setAutoCommit(false)：关闭自动提交，开启手动事务模式
            // 后续所有 SQL 执行后需显式 commit 才生效
            conn.setAutoCommit(false);
            // prepareStatement(..., Statement.RETURN_GENERATED_KEYS)：
            // 指定需要获取自增主键值，用于后续插入关联表
            ps = conn.prepareStatement(
                    "insert into checkgroup(name, create_time) values(?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);                    // 绑定组名称
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // 当前时间
            // executeUpdate：执行 INSERT 到主表
            ps.executeUpdate();
            // getGeneratedKeys：获取数据库自增生成的主键 ID
            keys = ps.getGeneratedKeys();
            int groupId = -1;
            // keys.next()：移动到主键结果集第一行
            if (keys.next()) {
                groupId = keys.getInt(1);  // 取出自增 ID
            }
            // ps.close()：关闭主表插入的语句对象，复用连接准备下一条 SQL
            ps.close();
            // 仅当成功获取到 groupId 且传入了非空的检查项 ID 列表时，才写入中间关联表
            if (groupId > 0 && itemIds != null && !itemIds.isEmpty()) {
                // 准备批量插入中间表的预处理语句
                ps = conn.prepareStatement("insert into checkgroup_item(group_id, item_id) values(?,?)");
                for (Integer id : itemIds) {
                    ps.setInt(1, groupId);  // 第 1 个 ?：检查组 ID
                    ps.setInt(2, id);       // 第 2 个 ?：检查项 ID
                    // addBatch：将当前参数组加入批处理队列，不立即发送到数据库
                    ps.addBatch();
                }
                // executeBatch：一次性将批处理队列中所有 INSERT 发送到数据库执行，减少网络往返
                ps.executeBatch();
            }
            // conn.commit()：提交事务，所有 INSERT 永久生效
            conn.commit();
            return groupId;
        } catch (SQLException e) {
            // JdbcUtil.rollback：事务失败时回滚所有未提交的更改，保持数据一致
            JdbcUtil.rollback(conn);
            throw new RuntimeException("创建检查组失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, keys);
        }
    }

    /**
     * 修改检查组（事务内：更新名称 + 先清空原关联再重写勾选的检查项）。
     *
     * <p>事务流程：关闭自动提交 → UPDATE 组名称 → DELETE 旧关联 →
     * 批量 INSERT 新关联 → commit。采用"先删后插"策略简化逻辑。</p>
     *
     * @param groupId 检查组 ID
     * @param name    新的检查组名称
     * @param itemIds 新的组内检查项 ID 列表（可为空，表示清空组内所有检查项）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int update(int groupId, String name, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // conn.setAutoCommit(false)：开启手动事务
            conn.setAutoCommit(false);
            // 第一步：更新检查组主表名称
            ps = conn.prepareStatement("update checkgroup set name=? where id=?");
            ps.setString(1, name);   // 新名称
            ps.setInt(2, groupId);   // WHERE 条件：组 ID
            ps.executeUpdate();
            ps.close();
            // 第二步（分离）：清空原有组内检查项关联，为重新写入做准备
            ps = conn.prepareStatement("delete from checkgroup_item where group_id=?");
            ps.setInt(1, groupId);
            ps.executeUpdate();
            ps.close();
            // 第三步（加入）：重新写入用户勾选的检查项关联
            ps = conn.prepareStatement("insert into checkgroup_item(group_id, item_id) values(?,?)");
            // 仅当传入了非空检查项 ID 列表时才批量插入（空列表表示清空组内所有项）
            if (itemIds != null && !itemIds.isEmpty()) {
                for (Integer id : itemIds) {
                    ps.setInt(1, groupId);
                    ps.setInt(2, id);
                    ps.addBatch();  // 加入批处理队列
                }
                ps.executeBatch();  // 批量执行
            }
            // conn.commit()：提交事务，三步操作同时生效或同时回滚
            conn.commit();
            return 1;
        } catch (SQLException e) {
            // 任一步骤失败则回滚，避免出现"名称改了但关联没改"的不一致状态
            JdbcUtil.rollback(conn);
            throw new RuntimeException("修改检查组失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 删除检查组（事务内：先删组内关联，再删检查组本体）。
     *
     * <p>必须先删除中间表关联记录，否则主表删除后关联记录成为孤儿数据。</p>
     *
     * @param groupId 检查组 ID
     * @return 受影响行数，&gt;0 表示成功
     */
    public int delete(int groupId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // conn.setAutoCommit(false)：开启手动事务，确保两步删除原子性
            conn.setAutoCommit(false);
            // 第一步：先删除中间表中的组项关联记录（外键依赖）
            ps = conn.prepareStatement("delete from checkgroup_item where group_id=?");
            ps.setInt(1, groupId);
            ps.executeUpdate();
            ps.close();
            // 第二步：再删除检查组主表记录
            ps = conn.prepareStatement("delete from checkgroup where id=?");
            ps.setInt(1, groupId);
            int num = ps.executeUpdate();  // 返回主表删除的行数
            // conn.commit()：提交事务，两步删除同时生效
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
