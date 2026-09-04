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
 * <p>对应"预约"大功能：患者预约体检、查看我的预约、取消预约；
 * 医生按患者姓名查询其预约以录入结果。</p>
 *
 * @author HealthySys 数据访问层
 */
public class AppointmentDao {

    /**
     * 创建预约（初始状态为"已预约"）。
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
            String sql = "insert into appointment(user_tel, group_id, exam_date, status, create_time) values(?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            ps.setInt(2, groupId);
            ps.setString(3, examDate);
            ps.setString(4, "已预约");
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
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
            String sql = "select a.id, a.user_tel, a.group_id, a.exam_date, a.status, "
                    + "g.name as group_name "
                    + "from appointment a left join checkgroup g on a.group_id = g.id "
                    + "where a.user_tel = ? order by a.exam_date desc, a.id desc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            r = ps.executeQuery();
            while (r.next()) {
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
            String sql = "select a.id, a.user_tel, a.group_id, a.exam_date, a.status, "
                    + "g.name as group_name, u.name as user_name "
                    + "from appointment a "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "left join users u on a.user_tel = u.tel "
                    + "where u.name like ? order by a.exam_date desc, a.id desc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
            r = ps.executeQuery();
            while (r.next()) {
                Appointment a = mapRow(r);
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
     * @param appointmentId 预约 ID
     * @return 受影响行数，&gt;0 表示取消成功；0 表示状态不允许或记录不存在
     */
    public int cancel(int appointmentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "update appointment set status='已取消' where id=? and status='已预约'";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
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
     * @param appointmentId 预约 ID
     * @param status        新状态（已预约/已完成/已取消）
     * @return 受影响行数，&gt;0 表示成功
     */
    public int updateStatus(int appointmentId, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "update appointment set status=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
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
        a.setId(r.getInt("id"));
        a.setUserTel(r.getString("user_tel"));
        a.setGroupId(r.getInt("group_id"));
        a.setExamDate(r.getString("exam_date"));
        a.setStatus(r.getString("status"));
        a.setGroupName(r.getString("group_name"));
        return a;
    }
}
