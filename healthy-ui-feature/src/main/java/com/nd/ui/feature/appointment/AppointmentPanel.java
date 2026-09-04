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
 * @author HealthySys 功能界面模块（预约）
 */
public class AppointmentPanel extends JPanel {

    /** 所属父窗口 */
    private Window owner;
    /** 预约数据访问对象 */
    private final AppointmentDao appointmentDao = new AppointmentDao();
    /** 检查组数据访问对象 */
    private final CheckGroupDao checkGroupDao = new CheckGroupDao();

    // 预约体检表单
    /** 检查组下拉框 */
    private JComboBox<CheckGroup> groupCombo;
    /** 日期选择器 */
    private JSpinner dateSpinner;

    // 我的预约列表
    /** 预约表格 */
    private JTable appointmentTable;
    /** 预约表格数据模型 */
    private DefaultTableModel appointmentModel;
    /** 当前用户预约列表缓存 */
    private List<Appointment> appointments = new ArrayList<Appointment>();
    /** 底部预约统计标签 */
    private JLabel appointmentStatusLabel;

    /** 日期格式化器 */
    private SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 构造预约面板并加载我的预约。
     *
     * @param owner 所属父窗口
     */
    public AppointmentPanel(Window owner) {
        this.owner = owner;
        initUI();
        loadMyAppointments();
    }

    /**
     * 初始化界面布局：预约表单（上）+ 我的预约列表（中），并绑定事件。
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // ============ 预约体检（表单） ============
        JPanel bookPanel = new JPanel(new GridBagLayout());
        bookPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "预约体检",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14)));
        bookPanel.setPreferredSize(new Dimension(0, 150));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 检查组
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel grpLabel = new JLabel("体检检查组:");
        grpLabel.setFont(labelFont);
        bookPanel.add(grpLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        groupCombo = new JComboBox<CheckGroup>();
        groupCombo.setFont(fieldFont);
        groupCombo.setPreferredSize(new Dimension(260, 30));
        loadGroups(groupCombo);
        bookPanel.add(groupCombo, gbc);

        // 预约日期
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel dateLabel = new JLabel("    预约日期:");
        dateLabel.setFont(labelFont);
        bookPanel.add(dateLabel, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setFont(fieldFont);
        dateSpinner.setPreferredSize(new Dimension(150, 30));
        bookPanel.add(dateSpinner, gbc);

        // 提交按钮
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JButton submitBtn = new JButton("提交预约");
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitBtn.setBackground(UITheme.PRIMARY);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setPreferredSize(new Dimension(110, 34));
        bookPanel.add(submitBtn, gbc);

        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitBooking();
            }
        });

        add(bookPanel, BorderLayout.NORTH);

        // ============ 我的预约（列表） ============
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "我的预约",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14)));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JButton cancelBtn = new JButton("取消预约");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelBtn.setBackground(UITheme.DANGER);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        top.add(cancelBtn);
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        listPanel.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "预约日期", "检查组", "状态"};
        appointmentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(appointmentModel);
        appointmentTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        appointmentTable.setRowHeight(28);
        appointmentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(appointmentTable);
        listPanel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        appointmentStatusLabel = new JLabel("  共 0 条预约");
        appointmentStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        bottom.add(appointmentStatusLabel);
        listPanel.add(bottom, BorderLayout.SOUTH);

        add(listPanel, BorderLayout.CENTER);

        // 事件监听
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelected();
            }
        });
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMyAppointments();
            }
        });
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     */
    public void onShow() {
        loadGroups(groupCombo);
        loadMyAppointments();
    }

    /**
     * 加载全部检查组到下拉框。
     *
     * @param combo 检查组下拉框
     */
    private void loadGroups(JComboBox<CheckGroup> combo) {
        try {
            combo.removeAllItems();
            for (CheckGroup g : checkGroupDao.queryAll()) {
                combo.addItem(g);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查组失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 提交预约：校验登录态与检查组选择，写入预约记录。
     */
    private void submitBooking() {
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先登录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CheckGroup g = (CheckGroup) groupCombo.getSelectedItem();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "请选择检查组！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String dateStr = dateFmt.format((java.util.Date) dateSpinner.getValue());
        try {
            int result = appointmentDao.create(Session.currentTel, g.getId(), dateStr);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "预约成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
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
     */
    private void loadMyAppointments() {
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            return;
        }
        try {
            appointments = appointmentDao.queryByUser(Session.currentTel);
            appointmentModel.setRowCount(0);
            for (Appointment a : appointments) {
                appointmentModel.addRow(new Object[]{a.getId(), a.getExamDate(), a.getGroupName(), a.getStatus()});
            }
            appointmentStatusLabel.setText("  共 " + appointments.size() + " 条预约");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载预约失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 获取当前表格中选中的预约对象。
     *
     * @return 选中的预约；未选择或异常返回 null
     */
    private Appointment getSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条预约！", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Integer id = (Integer) appointmentModel.getValueAt(row, 0);
        if (id == null) {
            return null;
        }
        for (Appointment a : appointments) {
            if (id.equals(a.getId())) {
                return a;
            }
        }
        return null;
    }

    /**
     * 取消选中的预约（仅"已预约"状态可取消，带确认提示）。
     */
    private void cancelSelected() {
        Appointment a = getSelectedAppointment();
        if (a == null) {
            return;
        }
        if (!"已预约".equals(a.getStatus())) {
            JOptionPane.showMessageDialog(this, "仅状态为『已预约』的预约可以取消！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "确定取消预约（" + a.getExamDate() + " · " + a.getGroupName() + "）吗？", "取消确认",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int result = appointmentDao.cancel(a.getId());
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "取消成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadMyAppointments();
            } else {
                JOptionPane.showMessageDialog(this, "取消失败，预约状态可能已变化，请刷新后重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "取消失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
