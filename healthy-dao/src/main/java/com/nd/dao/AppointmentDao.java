package com.nd.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.common.entity.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约数据访问对象（DAO）：负责 appointment 表的创建、查询、取消与状态更新。
 *
 * <p>所属模块：healthy-dao（数据访问层）。</p>
 *
 * <p>对应"预约"大功能：患者预约体检、查看我的预约、取消预约；
 * 医生按患者姓名查询其预约以录入结果。</p>
 *
 * <p>关键依赖：使用 {@link JdbcUtil} 获取连接/关闭资源；操作 {@link Appointment} 实体；
 * 被患者预约界面、医生录入结果界面调用。</p>
 *
 * @author HealthySys 数据访问层
 */
public class AppointmentDao {

    /**
     * 创建预约（初始状态为"已预约"）。
     *
     * <p>插入时自动设置当前时间为创建时间，状态字段硬编码为"已预约"。</p>
     *
     * @param userTel  预约人手机号
     * @param groupId  预约的检查组 ID
     * @param examDate 体检日期（yyyy-MM-dd）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int create(String userTel, int groupId, String examDate) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // INSERT SQL：? 依次对应 user_tel/group_id/exam_date/status/create_time
            String sql = "insert into appointment(user_tel, group_id, exam_date, status, create_time) values(?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);           // 预约人手机号
            ps.setInt(2, groupId);              // 检查组 ID
            ps.setString(3, examDate);          // 体检日期
            ps.setString(4, "已预约");           // 初始状态固定为"已预约"
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // 创建时间
            // executeUpdate：执行 INSERT，返回受影响行数
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("创建预约失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 查询某用户的全部预约（联表带出检查组名称）。
     *
     * <p>LEFT JOIN checkgroup 带出检查组名称，供患者在"我的预约"列表中显示。</p>
     *
     * @param userTel 用户手机号
     * @return 预约列表（按体检日期倒序）
     */
    public List<Appointment> queryByUser(String userTel) {
        List<Appointment> list = new ArrayList<Appointment>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            // LEFT JOIN checkgroup g：联表带出检查组名称（g.name as group_name）
            // WHERE a.user_tel = ?：按用户手机号过滤
            // ORDER BY a.exam_date desc：按体检日期倒序，最近的预约排最前
            String sql = "select a.id, a.user_tel, a.group_id, a.exam_date, a.status, "
                    + "g.name as group_name "
                    + "from appointment a left join checkgroup g on a.group_id = g.id "
                    + "where a.user_tel = ? order by a.exam_date desc, a.id desc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);  // 绑定用户手机号
            r = ps.executeQuery();
            while (r.next()) {
                // 映射当前行（含联表的检查组名称）
                list.add(mapRow(r));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户预约失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 按患者姓名模糊查询预约列表（供医生录入结果时选择患者与体检记录）。
     *
     * <p>LEFT JOIN users 带出患者姓名，WHERE u.name LIKE 模糊匹配。</p>
     *
     * @param name 患者姓名（模糊匹配）
     * @return 预约列表（含患者姓名，按体检日期倒序）
     */
    public List<Appointment> queryByUserName(String name) {
        List<Appointment> list = new ArrayList<Appointment>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            // LEFT JOIN users u：联表带出患者姓名（u.name as user_name）
            // WHERE u.name like ?：模糊匹配，参数拼接 %name%
            String sql = "select a.id, a.user_tel, a.group_id, a.exam_date, a.status, "
                    + "g.name as group_name, u.name as user_name "
                    + "from appointment a "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "left join users u on a.user_tel = u.tel "
                    + "where u.name like ? order by a.exam_date desc, a.id desc";
            ps = conn.prepareStatement(sql);
            // 拼接 %name% 实现前后模糊匹配
            ps.setString(1, "%" + name + "%");
            r = ps.executeQuery();
            while (r.next()) {
                Appointment a = mapRow(r);
                // 补充联表字段：患者姓名（mapRow 不含此字段）
                a.setUserName(r.getString("user_name"));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("按姓名查询预约失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 取消预约：仅允许取消状态为"已预约"的记录（已完成/已取消不可取消）。
     *
     * <p>通过 SQL WHERE 条件 status='已预约' 实现状态前置校验，
     * 若记录已被取消或已完成则 WHERE 不匹配，受影响行数为 0。</p>
     *
     * @param appointmentId 预约 ID
     * @return 受影响行数，&gt;0 表示取消成功；0 表示状态不允许或记录不存在
     */
    public int cancel(int appointmentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // UPDATE SQL：WHERE id=? AND status='已预约'——仅当当前状态为"已预约"时才能取消
            // 这是一种乐观并发控制：防止重复取消或取消已完成的预约
            String sql = "update appointment set status='已取消' where id=? and status='已预约'";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);  // 绑定预约 ID
            // executeUpdate：执行 UPDATE，返回受影响行数（0 表示状态不允许）
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("取消预约失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 更新预约状态（如医生录入结果后置为"已完成"）。
     *
     * <p>状态流转：已预约 → 已完成（医生录入结果后）；已预约 → 已取消（患者主动取消）。</p>
     *
     * @param appointmentId 预约 ID
     * @param status        新状态（已预约/已完成/已取消）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int updateStatus(int appointmentId, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            // UPDATE SQL：按 id 更新状态字段
            String sql = "update appointment set status=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);   // 新状态（如"已完成"）
            ps.setInt(2, appointmentId); // WHERE 条件：预约 ID
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新预约状态失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 将一行结果集映射为预约对象（含联表的检查组名称）。
     *
     * @param r 结果集（已定位到当前行）
     * @return Appointment 对象
     * @throws SQLException 读取列失败时抛出
     */
    private Appointment mapRow(ResultSet r) throws SQLException {
        Appointment a = new Appointment();
        a.setId(r.getInt("id"));                    // 预约主键
        a.setUserTel(r.getString("user_tel"));     // 预约人手机号
        a.setGroupId(r.getInt("group_id"));         // 检查组 ID
        a.setExamDate(r.getString("exam_date"));    // 体检日期
        a.setStatus(r.getString("status"));         // 预约状态
        a.setGroupName(r.getString("group_name"));   // 联表字段：检查组名称
        return a;
    }
}
