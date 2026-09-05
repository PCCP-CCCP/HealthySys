package com.nd.ui.feature.tracking;

import com.nd.common.entity.CheckItem;
import com.nd.common.entity.ExamResult;
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
 * 查看患者结果面板（医生角色）。
 *
 * <p>主界面医生视角的一级导航模块之一，与「检查项管理」「检查组管理」「录入结果」并列。
 * 实现方式与患者角色的「跟踪管理」一致：输入患者名称后：</p>
 * <ol>
 *   <li>结果总览：展示该患者所有历次体检的全部检查结果；</li>
 *   <li>按检查项对比：选择检查项查看历次结果对比与正常/异常统计。</li>
 * </ol>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / tracking（体检追踪）。</p>
 *
 * <p><b>与患者侧 {@link TrackingPanel} 的区别</b>：本面板面向医生，不依赖
 * {@link com.nd.common.util.Session} 当前登录患者，而是通过顶部搜索框
 * 输入任意患者姓名进行查询；表格列额外展示"患者姓名"列。</p>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckItemDao}（queryAll）、
 *       {@link ExamResultDao}（queryByUserName / queryByNameAndItem）；</li>
 *   <li>实体：{@link CheckItem}、{@link ExamResult}；</li>
 *   <li>UI 基类：{@link JPanel}、{@link JTabbedPane}、{@link JTable}、
 *       {@link JComboBox}、{@link DefaultTableModel} 等；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY}（查询按钮）、{@code UITheme.styleTable}。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（跟踪管理）
 */
public class PatientResultPanel extends JPanel {

    /** 所属父窗口（保留引用，当前未直接使用） */
    private Window owner;
    /** 检查项数据访问对象，加载可选检查项列表 */
    private final CheckItemDao checkItemDao = new CheckItemDao();
    /** 检查结果数据访问对象，按患者姓名查询历史结果 */
    private final ExamResultDao examResultDao = new ExamResultDao();

    /** 患者名称输入框（医生输入要查看的患者姓名） */
    private JTextField patientNameField;
    /** 当前查询的患者姓名缓存，刷新/对比查询时复用 */
    private String currentName = "";

    // ===== 结果总览页签控件 =====
    /** 总览表格，展示指定患者的全部历史结果 */
    private JTable overviewTable;
    /** 总览表格数据模型 */
    private DefaultTableModel overviewModel;
    /** 总览底部统计标签（总条数、正常数、异常数） */
    private JLabel overviewStatusLabel;

    // ===== 按检查项对比页签控件 =====
    /** 检查项下拉框 */
    private JComboBox<CheckItem> itemCombo;
    /** 历史结果表格 */
    private JTable historyTable;
    /** 历史结果表格数据模型 */
    private DefaultTableModel historyModel;
    /** 历史分析结论标签 */
    private JLabel historyAnalysisLabel;

    /**
     * 构造查看患者结果面板。
     *
     * @param owner 所属父窗口
     */
    public PatientResultPanel(Window owner) {
        this.owner = owner;
        // 构建顶部搜索栏与双 Tab 界面
        initUI();
    }

