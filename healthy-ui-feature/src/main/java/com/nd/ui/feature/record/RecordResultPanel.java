package com.nd.ui.feature.record;

import com.nd.common.entity.Appointment;
import com.nd.dao.AppointmentDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 录入结果面板（医生角色）。
 *
 * <p>主界面医生视角的一级导航模块之一，与「检查项管理」「检查组管理」「查看患者结果」并列。
 * 医生输入患者名称，查询该患者的预约记录，选择某次体检后录入检查结果。</p>
 *
 * @author HealthySys 功能界面模块（录入结果）
 */
public class RecordResultPanel extends JPanel {

    /** 所属父窗口 */
    private Window owner;
    /** 预约数据访问对象 */
    private final AppointmentDao appointmentDao = new AppointmentDao();
    /** 患者名称输入框 */
    private JTextField patientNameField;
    /** 预约表格 */
    private JTable appointmentTable;
    /** 预约表格数据模型 */
    private DefaultTableModel tableModel;
    /** 底部提示栏 */
    private JLabel statusLabel;
    /** 查询到的预约列表缓存 */
    private List<Appointment> appointments = new ArrayList<Appointment>();

    /**
     * 构造录入结果面板。
     *
     * @param owner 所属父窗口
     */
    public RecordResultPanel(Window owner) {
        this.owner = owner;
        initUI();
    }

    /**
     * 初始化界面布局：顶部工具栏 + 预约表格 + 底部提示栏，并绑定事件。
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        JLabel title = new JLabel("录入结果");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        top.add(title);
        top.add(Box.createHorizontalStrut(20));

        JLabel tip = new JLabel("患者名称:");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(tip);

        patientNameField = new JTextField(12);
        patientNameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(patientNameField);

        JButton queryBtn = new JButton("查询");
        queryBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryBtn.setBackground(UITheme.PRIMARY);
        queryBtn.setForeground(Color.WHITE);
        queryBtn.setFocusPainted(false);
        top.add(queryBtn);

        JButton recordBtn = new JButton("录入结果");
        recordBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        recordBtn.setBackground(UITheme.WARN);
        recordBtn.setForeground(Color.WHITE);
        recordBtn.setFocusPainted(false);
        top.add(recordBtn);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);

        // 预约表格
        String[] cols = {"预约ID", "患者姓名", "预约日期", "检查组", "状态"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        appointmentTable.setRowHeight(28);
        appointmentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(appointmentTable);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 底部提示栏
        statusLabel = new JLabel("请输入患者名称后点击『查询』，选择某次体检录入结果。");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // 事件监听
        queryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
        patientNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
        recordBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordSelected();
            }
        });
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
    }

    /**
     * 切到本模块时刷新（保留上次查询结果）。
     */
    public void onShow() {
        // 已有查询结果则重新渲染一次
        if (tableModel.getRowCount() > 0 && !appointments.isEmpty()) {
            renderRows(appointments);
        }
    }

    /**
     * 按患者姓名查询其预约列表。
     */
    private void doQuery() {
        String name = patientNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入患者名称！", "提示", JOptionPane.WARNING_MESSAGE);
            patientNameField.requestFocus();
            return;
        }
        try {
            appointments = appointmentDao.queryByUserName(name);
            renderRows(appointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 将预约列表渲染到表格并更新底部统计。
     *
     * @param list 待展示的预约列表
     */
    private void renderRows(List<Appointment> list) {
        tableModel.setRowCount(0);
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getUserName() != null ? a.getUserName() : a.getUserTel(),
                    a.getExamDate(),
                    a.getGroupName(),
                    a.getStatus()
            });
        }
        statusLabel.setText("  共找到 " + list.size() + " 条该患者的体检预约");
    }

    /**
     * 对选中的预约打开录入结果对话框（已取消的预约不可录入）。
     */
    private void recordSelected() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条体检预约！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        if (id == null) {
            return;
        }
        Appointment selected = null;
        for (Appointment a : appointments) {
            if (id.equals(a.getId())) {
                selected = a;
                break;
            }
        }
        if (selected == null) {
            return;
        }
        if ("已取消".equals(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "该预约已取消，不能录入结果！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RecordResultDialog dlg = new RecordResultDialog(owner, selected);
        dlg.setVisible(true);
        // 录入完成后刷新（状态会变为已完成）
        doQuery();
    }
}
