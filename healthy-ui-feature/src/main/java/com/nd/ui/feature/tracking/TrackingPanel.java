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
 * <p><b>所属模块</b>：healthy-ui-feature / tracking（体检追踪）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>使用内层 {@link JTabbedPane} 切换"结果总览"与"按检查项对比"两个页签；</li>
 *   <li>结果总览：一次性展示当前患者全部历史结果，并统计正常/异常条数；</li>
 *   <li>按检查项对比：选择具体检查项后，展示该检查项的历次数值与判定，
 *       并生成文字结论（总次数、正常/异常次数、首次→最近对比）。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckItemDao}（queryAll 加载可选检查项）、
 *       {@link ExamResultDao}（queryByUser / queryByUserAndItem）；</li>
 *   <li>实体：{@link CheckItem}、{@link ExamResult}；</li>
 *   <li>会话：{@link Session#currentTel}，当前登录患者手机号；</li>
 *   <li>UI 基类：{@link JPanel}、{@link JTabbedPane}、{@link JTable}、
 *       {@link JComboBox}、{@link DefaultTableModel} 等；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY}（查询按钮）、{@code UITheme.styleTable}。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（跟踪管理）
 */
public class TrackingPanel extends JPanel {

    /** 所属父窗口（保留引用，当前未直接使用） */
    private Window owner;
    /** 检查项数据访问对象，加载可选检查项列表 */
    private final CheckItemDao checkItemDao = new CheckItemDao();
    /** 检查结果数据访问对象，查询患者历史结果 */
    private final ExamResultDao examResultDao = new ExamResultDao();

    // ===== 结果总览页签控件 =====
    /** 总览表格，展示全部历史结果 */
    private JTable overviewTable;
    /** 总览表格数据模型 */
    private DefaultTableModel overviewModel;
    /** 总览底部统计标签（总条数、正常数、异常数） */
    private JLabel overviewStatusLabel;

    // ===== 按检查项对比页签控件 =====
    /** 检查项下拉框，选择要追踪的检查项 */
    private JComboBox<CheckItem> itemCombo;
    /** 历史结果表格，展示所选检查项的历次数值与判定 */
    private JTable historyTable;
    /** 历史结果表格数据模型 */
    private DefaultTableModel historyModel;
    /** 历史分析结论标签，展示文字统计与趋势对比 */
    private JLabel historyAnalysisLabel;

    /**
     * 构造跟踪管理面板并加载结果总览。
     *
     * @param owner 所属父窗口
     */
    public TrackingPanel(Window owner) {
        this.owner = owner;
        // 构建双 Tab 界面
        initUI();
        // 首次加载结果总览
        loadOverview();
    }

    /**
     * 初始化界面布局：内层双 Tab（结果总览 / 按检查项对比）。
     *
     * <p>使用 {@link JTabbedPane#TOP} 样式，两个页签分别由
     * {@link #buildOverviewTab()} 与 {@link #buildCompareTab()} 构建。</p>
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // 内层 Tab 面板，顶部标签样式
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
     * @return 总览页签面板（BorderLayout 布局）
     */
    private JPanel buildOverviewTab() {
        JPanel p = new JPanel(new BorderLayout());

        // 顶部：标题 + 刷新按钮
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JLabel title = new JLabel("历次检查结果总览");
        title.setFont(new Font("微软雅黑", Font.BOLD, 15));
        top.add(title);
        // 刷新按钮：重新加载总览数据
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH);

