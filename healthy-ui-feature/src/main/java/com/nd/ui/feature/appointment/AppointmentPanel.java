package com.nd.ui.feature.appointment;

import com.nd.common.entity.Appointment;
import com.nd.common.entity.CheckGroup;
import com.nd.common.util.Session;
import com.nd.dao.AppointmentDao;
import com.nd.dao.CheckGroupDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约面板（患者角色）。
 *
 * <p>主界面患者视角的一级导航模块之一，与「跟踪管理」并列。
 * 提供：预约体检（新建预约）+ 我的预约（查看/取消预约，仅"已预约"状态可取消）。</p>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / appointment（预约管理）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>上半部分为"预约体检"表单：选择检查组 + 选择日期 + 提交预约；</li>
 *   <li>下半部分为"我的预约"列表：展示当前登录患者的全部预约；</li>
 *   <li>支持取消预约（仅"已预约"状态可取消，带二次确认）；</li>
 *   <li>通过 {@link Session#currentTel} 获取当前登录患者手机号作为预约归属。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link AppointmentDao}（create/queryByUser/cancel）、
 *       {@link CheckGroupDao}（queryAll 加载可选检查组）；</li>
 *   <li>实体：{@link Appointment}、{@link CheckGroup}；</li>
 *   <li>会话：{@link Session#currentTel}，当前登录用户手机号；</li>
 *   <li>UI 基类：{@link JPanel}、{@link JTable}、{@link JComboBox}、
 *       {@link JSpinner}、{@link GridBagLayout} 等；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY}（提交按钮）、{@link UITheme#DANGER}
 *       （取消按钮，危险操作色）。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（预约）
 */
public class AppointmentPanel extends JPanel {

    /** 所属父窗口（用于弹出对话框时作为 owner 传参） */
    private Window owner;
    /** 预约数据访问对象，执行预约的创建/查询/取消 */
    private final AppointmentDao appointmentDao = new AppointmentDao();
    /** 检查组数据访问对象，加载可选检查组列表 */
    private final CheckGroupDao checkGroupDao = new CheckGroupDao();

    // ---- 预约体检表单区 ----
    /** 检查组下拉框，泛型为 CheckGroup，渲染时调用 toString 显示组名 */
    private JComboBox<CheckGroup> groupCombo;
    /** 日期选择器，使用 SpinnerDateModel 实现日期输入 */
    private JSpinner dateSpinner;

    // ---- 我的预约列表区 ----
    /** 预约列表展示表格 */
    private JTable appointmentTable;
    /** 预约表格数据模型 */
    private DefaultTableModel appointmentModel;
    /** 当前登录用户的预约列表缓存，取消预约时按 ID 从中匹配 */
    private List<Appointment> appointments = new ArrayList<Appointment>();
    /** 底部预约统计标签 */
    private JLabel appointmentStatusLabel;

    /** 日期格式化器，将 Date 格式化为 "yyyy-MM-dd" 字符串传给 DAO */
    private SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 构造预约面板并加载我的预约。
     *
     * <p>执行流程：保存父窗口引用 → 构建表单与列表界面（{@link #initUI()}）
     * → 首次加载当前用户的预约列表（{@link #loadMyAppointments()}）。</p>
     *
     * @param owner 所属父窗口（通常为主界面 JFrame）
     */
    public AppointmentPanel(Window owner) {
        this.owner = owner;
        // 构建预约体检表单 + 我的预约列表及事件
        initUI();
        // 构造完成后立即加载我的预约
        loadMyAppointments();
    }

    /**
     * 初始化界面布局：预约表单（上）+ 我的预约列表（中），并绑定事件。
     *
     * <p>整体采用 BorderLayout：NORTH 为带标题边框的"预约体检"表单区，
     * CENTER 为带标题边框的"我的预约"列表区。表单区使用 GridBagLayout 排列
     * 检查组下拉、日期选择器与提交按钮。</p>
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // ============ 预约体检（表单区，北部） ============
        // 使用 GridBagLayout 实现表单字段的灵活排列
        JPanel bookPanel = new JPanel(new GridBagLayout());
        // 给表单区加标题边框"预约体检"，标题字体粗体 14 号
        bookPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "预约体检",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14)));
        // 表单区固定高度 150，宽度自适应
        bookPanel.setPreferredSize(new Dimension(0, 150));
        // GridBagConstraints 控制每个组件在网格中的位置与拉伸
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 统一字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // ---- 第一列：检查组标签 ----
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel grpLabel = new JLabel("体检检查组:");
        grpLabel.setFont(labelFont);
        bookPanel.add(grpLabel, gbc);

        // ---- 第二列：检查组下拉框（水平拉伸） ----
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        groupCombo = new JComboBox<CheckGroup>();
        groupCombo.setFont(fieldFont);
        groupCombo.setPreferredSize(new Dimension(260, 30));
        // 从数据库加载全部检查组填充下拉框
        loadGroups(groupCombo);
        bookPanel.add(groupCombo, gbc);

        // ---- 第三列：预约日期标签 ----
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel dateLabel = new JLabel("    预约日期:");
        dateLabel.setFont(labelFont);
        bookPanel.add(dateLabel, gbc);

        // ---- 第四列：日期选择器 ----
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        // 使用 SpinnerDateModel 支持日期上下调节
        dateSpinner = new JSpinner(new SpinnerDateModel());
        // 设置编辑器显示格式为 yyyy-MM-dd
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setFont(fieldFont);
        dateSpinner.setPreferredSize(new Dimension(150, 30));
        bookPanel.add(dateSpinner, gbc);

        // ---- 第五列：提交按钮（靠右对齐） ----
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        // 提交预约按钮：主色蓝底白字粗体
        JButton submitBtn = new JButton("提交预约");
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitBtn.setBackground(UITheme.PRIMARY);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setPreferredSize(new Dimension(110, 34));
        bookPanel.add(submitBtn, gbc);

        // 提交按钮事件：触发表单校验与落库
        submitBtn.addActionListener(new ActionListener() {
            /**
             * 响应"提交预约"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                submitBooking();
            }
        });

        // 表单区整体放到北部
        add(bookPanel, BorderLayout.NORTH);

        // ============ 我的预约（列表区，中部） ============
        JPanel listPanel = new JPanel(new BorderLayout());
        // 加标题边框"我的预约"
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "我的预约",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14)));

        // 列表区顶部小工具栏：取消预约 + 刷新
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        // 取消预约按钮：使用 UITheme.DANGER 红色，突出危险操作
        JButton cancelBtn = new JButton("取消预约");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelBtn.setBackground(UITheme.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        top.add(cancelBtn);
        // 刷新按钮：默认样式
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        listPanel.add(top, BorderLayout.NORTH);

        // 预约表格：列 ID、预约日期、检查组、状态
        String[] cols = {"ID", "预约日期", "检查组", "状态"};
        // 匿名表格模型：重写 isCellEditable 使表格只读
        appointmentModel = new DefaultTableModel(cols, 0) {
            /**
             * 判断单元格是否可编辑。
             *
             * @param row    行索引（未使用）
             * @param column 列索引（未使用）
             * @return 恒为 false，表格只读
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(appointmentModel);
        appointmentTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        appointmentTable.setRowHeight(28);
        appointmentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        // 单选模式
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(appointmentTable);
        // 表格放入滚动面板
        listPanel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        // 底部统计标签
        appointmentStatusLabel = new JLabel("  共 0 条预约");
        appointmentStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        bottom.add(appointmentStatusLabel);
        listPanel.add(bottom, BorderLayout.SOUTH);

        // 列表区整体放到中部
        add(listPanel, BorderLayout.CENTER);

        // ---- 事件监听 ----
        // 取消预约按钮
        cancelBtn.addActionListener(new ActionListener() {
            /**
             * 响应"取消预约"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelected();
            }
        });
        // 刷新按钮：重新加载我的预约
        refreshBtn.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMyAppointments();
            }
        });
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     *
     * <p>同时刷新检查组下拉与我的预约列表，保证数据最新。</p>
     */
    public void onShow() {
        // 重新加载可选检查组
        loadGroups(groupCombo);
        // 重新加载我的预约
        loadMyAppointments();
    }

    /**
     * 加载全部检查组到下拉框。
     *
     * <p>先 removeAllItems 清空旧选项，再遍历 {@link CheckGroupDao#queryAll()}
     * 结果逐个 addItem。异常统一弹窗提示。</p>
     *
     * @param combo 待填充的检查组下拉框
     */
    private void loadGroups(JComboBox<CheckGroup> combo) {
        try {
            // 清空旧选项
            combo.removeAllItems();
            // 遍历 DAO 查询结果，逐个加入下拉框
            for (CheckGroup g : checkGroupDao.queryAll()) {
                combo.addItem(g);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查组失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 提交预约：校验登录态与检查组选择，写入预约记录。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>校验 {@link Session#currentTel} 非空（已登录）；</li>
     *   <li>校验已选择检查组；</li>
     *   <li>将日期选择器的值格式化为 yyyy-MM-dd 字符串；</li>
     *   <li>调用 {@link AppointmentDao#create(String, Integer, String)} 写入预约；</li>
     *   <li>成功后刷新我的预约列表。</li>
     * </ol>
     */
    private void submitBooking() {
        // 校验登录态：Session.currentTel 为空表示未登录
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先登录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 取选中的检查组
        CheckGroup g = (CheckGroup) groupCombo.getSelectedItem();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "请选择检查组！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 将日期选择器的 Date 值格式化为字符串
        String dateStr = dateFmt.format((java.util.Date) dateSpinner.getValue());
        try {
            // 调用 DAO 创建预约：当前用户手机号 + 检查组ID + 日期
            int result = appointmentDao.create(Session.currentTel, g.getId(), dateStr);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "预约成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                // 预约成功后刷新列表
                loadMyAppointments();
            } else {
                JOptionPane.showMessageDialog(this, "预约失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "预约失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载当前用户的预约列表并渲染。
     *
     * <p>未登录时直接返回；否则调用
     * {@link AppointmentDao#queryByUser(String)} 查询，
     * 清空表格后逐行 addRow，并更新底部统计标签。</p>
     */
    private void loadMyAppointments() {
        // 未登录直接返回，避免空指针
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            return;
        }
        try {
            // 按当前用户手机号查询其全部预约
            appointments = appointmentDao.queryByUser(Session.currentTel);
            // 清空表格现有行
            appointmentModel.setRowCount(0);
            for (Appointment a : appointments) {
                // 逐行添加：ID、预约日期、检查组、状态
                appointmentModel.addRow(new Object[]{a.getId(), a.getExamDate(), a.getGroupName(), a.getStatus()});
            }
            // 更新底部统计
            appointmentStatusLabel.setText("  共 " + appointments.size() + " 条预约");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载预约失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 获取当前表格中选中的预约对象。
     *
     * @return 选中的预约；未选择或 ID 不匹配返回 null（未选择时已弹窗提示）
     */
    private Appointment getSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条预约！", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        // 从第一列取出预约 ID
        Integer id = (Integer) appointmentModel.getValueAt(row, 0);
        if (id == null) {
            return null;
        }
        // 在缓存中按 ID 匹配实体
        for (Appointment a : appointments) {
            if (id.equals(a.getId())) {
                return a;
            }
        }
        return null;
    }

    /**
     * 取消选中的预约（仅"已预约"状态可取消，带确认提示）。
     *
     * <p>流程：取选中预约 → 校验状态为"已预约" → showConfirmDialog 二次确认 →
     * 调用 {@link AppointmentDao#cancel(Integer)} 取消 → 刷新列表。</p>
     */
    private void cancelSelected() {
        Appointment a = getSelectedAppointment();
        if (a == null) {
            return;
        }
        // 状态校验：仅"已预约"可取消，"已完成"/"已取消"不可操作
        if (!"已预约".equals(a.getStatus())) {
            JOptionPane.showMessageDialog(this, "仅状态为『已预约』的预约可以取消！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 二次确认对话框，展示日期与检查组信息
        int op = JOptionPane.showConfirmDialog(this,
                "确定取消预约（" + a.getExamDate() + " · " + a.getGroupName() + "）吗？", "取消确认",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            // 调用 DAO 取消预约，返回受影响行数
            int result = appointmentDao.cancel(a.getId());
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "取消成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                // 取消成功后刷新列表
                loadMyAppointments();
            } else {
                // 返回 0 表示预约状态已变化（可能被他人取消或已完成）
                JOptionPane.showMessageDialog(this, "取消失败，预约状态可能已变化，请刷新后重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "取消失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
