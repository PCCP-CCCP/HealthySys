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
 * <p>所属模块：healthy-dao（数据访问层）。</p>
 *
 * <p>对应"跟踪管理 / 查看患者结果"大功能，提供：</p>
 * <ul>
 *   <li>按预约查结果（录入对话框预填）；</li>
 *   <li>保存某次预约的全部结果（先清空再写入，事务内完成）；</li>
 *   <li>按用户/姓名/检查项查询历次结果（结果总览与按检查项对比）。</li>
 * </ul>
 *
 * <p>关键依赖：使用 {@link JdbcUtil} 获取连接/回滚/关闭资源；操作 {@link ExamResult} 实体；
 * 被医生录入结果界面与患者跟踪管理界面调用。</p>
 *
 * @author HealthySys 数据访问层
 */
public class ExamResultDao {

    /**
     * 查询某次预约的全部检查结果（含检查项名称）。
     *
     * <p>LEFT JOIN checkitem 带出检查项名称，供录入对话框预填显示。</p>
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
            // LEFT JOIN checkitem：联表带出检查项名称（c.name as item_name）
            // WHERE r.appointment_id = ? 过滤指定预约
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "c.name as item_name "
                    + "from exam_result r left join checkitem c on r.item_id = c.id "
                    + "where r.appointment_id = ? order by c.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);  // 绑定预约 ID
            r = ps.executeQuery();
            while (r.next()) {
                // 先用 mapRow 映射基础字段，再补充联表的检查项名称
                ExamResult er = mapRow(r);
                er.setItemName(r.getString("item_name"));  // 联表字段：检查项名称
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
     * <p>采用"先删后插"策略：先删除该预约的所有旧结果，再批量插入新结果，
     * 避免增量更新时遗漏删除未勾选的检查项。</p>
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
            // conn.setAutoCommit(false)：开启手动事务，确保删除+插入原子性
            conn.setAutoCommit(false);
            // 第一步：删除该预约的所有旧结果（重新录入时先清空）
            ps = conn.prepareStatement("delete from exam_result where appointment_id=?");
            ps.setInt(1, appointmentId);
            ps.executeUpdate();
            ps.close();
            // 第二步：批量插入新结果
            ps = conn.prepareStatement(
                    "insert into exam_result(appointment_id, item_id, item_value, result_status) values(?,?,?,?)");
            // 仅当结果列表非空时才批量插入
            if (results != null && !results.isEmpty()) {
                for (ExamResult er : results) {
                    ps.setInt(1, appointmentId);         // 预约 ID
                    ps.setInt(2, er.getItemId());      // 检查项 ID
                    ps.setString(3, er.getItemValue()); // 检测数值
                    ps.setString(4, er.getResultStatus()); // 判定结果（正常/异常）
                    ps.addBatch();  // 加入批处理队列
                }
                ps.executeBatch();  // 批量执行所有 INSERT
            }
            // conn.commit()：提交事务，删除旧数据+插入新数据同时生效
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
     * <p>JOIN appointment 联表查出体检日期，按日期升序排列便于观察指标趋势。</p>
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
            // JOIN appointment a：联表带出体检日期（a.exam_date）
            // WHERE a.user_tel = ? and r.item_id = ?：按用户+检查项双重过滤
            // ORDER BY a.exam_date asc：按日期升序，便于观察指标变化趋势
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "a.exam_date "
                    + "from exam_result r join appointment a on r.appointment_id = a.id "
                    + "where a.user_tel = ? and r.item_id = ? "
                    + "order by a.exam_date asc, r.id asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);  // 第 1 个 ?：用户手机号
            ps.setInt(2, itemId);     // 第 2 个 ?：检查项 ID
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));  // 联表字段：体检日期
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
     * <p>三表联查：exam_result → appointment → checkitem → checkgroup，
     * 一次查出结果+日期+检查项名+检查组名。</p>
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
            // 三表 JOIN：exam_result 关联 appointment 关联 checkitem，再 LEFT JOIN checkgroup 取组名
            // LEFT JOIN checkgroup：部分预约可能无检查组，LEFT JOIN 确保结果不丢失
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, "
                    + "a.exam_date, c.name as item_name, g.name as group_name "
                    + "from exam_result r "
                    + "join appointment a on r.appointment_id = a.id "
                    + "join checkitem c on r.item_id = c.id "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "where a.user_tel = ? "
                    + "order by a.exam_date desc, c.name asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);  // 绑定用户手机号
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));    // 体检日期
                er.setItemName(r.getString("item_name"));    // 检查项名称
                er.setGroupName(r.getString("group_name"));   // 检查组名称
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
     * <p>JOIN users 表按姓名模糊匹配（LIKE %name%），返回结果中附带患者姓名供医生辨认。</p>
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
            // JOIN users u：联表带出患者姓名（u.name as user_name）
            // WHERE u.name like ?：模糊匹配，参数在代码中拼接 %name%
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
            // 拼接 %name% 实现模糊匹配（前后加通配符）
            ps.setString(1, "%" + name + "%");
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapRow(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                er.setUserName(r.getString("user_name"));  // 联表字段：患者姓名
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
     * <p>在 {@link #queryByUserName(String)} 基础上增加检查项过滤，
     * 供医生对比同一检查项下不同患者的历史结果。</p>
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
            // 与 queryByUserName 相同的联表结构，WHERE 条件增加 r.item_id = ?
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
            ps.setString(1, "%" + name + "%");  // 第 1 个 ?：姓名模糊匹配
            ps.setInt(2, itemId);              // 第 2 个 ?：检查项 ID
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
     * <p>仅映射 exam_result 表原生列，联表冗余字段由各调用方法自行补充。</p>
     *
     * @param r 结果集（已定位到当前行）
     * @return ExamResult 对象
     * @throws SQLException 读取列失败时抛出
     */
    private ExamResult mapRow(ResultSet r) throws SQLException {
        ExamResult er = new ExamResult();
        er.setId(r.getInt("id"));                    // 结果主键
        er.setAppointmentId(r.getInt("appointment_id")); // 预约 ID
        er.setItemId(r.getInt("item_id"));          // 检查项 ID
        er.setItemValue(r.getString("item_value")); // 检测数值
        er.setResultStatus(r.getString("result_status")); // 判定结果
        return er;
    }
}
