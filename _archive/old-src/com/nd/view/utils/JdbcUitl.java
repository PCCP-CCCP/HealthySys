package com.nd.view.utils;

import com.nd.view.entity.Appointment;
import com.nd.view.entity.CheckGroup;
import com.nd.view.entity.CheckItem;
import com.nd.view.entity.ExamResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUitl {//java连接数据库

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/newcenter?characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Youlhsj19.";

    private Connection con = null;
    private PreparedStatement pstm = null;
    private ResultSet rs = null;

    //1、加载驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //2、建立连接
    private Connection getConnection() {
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            return con;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //3、执行查询sql语句--获得查询结果
    public ResultSet querySql(String sql, Object[] param) {
        try {
            con = getConnection();
            pstm = con.prepareStatement(sql);
            if (param != null) {
                //将实际参数值 赋值 到sql语句里
                for (int i = 0; i < param.length; i++) {
                    pstm.setObject(i + 1, param[i]);
                }
            }
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }

    //3、执行新增、修改、删除sql语句 -- 获得结果
    public int iudSql(String sql, Object[] param) {
        int num = 0;
        try {
            con = getConnection();
            pstm = con.prepareStatement(sql);
            if (param != null) {
                //将实际参数值 赋值 到sql语句里
                for (int i = 0; i < param.length; i++) {
                    pstm.setObject(i + 1, param[i]);
                }
            }
            num = pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return num;
    }

    //4、关闭流
    public void close() {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //5、查询检查项列表（供 MainView 使用）
    public static List<CheckItem> queryCheckItems() {
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select id, name, category, price, description, create_time from checkitem";
            ps = conn.prepareStatement(sql);
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
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //6、新增检查项（供 CreateCheckItemDialog 使用）
    public static int insertCheckItem(CheckItem item) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "insert into checkitem(name, category, price, description, create_time) values (?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getDescription());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, null);
        }
    }

    //7、修改检查项（供编辑使用）
    public static int updateCheckItem(CheckItem item) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "update checkitem set name=?, category=?, price=?, description=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getDescription());
            ps.setInt(5, item.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, null);
        }
    }

    //8、删除检查项（供删除使用）
    public static int deleteCheckItem(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "delete from checkitem where id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, null);
        }
    }

    //9、查询检查组列表（包含组内检查项数量）
    public static List<CheckGroup> queryCheckGroups() {
        List<CheckGroup> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //10、查询某检查组内的检查项
    public static List<CheckItem> queryGroupItems(int groupId) {
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select c.id, c.name, c.category, c.price, c.description, c.create_time "
                    + "from checkitem c join checkgroup_item gi on c.id = gi.item_id "
                    + "where gi.group_id = ? order by c.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            r = ps.executeQuery();
            while (r.next()) {
                list.add(mapCheckItem(r));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //11、查询某检查组包含的检查项ID集合（用于编辑时预勾选）
    public static List<Integer> queryGroupItemIds(int groupId) {
        List<Integer> ids = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select item_id from checkgroup_item where group_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            r = ps.executeQuery();
            while (r.next()) {
                ids.add(r.getInt("item_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return ids;
    }

    //12、创建检查组（勾选多个检查项形成检查组）
    public static int createCheckGroup(String name, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("insert into checkgroup(name, create_time) values(?,?)", Statement.RETURN_GENERATED_KEYS);
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
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (keys != null) keys.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //13、修改检查组（更新名称；重设组内检查项：先分离后加入）
    public static int updateCheckGroup(int groupId, String name, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //14、删除检查组（连带删除组内关联）
    public static int deleteCheckGroup(int groupId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //15、创建预约（预约体检）
    public static int createAppointment(String userTel, int groupId, String examDate) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "insert into appointment(user_tel, group_id, exam_date, status, create_time) values(?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            ps.setInt(2, groupId);
            ps.setString(3, examDate);
            ps.setString(4, "已预约");
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //16、查询某用户的预约列表
    public static List<Appointment> queryAppointmentsByUser(String userTel) {
        List<Appointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select a.id, a.user_tel, a.group_id, a.exam_date, a.status, a.create_time, g.name as group_name "
                    + "from appointment a left join checkgroup g on a.group_id = g.id "
                    + "where a.user_tel = ? order by a.exam_date desc, a.id desc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            r = ps.executeQuery();
            while (r.next()) {
                Appointment a = new Appointment();
                a.setId(r.getInt("id"));
                a.setUserTel(r.getString("user_tel"));
                a.setGroupId(r.getInt("group_id"));
                a.setExamDate(r.getString("exam_date"));
                a.setStatus(r.getString("status"));
                a.setGroupName(r.getString("group_name"));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //17、按患者姓名模糊查询预约列表（供医生录入结果使用）
    public static List<Appointment> queryAppointmentsByUserName(String name) {
        List<Appointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select a.id, a.user_tel, a.group_id, a.exam_date, a.status, a.create_time, "
                    + "g.name as group_name, u.name as user_name "
                    + "from appointment a "
                    + "left join checkgroup g on a.group_id = g.id "
                    + "left join users u on a.user_tel = u.tel "
                    + "where u.name like ? order by a.exam_date desc, a.id desc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
            r = ps.executeQuery();
            while (r.next()) {
                Appointment a = new Appointment();
                a.setId(r.getInt("id"));
                a.setUserTel(r.getString("user_tel"));
                a.setGroupId(r.getInt("group_id"));
                a.setExamDate(r.getString("exam_date"));
                a.setStatus(r.getString("status"));
                a.setGroupName(r.getString("group_name"));
                a.setUserName(r.getString("user_name"));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //18、取消预约（仅允许取消"已预约"状态，已完成/已取消不可取消）
    public static int cancelAppointment(int appointmentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "update appointment set status='已取消' where id=? and status='已预约'";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //19、更新预约状态
    public static int updateAppointmentStatus(int appointmentId, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "update appointment set status=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //20、查询某预约的检查结果
    public static List<ExamResult> queryExamResultsByAppointment(int appointmentId) {
        List<ExamResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, c.name as item_name "
                    + "from exam_result r left join checkitem c on r.item_id = c.id "
                    + "where r.appointment_id = ? order by c.name";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapExamResult(r);
                er.setItemName(r.getString("item_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //21、保存某预约的检查结果（先清空再写入）
    public static int recordExamResults(int appointmentId, List<ExamResult> results) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("delete from exam_result where appointment_id=?");
            ps.setInt(1, appointmentId);
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("insert into exam_result(appointment_id, item_id, item_value, result_status) values(?,?,?,?)");
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
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    //22、查询某用户某检查项的历次结果（用于历史对比分析）
    public static List<ExamResult> queryExamResultsByUserAndItem(String userTel, int itemId) {
        List<ExamResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "select r.id, r.appointment_id, r.item_id, r.item_value, r.result_status, a.exam_date "
                    + "from exam_result r join appointment a on r.appointment_id = a.id "
                    + "where a.user_tel = ? and r.item_id = ? "
                    + "order by a.exam_date asc, r.id asc";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userTel);
            ps.setInt(2, itemId);
            r = ps.executeQuery();
            while (r.next()) {
                ExamResult er = mapExamResult(r);
                er.setExamDate(r.getString("exam_date"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //23、查询某用户所有历次检查结果（跟踪管理-结果总览）
    public static List<ExamResult> queryExamResultsByUser(String userTel) {
        List<ExamResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
                ExamResult er = mapExamResult(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //24、按患者姓名模糊查询所有历次检查结果（医生-查看患者结果-总览）
    public static List<ExamResult> queryExamResultsByUserName(String name) {
        List<ExamResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
                ExamResult er = mapExamResult(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                er.setUserName(r.getString("user_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    //25、按患者姓名 + 检查项 查询历次结果（医生-查看患者结果-按检查项对比）
    public static List<ExamResult> queryExamResultsByNameAndItem(String name, int itemId) {
        List<ExamResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet r = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
                ExamResult er = mapExamResult(r);
                er.setExamDate(r.getString("exam_date"));
                er.setItemName(r.getString("item_name"));
                er.setGroupName(r.getString("group_name"));
                er.setUserName(r.getString("user_name"));
                list.add(er);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release(conn, ps, r);
        }
        return list;
    }

    private static ExamResult mapExamResult(ResultSet r) throws SQLException {
        ExamResult er = new ExamResult();
        er.setId(r.getInt("id"));
        er.setAppointmentId(r.getInt("appointment_id"));
        er.setItemId(r.getInt("item_id"));
        er.setItemValue(r.getString("item_value"));
        er.setResultStatus(r.getString("result_status"));
        return er;
    }

    private static CheckItem mapCheckItem(ResultSet r) throws SQLException {
        CheckItem item = new CheckItem();
        item.setId(r.getInt("id"));
        item.setName(r.getString("name"));
        item.setCategory(r.getString("category"));
        item.setPrice(r.getBigDecimal("price"));
        item.setDescription(r.getString("description"));
        item.setCreateTime(r.getTimestamp("create_time"));
        return item;
    }

    private static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private static void release(Connection conn, PreparedStatement ps, ResultSet r) {
        try {
            if (r != null) r.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException ignored) {
        }
    }
}