    /**
     * 初始化界面布局：顶部患者查询 + 内层双 Tab（结果总览 / 按检查项对比）。
     *
     * <p>整体采用 BorderLayout：NORTH 为患者名称搜索栏，CENTER 为内层
     * JTabbedPane。同时为查询按钮与搜索框注册事件。</p>
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // ============ 北部：患者名称搜索栏 ============
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        // 模块标题"查看患者结果"
        JLabel title = new JLabel("查看患者结果");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        top.add(title);
        // 标题与搜索区之间插入 20 像素水平间隔
        top.add(Box.createHorizontalStrut(20));

        // "患者名称:" 标签
        JLabel tip = new JLabel("患者名称:");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(tip);

        // 患者姓名输入框，列宽 12
        patientNameField = new JTextField(12);
        patientNameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(patientNameField);

        // 查询按钮：主色蓝底白字
        JButton queryBtn = new JButton("查询");
        queryBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        queryBtn.setBackground(UITheme.PRIMARY);
        queryBtn.setForeground(Color.WHITE);
        queryBtn.setFocusPainted(false);
        top.add(queryBtn);

        add(top, BorderLayout.NORTH);

        // ============ 中部：双视图 Tab ============
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tabs.addTab("结果总览", buildOverviewTab());
        tabs.addTab("按检查项对比", buildCompareTab());
        add(tabs, BorderLayout.CENTER);

        // ---- 事件监听 ----
        // 查询按钮：按患者姓名刷新总览与对比
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
        // 搜索框回车也触发查询
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
    }

    // ==================== 结果总览 ====================

    /**
     * 构建"结果总览"页签：标题 + 刷新按钮 + 表格 + 统计栏。
     *
     * @return 总览页签面板（BorderLayout 布局）
     */
    private JPanel buildOverviewTab() {
        JPanel p = new JPanel(new BorderLayout());

        // 顶部：子标题 + 刷新按钮
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JLabel sub = new JLabel("该患者历次检查结果总览");
        sub.setFont(new Font("微软雅黑", Font.BOLD, 15));
        top.add(sub);
        // 刷新按钮：按当前 currentName 重新加载
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(refreshBtn);
        p.add(top, BorderLayout.NORTH);

        // 中部表格：列 患者姓名、预约日期、检查组、检查项、检测数值、结果
        // 注意：比患者侧 TrackingPanel 多了"患者姓名"列，便于医生核对
        String[] cols = {"患者姓名", "预约日期", "检查组", "检查项", "检测数值", "结果"};
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

        // 底部统计标签（初始引导文字）
        overviewStatusLabel = new JLabel("请输入患者名称后点击『查询』。");
        overviewStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        overviewStatusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(overviewStatusLabel, BorderLayout.SOUTH);

        // 刷新按钮事件：按当前 currentName 重新加载总览
        refreshBtn.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOverview(currentName);
            }
        });

        return p;
    }

    /**
     * 加载指定患者所有历次检查结果并渲染，同时统计正常/异常数量。
     *
     * <p>调用 {@link ExamResultDao#queryByUserName(String)} 查询，
     * 逐行渲染到总览表格，并在底部统计正常/异常条数。</p>
     *
     * @param name 患者姓名
     */
    private void loadOverview(String name) {
        // 姓名为空直接返回
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            // 按患者姓名查询其全部历史结果
            List<ExamResult> list = examResultDao.queryByUserName(name.trim());
            // 清空表格现有行
            overviewModel.setRowCount(0);
            // 统计正常/异常计数
            int normal = 0, abnormal = 0;
            for (ExamResult er : list) {
                // 逐行添加：患者姓名、预约日期、检查组、检查项、数值、判定
                overviewModel.addRow(new Object[]{
                        er.getUserName(), er.getExamDate(), er.getGroupName(), er.getItemName(),
                        er.getItemValue(), er.getResultStatus()
                });
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

        // 顶部：子标题 + 检查项下拉 + 查询/刷新按钮
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JLabel sub = new JLabel("按检查项对比");
        sub.setFont(new Font("微软雅黑", Font.BOLD, 15));
        top.add(sub);
        JLabel itemLabel = new JLabel("检查项:");
        itemLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(itemLabel);
        // 检查项下拉框
        itemCombo = new JComboBox<CheckItem>();
        itemCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        itemCombo.setPreferredSize(new Dimension(180, 28));
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

        // 中部历史表格：列 患者姓名、预约日期、检测数值、结果
        String[] cols = {"患者姓名", "预约日期", "检测数值", "结果"};
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
        historyAnalysisLabel = new JLabel("请输入患者名称并选择检查项后点击『查询』。");
        historyAnalysisLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyAnalysisLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(historyAnalysisLabel, BorderLayout.SOUTH);

        // 查询按钮事件：按当前患者 + 所选检查项查询历史
        queryBtn.addActionListener(new ActionListener() {
            /**
             * 响应"查询"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doCompareQuery();
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
     * 顶部查询：按患者名称刷新总览与对比。
     *
     * <p>流程：读取并校验患者姓名 → 缓存到 {@link #currentName} →
     * 调用 {@link #loadOverview(String)} 加载总览 → 调用
     * {@link #doCompareQuery()} 按当前选中检查项刷新对比结果。</p>
     */
    private void doQuery() {
        // 读取患者姓名并去空白
        String name = patientNameField.getText().trim();
        // 姓名必填校验
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入患者名称！", "提示", JOptionPane.WARNING_MESSAGE);
            patientNameField.requestFocus();
            return;
        }
        // 缓存当前患者姓名，供刷新/对比查询复用
        currentName = name;
        // 加载总览
        loadOverview(name);
        // 同时触发对比查询（按当前下拉选中的检查项）
        doCompareQuery();
    }

    /**
     * 按当前患者 + 当前检查项查询历次结果，并生成趋势分析文本。
     *
     * <p>流程：校验 currentName 非空、已选检查项 → 调用
     * {@link ExamResultDao#queryByNameAndItem(String, Integer)} 查询 →
     * 渲染历史表格并统计正常/异常 → 生成文字结论（总次数、正常/异常次数、
     * 首次→最近数值对比）。</p>
     */
    private void doCompareQuery() {
        // 校验已输入并查询过患者姓名
        if (currentName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先输入患者名称并查询！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 取选中的检查项
        CheckItem it = (CheckItem) itemCombo.getSelectedItem();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "请选择检查项！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // 按患者姓名 + 检查项ID查询历史结果
            List<ExamResult> list = examResultDao.queryByNameAndItem(currentName, it.getId());
            // 清空历史表格
            historyModel.setRowCount(0);
            // 统计正常/异常
            int normal = 0, abnormal = 0;
            for (ExamResult er : list) {
                historyModel.addRow(new Object[]{er.getUserName(), er.getExamDate(), er.getItemValue(), er.getResultStatus()});
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
            // 至少 2 次结果时，追加首次→最近数值对比
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
     * 切到本模块时刷新（保留上次查询的患者）。
     *
     * <p>若已有查询过的患者姓名，则重载总览；同时刷新检查项下拉框。</p>
     */
    public void onShow() {
        // 保留上次查询的患者，重新加载总览
        if (!currentName.isEmpty()) {
            loadOverview(currentName);
        }
        // 刷新检查项下拉框
        loadItems(itemCombo);
    }
}
