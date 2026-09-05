package com.nd.ui.feature.checkgroup;

import com.nd.common.entity.CheckGroup;
import com.nd.common.entity.CheckItem;
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
 * 检查组管理面板（医生角色）。
 *
 * <p>主界面医生视角的一级导航模块之一，与「检查项管理」「录入结果」「查看患者结果」并列。
 * 提供：查询、创建、修改、删除检查组，底部显示检查组数量。</p>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / checkgroup（检查组管理）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>以表格形式展示全部检查组（ID、名称、组内检查项数、创建时间）；</li>
 *   <li>支持按检查组名称或组内检查项名称关键字搜索；</li>
 *   <li>通过 {@link CheckGroupEditDialog} 完成检查组的新增与编辑；</li>
 *   <li>对选中检查组进行删除（带二次确认）；</li>
 *   <li>底部状态栏实时显示检查组总数。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckGroupDao}，提供 queryAll / queryGroupItems / delete 等方法；</li>
 *   <li>实体：{@link CheckGroup}、{@link CheckItem}（组内检查项）；</li>
 *   <li>UI 基类：{@link JPanel}、{@link JTable}、{@link DefaultTableModel}、
 *       {@link JOptionPane} 等 Swing 标准控件；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY} 主色与 {@code UITheme.styleTable(JTable)} 表格样式；</li>
 *   <li>对话框：{@link CheckGroupEditDialog}，新增/编辑检查组的弹出窗口。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（检查组管理）
 */
public class CheckGroupManagePanel extends JPanel {

    /** 所属父窗口（用于弹出对话框时作为 owner 传参） */
    private Window owner;
    /** 检查组数据访问对象，封装对 check_group 表及关联关系的 SQL 调用 */
    private final CheckGroupDao checkGroupDao = new CheckGroupDao();
    /** 检查组列表展示表格 */
    private JTable groupTable;
    /** 表格数据模型，维护列名与行数据 */
    private DefaultTableModel tableModel;
    /** 关键字搜索框（支持回车触发查询） */
    private JTextField searchField;
    /** 「查询」按钮 */
    private JButton queryButton;
    /** 「创建检查组」按钮（主色按钮） */
    private JButton createButton;
    /** 「修改」按钮 */
    private JButton editButton;
    /** 「删除」按钮 */
    private JButton deleteButton;
    /** 「刷新」按钮 */
    private JButton refreshButton;
    /** 底部状态栏，展示检查组总数 */
    private JLabel statusLabel;
    /** 日期格式化器，将 Timestamp 格式化为 "yyyy-MM-dd HH:mm:ss" */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /** 全量检查组缓存（用于本地过滤），loadGroups 时从 DAO 拉取后暂存 */
    private List<CheckGroup> allGroups = new ArrayList<CheckGroup>();

    /**
     * 构造检查组管理面板并加载数据。
     *
     * <p>执行流程：保存父窗口引用 → 构建界面与事件绑定（{@link #initUI()}）
     * → 首次从数据库加载检查组数据（{@link #loadGroups()}）。</p>
     *
     * @param owner 所属父窗口（通常为主界面 JFrame）
     */
    public CheckGroupManagePanel(Window owner) {
        this.owner = owner;
        // 初始化顶部工具栏、表格、底部状态栏及事件监听
        initUI();
        // 首次加载数据，避免面板空白
        loadGroups();
    }

