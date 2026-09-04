package com.nd.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.common.entity.ExamResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查结果数据访问对象（DAO）：负责 exam_result 表的录入与多维查询。
 *
 * <p>对应"跟踪管理 / 查看患者结果"大功能，提供：</p>
 * <ul>
 *   <li>按预约查结果（录入对话框预填）；</li>
 *   <li>保存某次预约的全部结果（先清空再写入，事务内完成）；</li>
 *   <li>按用户/姓名/检查项查询历次结果（结果总览与按检查项对比）。</li>
 * </ul>
 *
 * @author HealthySys 数据访问层
 */
public class ExamResultDao {

    /**
     * 查询某次预约的全部检查结果（含检查项名称）。
     *
     * @param appointmentId 预约 ID
     * @return 结果列表（按检查项名称排序）
     */
    public List<ExamResult> queryByAppointment(int appointmentId) {
        List<ExamResult> list = new ArrayList<ExamResult>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "c.name as item_name "
                    + "from exam_result r left join checkitem c on r.item_id = c.id "
                    + "where r.appointment_id = ? order by c.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setItemName(r.getString("item_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询预约结果失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 保存某次预约的全部检查结果（事务内：先清空该预约旧结果，再批量写入新结果）。
     *
     * @param appointmentId 预约 ID
     * @param results       检查结果列表
     * @return 受影响行数，&gt;0 表示成功
     */
    public int record(int appointmentId, List<ExamResult> results) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("delete from exam_result where appointment_id=?");
            ps.setInt(1, appointmentId);
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement(
                    "insert into exam_result(appointment_id, item_id, item_value, result_status) values(?,?,?,?)");
            if (results != null && !results.isEmpty()) {
                for (ExamResult er : results) {
                    ps.setInt(1, appointmentId);
                    ps.setInt(2, er.getItemId());
                    ps.setString(3, er.getItemValue());
                    ps.setString(4, er.getResultStatus());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return 1;
        } catch (SQLException e) {
            JdbcUtil.rollback(conn);
            throw new RuntimeException("保存检查结果失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, null);
        }
    }

    /**
     * 查询某用户某检查项的历次结果（患者视角：按检查项对比）。
     *
     * @param userTel 用户手机号
     * @param itemId  检查项 ID
     * @return 结果列表（按体检日期升序，便于观察趋势）
     */
    public List<ExamResult> queryByUserAndItem(String userTel, int itemId) {
        List<ExamResult> list = new ArrayList<ExamResult>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "a.exam_date "
                    + "from exam_result r join appointment a on r.appointment_id = a.id "
                    + "where a.user_tel = ? and r.item_id = ? "
                    + "order by a.exam_date asc, r.id asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            ps.setInt(2, itemId);
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户检查项结果失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 查询某用户所有历次检查结果（患者视角：结果总览）。
     *
     * @param userTel 用户手机号
     * @return 结果列表（按体检日期倒序）
     */
    public List<ExamResult> queryByUser(String userTel) {
        List<ExamResult> list = new ArrayList<ExamResult>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "a.exam_date, c.name as item_name, g.name as group_name "
                    + "from exam_result r "
                    + "join appointment a on r.appointment_id = a.id "
                    + "join checkitem c on r.item_id = c.id "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "where a.user_tel = ? "
                    + "order by a.exam_date desc, c.name asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询用户全部结果失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 按患者姓名模糊查询其所有历次检查结果（医生视角：查看患者结果-总览）。
     *
     * @param name 患者姓名（模糊匹配）
     * @return 结果列表（含患者姓名，按姓名/体检日期排序）
     */
    public List<ExamResult> queryByUserName(String name) {
        List<ExamResult> list = new ArrayList<ExamResult>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "a.exam_date, c.name as item_name, g.name as group_name, u.name as user_name "
                    + "from exam_result r "
                    + "join appointment a on r.appointment_id = a.id "
                    + "join checkitem c on r.item_id = c.id "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "join users u on a.user_tel = u.tel "
                    + "where u.name like ? "
                    + "order by u.name asc, a.exam_date desc, c.name asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                er.setUserName(r.getString("user_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException("按姓名查询结果失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 按患者姓名 + 检查项查询历次结果（医生视角：查看患者结果-按检查项对比）。
     *
     * @param name   患者姓名（模糊匹配）
     * @param itemId 检查项 ID
     * @return 结果列表（按姓名/体检日期升序）
     */
    public List<ExamResult> queryByNameAndItem(String name, int itemId) {
        List<ExamResult> list = new ArrayList<ExamResult>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = JdbcUtil.getConnection();
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "a.exam_date, c.name as item_name, g.name as group_name, u.name as user_name "
                    + "from exam_result r "
                    + "join appointment a on r.appointment_id = a.id "
                    + "join checkitem c on r.item_id = c.id "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "join users u on a.user_tel = u.tel "
                    + "where u.name like ? and r.item_id = ? "
                    + "order by u.name asc, a.exam_date asc, r.id asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
            ps.setInt(2, itemId);
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                er.setUserName(r.getString("user_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException("按姓名和检查项查询结果失败：" + e.getMessage(), e);
        } finally {
            JdbcUtil.close(conn, ps, r);
        }
        return list;
    }

    /**
     * 将一行结果集映射为检查结果对象（基础字段）。
     *
     * @param r 结果集（已定位到当前行）
     * @return ExamResult 对象
     * @throws SQLException 读取列失败时抛出
     */
    private ExamResult mapRow(ResultSet r) throws SQLException {
        ExamResult er = new ExamResult();
        er.setId(r.getInt("id"));
        er.setAppointmentId(r.getInt("appointment_id"));
        er.setItemId(r.getInt("item_id"));
        er.setItemValue(r.getString("item_value"));
        er.setResultStatus(r.getString("result_status"));
        return er;
    }
}