        // 中部：结果表格，列：预约日期、检查组、检查项、检测数值、结果
        String[] cols = {"预约日期", "检查组", "检查项", "检测数值", "结果"};
        // 匿名表格模型：重写 isCellEditable 使表格只读
        overviewModel = new DefaultTableModel(cols, 0) {
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
        overviewTable = new JTable(overviewModel);
        overviewTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        overviewTable.setRowHeight(28);
        overviewTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        overviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(overviewTable);
        // 表格放入滚动面板
        JScrollPane scroll = new JScrollPane(overviewTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        p.add(scroll, BorderLayout.CENTER);

        // 底部统计栏
        overviewStatusLabel = new JLabel("  共 0 条结果");
        overviewStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        overviewStatusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(overviewStatusLabel, BorderLayout.SOUTH);

        // 刷新按钮事件：重新加载总览
        refreshBtn.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOverview();
            }
        });

        return p;
    }

    /**
     * 加载当前用户所有历次检查结果并渲染，同时统计正常/异常数量。
     *
     * <p>未登录直接返回；否则调用
     * {@link ExamResultDao#queryByUser(String)} 查询，
     * 逐行渲染到总览表格，并在底部统计正常/异常条数。</p>
     */
    private void loadOverview() {
        // 未登录直接返回
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            return;
        }
        try {
            // 查询当前用户的全部历史结果
            List<ExamResult> list = examResultDao.queryByUser(Session.currentTel);
            // 清空表格现有行
            overviewModel.setRowCount(0);
            // 统计正常/异常计数
            int normal = 0, abnormal = 0;
            for (ExamResult er : list) {
                // 逐行添加：预约日期、检查组、检查项、数值、判定
                overviewModel.addRow(new Object[]{
                        er.getExamDate(), er.getGroupName(), er.getItemName(),
                        er.getItemValue(), er.getResultStatus()
                });
                // 按判定字段累加统计
                if ("正常".equals(er.getResultStatus())) {
                    normal++;
                } else {
                    abnormal++;
                }
            }
            // 更新底部统计标签
            overviewStatusLabel.setText("  共 " + list.size() + " 条结果，其中正常 " + normal + " 条，异常 " + abnormal + " 条");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载结果总览失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== 按检查项对比 ====================

    /**
     * 构建"按检查项对比"页签：检查项选择 + 查询/刷新 + 表格 + 分析结论。
     *
     * @return 对比页签面板（BorderLayout 布局）
     */
    private JPanel buildCompareTab() {
        JPanel p = new JPanel(new BorderLayout());

        // 顶部：标题 + 检查项下拉 + 查询/刷新按钮
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JLabel title = new JLabel("按检查项对比");
        title.setFont(new Font("微软雅黑", Font.BOLD, 15));
        top.add(title);
        JLabel itemLabel = new JLabel("检查项:");
        itemLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(itemLabel);
        // 检查项下拉框
        itemCombo = new JComboBox<CheckItem>();
        itemCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        itemCombo.setPreferredSize(new Dimension(180, 28));
        // 加载全部检查项到下拉框
        loadItems(itemCombo);
        top.add(itemCombo);
        // 查询按钮：主色蓝底白字
        JButton queryBtn = new JButton("查询");
        queryBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryBtn.setBackground(UITheme.PRIMARY);
        queryBtn.setForeground(Color.WHITE);
        queryBtn.setFocusPainted(false);
        top.add(queryBtn);
        // 刷新按钮：重载检查项下拉框
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH);

        // 中部：历史结果表格，列：预约日期、检测数值、结果
        String[] cols = {"预约日期", "检测数值", "结果"};
        // 匿名表格模型：重写 isCellEditable 使表格只读
        historyModel = new DefaultTableModel(cols, 0) {
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
        historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyTable.setRowHeight(28);
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(historyTable);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        p.add(scroll, BorderLayout.CENTER);

        // 底部分析结论标签（初始引导文字）
        historyAnalysisLabel = new JLabel("选择检查项后点击『查询』查看历次结果跟踪对比。");
        historyAnalysisLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyAnalysisLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(historyAnalysisLabel, BorderLayout.SOUTH);

        // 查询按钮事件：按所选检查项查询历史结果
        queryBtn.addActionListener(new ActionListener() {
            /**
             * 响应"查询"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doHistoryQuery();
            }
        });
        // 刷新按钮事件：重载检查项下拉框
        refreshBtn.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
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
     * <p>先 removeAllItems 清空旧选项，再遍历 {@link CheckItemDao#queryAll()}
     * 逐个 addItem。</p>
     *
     * @param combo 待填充的检查项下拉框
     */
    private void loadItems(JComboBox<CheckItem> combo) {
        try {
            // 清空旧选项
            combo.removeAllItems();
            // 遍历 DAO 查询结果，逐个加入下拉框
            for (CheckItem it : checkItemDao.queryAll()) {
                combo.addItem(it);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 按当前检查项查询当前用户历次结果，并生成趋势分析文本。
     *
     * <p>流程：校验登录态与检查项选择 → 调用
     * {@link ExamResultDao#queryByUserAndItem(String, Integer)} 查询 →
     * 渲染历史结果表格并统计正常/异常 → 生成文字结论（总次数、正常/异常次数、
     * 首次→最近数值对比）显示在底部标签。</p>
     */
    private void doHistoryQuery() {
        // 校验登录态
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先登录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 取选中的检查项
        CheckItem it = (CheckItem) itemCombo.getSelectedItem();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "请选择检查项！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // 按当前用户 + 所选检查项查询历史结果
            List<ExamResult> list = examResultDao.queryByUserAndItem(Session.currentTel, it.getId());
            // 清空历史表格
            historyModel.setRowCount(0);
            // 统计正常/异常
            int normal = 0, abnormal = 0;
            for (ExamResult er : list) {
                historyModel.addRow(new Object[]{er.getExamDate(), er.getItemValue(), er.getResultStatus()});
                if ("正常".equals(er.getResultStatus())) {
                    normal++;
                } else {
                    abnormal++;
                }
            }
            // 拼接分析结论文本
            StringBuilder sb = new StringBuilder();
            sb.append("检查项“").append(it.getName()).append("”共跟踪 ")
                    .append(list.size()).append(" 次结果，其中正常 ").append(normal)
                    .append(" 次，异常 ").append(abnormal).append(" 次。");
            // 至少有 2 次结果时，追加首次→最近的数值对比
            if (list.size() >= 2) {
                ExamResult first = list.get(0);
                ExamResult last = list.get(list.size() - 1);
                sb.append("  首次(").append(first.getExamDate()).append(")：").append(first.getItemValue())
                        .append(" → 最近(").append(last.getExamDate()).append(")：").append(last.getItemValue());
            }
            // 将结论文本到底部标签
            historyAnalysisLabel.setText(sb.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     *
     * <p>同时刷新结果总览与检查项下拉框。</p>
     */
    public void onShow() {
        loadOverview();
        loadItems(itemCombo);
    }
}