    /**
     * 初始化界面布局：顶部工具栏 + 表格 + 底部状态栏，并绑定事件。
     *
     * <p>布局采用 {@link BorderLayout}：NORTH 顶部工具栏，CENTER 带滚动条表格，SOUTH 底部状态栏。</p>
     */
    private void initUI() {
        // 整体 BorderLayout 布局
        setLayout(new BorderLayout());

        // ---- 顶部工具栏 ----
        // FlowLayout.LEFT 左对齐排列，水平间距 8、垂直间距 10
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        // 模块标题"检查组管理"，微软雅黑粗体 18 号
        JLabel title = new JLabel("检查组管理");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        top.add(title);
        // 标题与搜索框之间插入 20 像素水平间隔
        top.add(Box.createHorizontalStrut(20));

        // 关键字搜索框，列宽 12
        searchField = new JTextField(12);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(searchField);

        // 查询按钮：默认浅灰样式
        queryButton = buildToolButton("查询", null);
        top.add(queryButton);

        // 创建检查组按钮：主色蓝底白字（主操作）
        createButton = buildToolButton("创建检查组", UITheme.PRIMARY);
        top.add(createButton);

        // 修改按钮：默认浅灰
        editButton = buildToolButton("修改", null);
        top.add(editButton);

        // 删除按钮：默认浅灰
        deleteButton = buildToolButton("删除", null);
        top.add(deleteButton);

        // 刷新按钮：默认浅灰
        refreshButton = buildToolButton("刷新", null);
        top.add(refreshButton);

        add(top, BorderLayout.NORTH);

        // ---- 中部数据表格 ----
        // 列：ID、检查组名称、组内检查项数、创建时间
        String[] columns = {"ID", "检查组名称", "组内检查项数", "创建时间"};
        // 构建表格模型，匿名重写 isCellEditable 使表格只读
        tableModel = new DefaultTableModel(columns, 0) {
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
        groupTable = new JTable(tableModel);
        groupTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        groupTable.setRowHeight(28);
        groupTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        // 单选模式，保证"修改/删除"只能针对一个检查组
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 调用 UITheme 统一表格样式
        UITheme.styleTable(groupTable);

        // 表格放入滚动面板
        JScrollPane scrollPane = new JScrollPane(groupTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // ---- 底部状态栏 ----
        statusLabel = new JLabel("  共 0 个检查组");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);

        // ---- 事件监听 ----
        // 查询按钮：触发本地过滤
        queryButton.addActionListener(new ActionListener() {
            /**
             * 响应"查询"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        // 搜索框回车触发查询
        searchField.addActionListener(new ActionListener() {
            /**
             * 响应搜索框回车事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        // 创建按钮：打开新增对话框
        createButton.addActionListener(new ActionListener() {
            /**
             * 响应"创建检查组"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateDialog();
            }
        });
        // 修改按钮：打开编辑对话框
        editButton.addActionListener(new ActionListener() {
            /**
             * 响应"修改"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelected();
            }
        });
        // 删除按钮：删除选中检查组
        deleteButton.addActionListener(new ActionListener() {
            /**
             * 响应"删除"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        // 刷新按钮：重新加载数据
        refreshButton.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                loadGroups();
            }
        });
    }

    /**
     * 统一的工具栏按钮样式工厂方法。
     *
     * @param text 按钮文字
     * @param bg   背景色；为 null 时使用默认浅灰，非 null 时使用蓝底白字主样式
     * @return 样式化按钮
     */
    private JButton buildToolButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        if (bg != null) {
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }
        return b;
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     */
    public void onShow() {
        loadGroups();
    }

    /**
     * 加载全部检查组并渲染到表格。
     *
     * <p>调用 {@link CheckGroupDao#queryAll()} 拉取全部检查组，
     * 暂存到 {@link #allGroups} 后调用 {@link #renderGroups(List)} 渲染。
     * 异常统一弹窗提示。</p>
     */
    private void loadGroups() {
        try {
            // DAO 查询全部检查组（含 itemCount 字段）
            allGroups = checkGroupDao.queryAll();
            renderGroups(allGroups);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 将检查组列表渲染到表格并更新底部统计。
     *
     * <p>先 setRowCount(0) 清空表格，再逐行 addRow。</p>
     *
     * @param list 待展示的检查组列表（全量或过滤后的子集）
     */
    private void renderGroups(List<CheckGroup> list) {
        // 清空现有所有行
        tableModel.setRowCount(0);
        for (CheckGroup g : list) {
            // 按列顺序组装一行数据；创建时间判空后格式化
            Object[] row = new Object[]{
                    g.getId(),
                    g.getName(),
                    g.getItemCount(),
                    g.getCreateTime() != null ? sdf.format(g.getCreateTime()) : ""
            };
            tableModel.addRow(row);
        }
        // 更新底部状态栏
        statusLabel.setText("  共 " + list.size() + " 个检查组");
    }

    /**
     * 查询：按检查组名称或组内检查项名称匹配。
     *
     * <p>过滤逻辑：</p>
     * <ol>
     *   <li>关键字为空：直接渲染全量；</li>
     *   <li>先检查组名称模糊匹配；</li>
     *   <li>若名称未命中，再调用 {@link CheckGroupDao#queryGroupItems(Integer)}
     *       查询组内检查项列表，逐个匹配检查项名称；</li>
     *   <li>任一命中即保留该检查组。组内查询异常被忽略，不影响整体过滤。</li>
     * </ol>
     */
    private void doSearch() {
        String kw = searchField.getText().trim();
        // 关键字为空：渲染全量，相当于清除搜索
        if (kw.isEmpty()) {
            renderGroups(allGroups);
            return;
        }
        // 统一小写，实现不区分大小写匹配
        String lower = kw.toLowerCase();
        List<CheckGroup> filtered = new ArrayList<CheckGroup>();
        for (CheckGroup g : allGroups) {
            // 第一优先级：检查组名称是否包含关键字
            boolean hit = g.getName() != null && g.getName().toLowerCase().contains(lower);
            // 名称未命中：再查组内检查项名称
            if (!hit) {
                try {
                    // 查询该检查组关联的所有检查项
                    List<CheckItem> items = checkGroupDao.queryGroupItems(g.getId());
                    for (CheckItem it : items) {
                        // 任一检查项名称命中即保留该组
                        if (it.getName() != null && it.getName().toLowerCase().contains(lower)) {
                            hit = true;
                            break;
                        }
                    }
                } catch (Exception ignored) {
                    // 组内查询失败不影响整体过滤结果，静默忽略
                }
            }
            if (hit) {
                filtered.add(g);
            }
        }
        renderGroups(filtered);
    }

    /**
     * 获取当前表格中选中的检查组对象。
     *
     * @return 选中的检查组；未选择或 ID 不匹配返回 null（未选择时已弹窗提示）
     */
    private CheckGroup getSelectedGroup() {
        int row = groupTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个检查组！", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        // 从第一列取出 ID
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        if (id == null) {
            return null;
        }
        // 在全量缓存中按 ID 匹配实体
        for (CheckGroup g : allGroups) {
            if (id.equals(g.getId())) {
                return g;
            }
        }
        return null;
    }

    /**
     * 打开创建检查组对话框（关闭后刷新列表）。
     *
     * <p>注意：此处通过 {@code setVisible(true)} 阻塞等待对话框关闭，关闭后立即
     * {@code loadGroups()} 刷新表格，不依赖回调。</p>
     */
    private void openCreateDialog() {
        // 新建模式：待编辑对象传 null
        CheckGroupEditDialog dlg = new CheckGroupEditDialog(owner, null);
        // 模态显示，阻塞直到对话框关闭
        dlg.setVisible(true);
        // 对话框关闭后重新加载列表（无论是否保存都刷新，保证数据最新）
        loadGroups();
    }

    /**
     * 修改选中的检查组（打开编辑对话框）。
     */
    private void editSelected() {
        CheckGroup g = getSelectedGroup();
        if (g == null) {
            return;
        }
        // 以现有检查组为初始数据打开编辑对话框
        CheckGroupEditDialog dlg = new CheckGroupEditDialog(owner, g);
        dlg.setVisible(true);
        // 关闭后刷新列表
        loadGroups();
    }

    /**
     * 删除选中的检查组（带确认提示）。
     */
    private void deleteSelected() {
        CheckGroup g = getSelectedGroup();
        if (g == null) {
            return;
        }
        // 二次确认对话框
        int op = JOptionPane.showConfirmDialog(this,
                "确定删除检查组“" + g.getName() + "”吗？", "删除确认",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            // 调用 DAO 按 ID 删除
            int result = checkGroupDao.delete(g.getId());
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadGroups();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
