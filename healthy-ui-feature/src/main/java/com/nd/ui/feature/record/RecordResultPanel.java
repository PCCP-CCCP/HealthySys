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
 * <p><b>所属模块</b>：healthy-ui-feature / record（体检结果录入）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>按患者姓名查询其全部体检预约；</li>
 *   <li>以表格展示预约（ID、患者姓名、预约日期、检查组、状态）；</li>
 *   <li>选中某次预约后打开 {@link RecordResultDialog} 录入/编辑检查结果；</li>
 *   <li>已取消的预约禁止录入结果；</li>
 *   <li>录入完成后自动刷新查询，预约状态变为"已完成"。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link AppointmentDao}，提供 queryByUserName 方法；</li>
 *   <li>实体：{@link Appointment}，承载预约数据；</li>
 *   <li>UI 基类：{@link JPanel}、{@link JTable}、{@link DefaultTableModel}、
 *       {@link JOptionPane} 等 Swing 控件；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY}（查询按钮）、{@link UITheme#WARN}
 *       （录入按钮，警示色）、{@code UITheme.styleTable}；</li>
 *   <li>对话框：{@link RecordResultDialog}，结果录入/编辑弹窗。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（录入结果）
 */
public class RecordResultPanel extends JPanel {

    /** 所属父窗口（用于弹出对话框时作为 owner 传参） */
    private Window owner;
    /** 预约数据访问对象，提供按用户名查询预约的方法 */
    private final AppointmentDao appointmentDao = new AppointmentDao();
    /** 患者名称输入框（必填，支持回车查询） */
    private JTextField patientNameField;
    /** 预约列表展示表格 */
    private JTable appointmentTable;
    /** 表格数据模型 */
    private DefaultTableModel tableModel;
    /** 底部提示栏，展示查询结果数量与操作引导 */
    private JLabel statusLabel;
    /** 查询到的预约列表缓存，recordSelected 时按 ID 从中匹配选中项 */
    private List<Appointment> appointments = new ArrayList<Appointment>();

    /**
     * 构造录入结果面板。
     *
     * <p>执行流程：保存父窗口引用 → 调用 {@link #initUI()} 构建界面与事件。
     * 注意：本面板构造时不自动查询数据，需医生输入患者姓名后手动触发查询。</p>
     *
     * @param owner 所属父窗口（通常为主界面 JFrame）
     */
    public RecordResultPanel(Window owner) {
        this.owner = owner;
        // 构建顶部工具栏、预约表格、底部提示栏及事件监听
        initUI();
    }

    /**
     * 初始化界面布局：顶部工具栏 + 预约表格 + 底部提示栏，并绑定事件。
     *
     * <p>布局采用 {@link BorderLayout}：NORTH 顶部工具栏（标题+患者名输入+查询/录入/刷新按钮），
     * CENTER 带滚动条的预约表格，SOUTH 底部提示栏。</p>
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // ---- 顶部工具栏 ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        // 模块标题"录入结果"
        JLabel title = new JLabel("录入结果");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        top.add(title);
        // 标题与后续控件间插入 20 像素水平间隔
        top.add(Box.createHorizontalStrut(20));

        // "患者名称:" 标签
        JLabel tip = new JLabel("患者名称:");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(tip);

        // 患者姓名输入框，列宽 12
        patientNameField = new JTextField(12);
        patientNameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(patientNameField);

        // "查询"按钮：主色蓝底白字
        JButton queryBtn = new JButton("查询");
        queryBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryBtn.setBackground(UITheme.PRIMARY);
        queryBtn.setForeground(Color.WHITE);
        queryBtn.setFocusPainted(false);
        top.add(queryBtn);

        // "录入结果"按钮：使用 UITheme.WARN 警示色（橙色），突出"录入"这一重要操作
        JButton recordBtn = new JButton("录入结果");
        recordBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        recordBtn.setBackground(UITheme.WARN);
        recordBtn.setForeground(Color.WHITE);
        recordBtn.setFocusPainted(false);
        top.add(recordBtn);

        // "刷新"按钮：默认样式
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);

        // ---- 中部预约表格 ----
        // 列：预约ID、患者姓名、预约日期、检查组、状态
        String[] cols = {"预约ID", "患者姓名", "预约日期", "检查组", "状态"};
        // 匿名表格模型：重写 isCellEditable 使表格只读
        tableModel = new DefaultTableModel(cols, 0) {
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
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        appointmentTable.setRowHeight(28);
        appointmentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        // 单选模式
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 统一表格样式
        UITheme.styleTable(appointmentTable);

        // 表格放入滚动面板
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // ---- 底部提示栏 ----
        // 初始引导文字，后续由 renderRows 更新为查询结果数量
        statusLabel = new JLabel("请输入患者名称后点击『查询』，选择某次体检录入结果。");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // ---- 事件监听 ----
        // 查询按钮：按患者姓名查询预约
        queryBtn.addActionListener(new ActionListener() {
            /**
             * 响应"查询"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
        // 患者姓名框回车也触发查询
        patientNameField.addActionListener(new ActionListener() {
            /**
             * 响应患者姓名输入框回车事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
        // 录入结果按钮：对选中预约打开录入对话框
        recordBtn.addActionListener(new ActionListener() {
            /**
             * 响应"录入结果"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                recordSelected();
            }
        });
        // 刷新按钮：重新执行查询（按当前输入的患者名）
        refreshBtn.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
    }

    /**
     * 切到本模块时刷新（保留上次查询结果）。
     *
     * <p>主界面 Tab 切换回调。若当前表格已有数据且缓存非空，则重新渲染一次，
     * 以保证从录入对话框返回后状态已同步。</p>
     */
    public void onShow() {
        // 已有查询结果则重新渲染一次
        if (tableModel.getRowCount() > 0 && !appointments.isEmpty()) {
            renderRows(appointments);
        }
    }

    /**
     * 按患者姓名查询其预约列表。
     *
     * <p>流程：读取并校验患者姓名 → 调用
     * {@link AppointmentDao#queryByUserName(String)} 查询 →
     * 缓存结果到 {@link #appointments} 并调用 {@link #renderRows(List)} 渲染。
     * 异常统一弹窗提示。</p>
     */
    private void doQuery() {
        // 读取患者姓名并去除首尾空白
        String name = patientNameField.getText().trim();
        // 姓名必填校验
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入患者名称！", "提示", JOptionPane.WARNING_MESSAGE);
            patientNameField.requestFocus();
            return;
        }
        try {
            // DAO 按用户名模糊/精确查询预约列表
            appointments = appointmentDao.queryByUserName(name);
            renderRows(appointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 将预约列表渲染到表格并更新底部统计。
     *
     * <p>先 setRowCount(0) 清空表格，再逐行 addRow。
     * 患者姓名列优先显示 userName，为空时回退到 userTel（电话）。</p>
     *
     * @param list 待展示的预约列表
     */
    private void renderRows(List<Appointment> list) {
        // 清空现有所有行
        tableModel.setRowCount(0);
        for (Appointment a : list) {
            // 按列顺序组装一行；患者姓名做 null 保护，空时用电话兜底
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getUserName() != null ? a.getUserName() : a.getUserTel(),
                    a.getExamDate(),
                    a.getGroupName(),
                    a.getStatus()
            });
        }
        // 更新底部状态栏，显示本次查询到的预约条数
        statusLabel.setText("  共找到 " + list.size() + " 条该患者的体检预约");
    }

    /**
     * 对选中的预约打开录入结果对话框（已取消的预约不可录入）。
     *
     * <p>流程：取选中行 → 从表格第一列取 ID → 在 {@link #appointments} 中匹配实体
     * → 校验状态不为"已取消" → 构造 {@link RecordResultDialog} 并模态显示
     * → 关闭后调用 {@link #doQuery()} 刷新（状态会变为已完成）。</p>
     */
    private void recordSelected() {
        // 取当前选中行索引，-1 表示未选中
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条体检预约！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 从第一列取出预约 ID
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        if (id == null) {
            return;
        }
        // 在缓存的预约列表中按 ID 匹配出完整实体
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
        // 状态校验：已取消的预约不允许录入结果
        if ("已取消".equals(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "该预约已取消，不能录入结果！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 以选中预约为上下文打开录入对话框
        RecordResultDialog dlg = new RecordResultDialog(owner, selected);
        // 模态显示，阻塞直到对话框关闭
        dlg.setVisible(true);
        // 录入完成后刷新（状态会变为已完成）
        doQuery();
    }
}
