package com.nd.ui.feature.tracking;

import com.nd.common.entity.CheckItem;
import com.nd.common.entity.ExamResult;
import com.nd.common.util.Session;
import com.nd.dao.CheckItemDao;
import com.nd.dao.ExamResultDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 跟踪管理面板（患者角色）。
 *
 * <p>主界面患者视角的一级导航模块之一，与「预约」并列。
 * 提供两个视图：</p>
 * <ol>
 *   <li>结果总览：展示该患者所有历次体检的全部检查结果；</li>
 *   <li>按检查项对比：选择检查项查看历次结果对比与正常/异常统计。</li>
 * </ol>
 *
 * @author HealthySys 功能界面模块（跟踪管理）
 */
public class TrackingPanel extends JPanel {

    /** 所属父窗口 */
    private Window owner;
    /** 检查项数据访问对象 */
    private final CheckItemDao checkItemDao = new CheckItemDao();
    /** 检查结果数据访问对象 */
    private final ExamResultDao examResultDao = new ExamResultDao();

    // ===== 结果总览 =====
    /** 总览表格 */
    private JTable overviewTable;
    /** 总览表格数据模型 */
    private DefaultTableModel overviewModel;
    /** 总览底部统计标签 */
    private JLabel overviewStatusLabel;

    // ===== 按检查项对比 =====
    /** 检查项下拉框 */
    private JComboBox<CheckItem> itemCombo;
    /** 历史结果表格 */
    private JTable historyTable;
    /** 历史结果表格数据模型 */
    private DefaultTableModel historyModel;
    /** 历史分析结论标签 */
    private JLabel historyAnalysisLabel;

    /**
     * 构造跟踪管理面板并加载结果总览。
     *
     * @param owner 所属父窗口
     */
    public TrackingPanel(Window owner) {
        this.owner = owner;
        initUI();
        loadOverview();
    }

    /**
     * 初始化界面布局：内层双 Tab（结果总览 / 按检查项对比）。
     */
    private void initUI() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tabs.addTab("结果总览", buildOverviewTab());
        tabs.addTab("按检查项对比", buildCompareTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ==================== 结果总览 ====================

    /**
     * 构建"结果总览"页签：标题 + 刷新按钮 + 表格 + 统计栏。
     *
     * @return 总览页签面板
     */
    private JPanel buildOverviewTab() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JLabel title = new JLabel("历次检查结果总览");
        title.setFont(new Font("微软雅黑", Font.BOLD, 15));
        top.add(title);
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"预约日期", "检查组", "检查项", "检测数值", "结果"};
        overviewModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        overviewTable = new JTable(overviewModel);
        overviewTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        overviewTable.setRowHeight(28);
        overviewTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        overviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(overviewTable);
        JScrollPane scroll = new JScrollPane(overviewTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        p.add(scroll, BorderLayout.CENTER);

        overviewStatusLabel = new JLabel("  共 0 条结果");
        overviewStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        overviewStatusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(overviewStatusLabel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOverview();
            }
        });

        return p;
    }

    /**
     * 加载当前用户所有历次检查结果并渲染，同时统计正常/异常数量。
     */
    private void loadOverview() {
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            return;
        }
        try {
            List<ExamResult> list = examResultDao.queryByUser(Session.currentTel);
            overviewModel.setRowCount(0);
            int normal = 0, abnormal = 0;
            for (ExamResult er : list) {
                overviewModel.addRow(new Object[]{
                        er.getExamDate(), er.getGroupName(), er.getItemName(),
                        er.getItemValue(), er.getResultStatus()
                });
                if ("正常".equals(er.getResultStatus())) {
                    normal++;
                } else {
                    abnormal++;
                }
            }
            overviewStatusLabel.setText("  共 " + list.size() + " 条结果，其中正常 " + normal + " 条，异常 " + abnormal + " 条");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载结果总览失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== 按检查项对比 ====================

    /**
     * 构建"按检查项对比"页签：检查项选择 + 查询/刷新 + 表格 + 分析结论。
     *
     * @return 对比页签面板
     */
    private JPanel buildCompareTab() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JLabel title = new JLabel("按检查项对比");
        title.setFont(new Font("微软雅黑", Font.BOLD, 15));
        top.add(title);
        JLabel itemLabel = new JLabel("检查项:");
        itemLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(itemLabel);
        itemCombo = new JComboBox<CheckItem>();
        itemCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        itemCombo.setPreferredSize(new Dimension(180, 28));
        loadItems(itemCombo);
        top.add(itemCombo);
        JButton queryBtn = new JButton("查询");
        queryBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryBtn.setBackground(UITheme.PRIMARY);
        queryBtn.setForeground(Color.WHITE);
        queryBtn.setFocusPainted(false);
        top.add(queryBtn);
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"预约日期", "检测数值", "结果"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyTable.setRowHeight(28);
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(historyTable);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        p.add(scroll, BorderLayout.CENTER);

        historyAnalysisLabel = new JLabel("选择检查项后点击『查询』查看历次结果跟踪对比。");
        historyAnalysisLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyAnalysisLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(historyAnalysisLabel, BorderLayout.SOUTH);

        queryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doHistoryQuery();
            }
        });
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadItems(itemCombo);
            }
        });

        return p;
    }

    /**
     * 加载全部检查项到下拉框。
     *
     * @param combo 检查项下拉框
     */
    private void loadItems(JComboBox<CheckItem> combo) {
        try {
            combo.removeAllItems();
            for (CheckItem it : checkItemDao.queryAll()) {
                combo.addItem(it);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 按当前检查项查询当前用户历次结果，并生成趋势分析文本。
     */
    private void doHistoryQuery() {
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先登录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CheckItem it = (CheckItem) itemCombo.getSelectedItem();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "请选择检查项！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<ExamResult> list = examResultDao.queryByUserAndItem(Session.currentTel, it.getId());
            historyModel.setRowCount(0);
            int normal = 0, abnormal = 0;
            for (ExamResult er : list) {
                historyModel.addRow(new Object[]{er.getExamDate(), er.getItemValue(), er.getResultStatus()});
                if ("正常".equals(er.getResultStatus())) {
                    normal++;
                } else {
                    abnormal++;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("检查项“").append(it.getName()).append("”共跟踪 ")
                    .append(list.size()).append(" 次结果，其中正常 ").append(normal)
                    .append(" 次，异常 ").append(abnormal).append(" 次。");
            if (list.size() >= 2) {
                ExamResult first = list.get(0);
                ExamResult last = list.get(list.size() - 1);
                sb.append("  首次(").append(first.getExamDate()).append(")：").append(first.getItemValue())
                        .append(" → 最近(").append(last.getExamDate()).append(")：").append(last.getItemValue());
            }
            historyAnalysisLabel.setText(sb.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     */
    public void onShow() {
        loadOverview();
        loadItems(itemCombo);
    }
}
