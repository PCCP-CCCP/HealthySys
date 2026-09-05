package com.nd.ui.feature.checkitem;

import com.nd.common.entity.CheckItem;
import com.nd.dao.CheckItemDao;
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
 * 检查项管理面板（医生角色）。
 *
 * <p>主界面医生视角的一级导航模块之一，与「检查组管理」「录入结果」「查看患者结果」并列。
 * 提供：查询、创建、编辑、删除检查项，底部显示记录数。</p>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / checkitem（检查项管理）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>以表格形式展示全部检查项（ID、名称、分类、价格、描述、创建时间）；</li>
 *   <li>支持按名称/分类关键字本地模糊查询；</li>
 *   <li>通过 {@link CreateCheckItemDialog} 完成检查项的新增与编辑；</li>
 *   <li>对选中记录进行删除（带二次确认）；</li>
 *   <li>底部状态栏实时显示当前记录条数。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckItemDao}，负责检查项的增删改查数据库操作；</li>
 *   <li>实体：{@link CheckItem}，承载单条检查项数据；</li>
 *   <li>UI 基类：{@link JPanel}、{@link JTable}、{@link DefaultTableModel}、
 *       {@link JOptionPane} 等 Swing 标准控件；</li>
 *   <li>UI 主题：{@link UITheme}，提供主色常量 {@code PRIMARY} 与
 *       {@code UITheme.styleTable(JTable)} 表格统一样式；</li>
 *   <li>对话框：{@link CreateCheckItemDialog}，新增/编辑检查项的弹出窗口。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（检查项管理）
 */
public class CheckItemManagePanel extends JPanel {

    /** 所属父窗口（用于弹出对话框时作为 owner 传参，保证对话框置顶于主窗口之上） */
    private Window owner;
    /** 检查项数据访问对象，封装对 check_item 表的增删改查 SQL 调用 */
    private final CheckItemDao checkItemDao = new CheckItemDao();
    /** 检查项列表展示表格，绑定到 tableModel 渲染数据 */
    private JTable checkItemTable;
    /** 表格数据模型，维护列名与行数据，通过 addRow/setRowCount 控制表格内容 */
    private DefaultTableModel tableModel;
    /** 「创建检查项」按钮，点击后弹出新增对话框 */
    private JButton createButton;
    /** 「刷新」按钮，重新从数据库加载全部检查项 */
    private JButton refreshButton;
    /** 「编辑」按钮，打开编辑对话框修改选中行 */
    private JButton editButton;
    /** 「删除」按钮，删除选中行（带确认提示） */
    private JButton deleteButton;
    /** 「查询」按钮，按搜索框关键字过滤表格 */
    private JButton queryButton;
    /** 关键字搜索框，支持回车触发查询 */
    private JTextField searchField;
    /** 底部状态栏，展示当前表格记录总数 */
    private JLabel statusLabel;
    /** 日期格式化器，将数据库返回的 Timestamp 格式化为 "yyyy-MM-dd HH:mm:ss" 字符串展示 */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /** 全量检查项缓存（用于本地过滤），loadTableData 时从 DAO 拉取后暂存于此，搜索时在内存中筛选 */
    private List<CheckItem> allItems = new ArrayList<CheckItem>();

    /**
     * 构造检查项管理面板并加载数据。
     *
     * <p>执行流程：保存父窗口引用 → 初始化界面组件与事件绑定（{@link #initUI()}）
     * → 首次从数据库加载检查项数据填充表格（{@link #loadTableData()}）。</p>
     *
     * @param owner 所属父窗口（通常为主界面 JFrame），后续弹出对话框时作为 owner 传入
     */
    public CheckItemManagePanel(Window owner) {
        // 记录父窗口引用，供后续对话框置顶使用
        this.owner = owner;
        // 构建顶部工具栏、数据表格、底部状态栏，并绑定各按钮的事件监听器
        initUI();
        // 构造完成后立即加载一次数据，避免面板显示空白
        loadTableData();
    }

    /**
     * 初始化界面布局：顶部工具栏 + 表格 + 底部状态栏，并绑定事件。
     *
     * <p>布局采用 {@link BorderLayout}：NORTH 放置顶部工具栏，CENTER 放置带滚动条的表格，
     * SOUTH 放置底部状态栏。同时为各按钮与搜索框注册 ActionListener。</p>
     */
    private void initUI() {
        // 整体使用 BorderLayout，便于将工具栏/表格/状态栏分布在南北中三个区域
        setLayout(new BorderLayout());

        // ---- 顶部工具栏：标题 + 搜索框 + 各操作按钮 ----
        // 使用 FlowLayout.LEFT 让子组件从左向右排列，水平间距 8、垂直间距 10
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        // 设置顶部面板四周留白（上10、左10、下4、右10），避免组件紧贴边框
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        // 模块标题"检查项管理"，使用微软雅黑粗体 18 号字
        JLabel titleLabel = new JLabel("检查项管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        topPanel.add(titleLabel);

        // 在标题与搜索框之间插入 20 像素的水平空白间隔，拉开视觉距离
        topPanel.add(Box.createHorizontalStrut(20));

        // 关键字搜索框，列宽 10 列，使用微软雅黑常规 14 号字
        searchField = new JTextField(10);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        topPanel.add(searchField);

        // 查询按钮：传入 null 背景色，表示使用默认浅灰样式
        queryButton = buildToolButton("查询", null);
        topPanel.add(queryButton);

        // 创建检查项按钮：传入 UITheme.PRIMARY 主蓝色背景，使其视觉突出（主操作）
        createButton = buildToolButton("创建检查项", UITheme.PRIMARY);
        topPanel.add(createButton);

        // 编辑按钮：默认浅灰样式
        editButton = buildToolButton("编辑", null);
        topPanel.add(editButton);

        // 删除按钮：默认浅灰样式
        deleteButton = buildToolButton("删除", null);
        topPanel.add(deleteButton);

        // 刷新按钮：默认浅灰样式
        refreshButton = buildToolButton("刷新", null);
        topPanel.add(refreshButton);

        // 将顶部工具栏整体放到 BorderLayout 的 NORTH 区域
        add(topPanel, BorderLayout.NORTH);

        // ---- 中部数据表格 ----
        // 定义表格列名：ID、检查项名称、分类、价格(元)、描述、创建时间
        String[] columnNames = {"ID", "检查项名称", "分类", "价格(元)", "描述", "创建时间"};
        // 构建表格模型：初始行数为 0；通过匿名内部类重写 isCellEditable 使所有单元格不可编辑
        tableModel = new DefaultTableModel(columnNames, 0) {
            /**
             * 判断指定单元格是否可编辑。
             *
             * @param row    行索引（未使用）
             * @param column 列索引（未使用）
             * @return 恒为 false，表示表格仅用于展示，不允许在表格内直接编辑
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                // 表格只读：编辑统一走"编辑"按钮弹出对话框，避免误改
                return false;
            }
        };
        // 用上面的模型创建 JTable
        checkItemTable = new JTable(tableModel);
        // 表格正文使用微软雅黑 13 号
        checkItemTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        // 每行高度 28 像素，提升可读性
        checkItemTable.setRowHeight(28);
        // 表头使用微软雅黑粗体 13 号
        checkItemTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        // 设置为单选模式，保证"编辑/删除"操作只能针对一条记录
        checkItemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 调用 UITheme 统一表格样式（交替行颜色、网格线等），保持视觉一致
        UITheme.styleTable(checkItemTable);

        // 将表格放入 JScrollPane，使其在数据超出可视区域时出现滚动条
        JScrollPane scrollPane = new JScrollPane(checkItemTable);
        // 滚动面板四周留白（上0、左10、下10、右10），与顶部工具栏呼应
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        // 将带滚动条的表格放到 BorderLayout 的 CENTER 区域，占据剩余空间
        add(scrollPane, BorderLayout.CENTER);

        // ---- 底部状态栏 ----
        // 初始显示"共 0 条记录"，后续由 renderRows 更新实际条数
        statusLabel = new JLabel("  共 0 条记录");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);

        // ---- 事件监听：为各按钮与搜索框注册动作监听器 ----
        // "创建检查项"按钮：点击后打开新增对话框
        createButton.addActionListener(new ActionListener() {
            /**
             * 响应"创建检查项"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateDialog();
            }
        });
        // "编辑"按钮：对当前选中行打开编辑对话框
        editButton.addActionListener(new ActionListener() {
            /**
             * 响应"编辑"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelected();
            }
        });
        // "删除"按钮：删除当前选中行
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
        // "查询"按钮：按搜索框关键字过滤表格
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
        // "刷新"按钮：重新从数据库加载全部数据
        refreshButton.addActionListener(new ActionListener() {
            /**
             * 响应"刷新"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                // 直接调用 loadTableData 重新拉取并渲染
                loadTableData();
            }
        });
        // 回车也可触发查询：在搜索框内按回车等同于点击"查询"按钮
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
    }

    /**
     * 统一的工具栏按钮样式工厂方法。
     *
     * <p>根据传入的背景色决定按钮外观：若 bg 为 null 则使用默认浅灰（次要操作按钮）；
     * 若传入 {@link UITheme#PRIMARY} 主色，则使用蓝底白字（主操作按钮），并关闭焦点框。</p>
     *
     * @param text 按钮显示文字
     * @param bg   按钮背景色；为 null 时不设置背景色，使用系统默认浅灰
     * @return 配置好字体与颜色的按钮对象
     */
    private JButton buildToolButton(String text, Color bg) {
        // 创建按钮并设置微软雅黑 14 号字体，保持全应用字体一致
        JButton b = new JButton(text);
        b.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 仅当传入背景色时才套用主色样式（蓝底白字、去掉焦点框）
        if (bg != null) {
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            // setFocusPainted(false) 去掉按钮获得焦点时的虚线框，视觉更简洁
            b.setFocusPainted(false);
        }
        return b;
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     *
     * <p>主界面在切换到本面板对应的 Tab 时会回调此方法，确保每次进入都看到最新数据。</p>
     */
    public void onShow() {
        // 重新从数据库加载并渲染
        loadTableData();
    }

    /**
     * 打开创建检查项对话框（保存成功后刷新表格）。
     *
     * <p>通过 {@link CreateCheckItemDialog} 的第三个参数（Runnable 回调）实现：
     * 当对话框内保存成功时，回调该 Runnable 重新加载表格数据。</p>
     */
    private void openCreateDialog() {
        // 构造新增对话框：owner 为 this（本面板所在窗口），待编辑对象传 null 表示新增
        // 第三个参数为保存成功后的回调 Runnable，触发 loadTableData 刷新
        CreateCheckItemDialog dialog = new CreateCheckItemDialog(this, null, new Runnable() {
            /**
             * 对话框保存成功后的回调：刷新表格数据。
             */
            @Override
            public void run() {
                loadTableData();
            }
        });
        // 以模态方式显示对话框，阻塞直到对话框关闭
        dialog.setVisible(true);
    }

    /**
     * 加载全部检查项并渲染到表格。
     *
     * <p>调用 {@link CheckItemDao#queryAll()} 从数据库拉取全部检查项，
     * 先暂存到 {@link #allItems} 作为本地过滤缓存，再调用 {@link #renderRows(List)} 渲染。
     * 若 DAO 抛出异常，则弹出错误提示框。</p>
     */
    public void loadTableData() {
        try {
            // DAO 查询全部检查项，返回 List<CheckItem>
            allItems = checkItemDao.queryAll();
            // 用全量数据渲染表格
            renderRows(allItems);
        } catch (Exception e) {
            // DAO 层任何异常（如 SQL 错误、连接失败）都统一弹出错误对话框提示用户
            JOptionPane.showMessageDialog(this, "加载数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 根据关键字按名称/分类本地过滤。
     *
     * <p>过滤在内存中进行：将 {@link #allItems} 中名称或分类包含关键字（忽略大小写）
     * 的记录收集到 filtered 列表，再调用 {@link #renderRows(List)} 渲染。
     * 关键字为空时直接渲染全量数据，相当于清除搜索。</p>
     */
    private void doSearch() {
        // 去除搜索框前后空白
        String kw = searchField.getText().trim();
        // 关键字为空：渲染全部记录，相当于"清除搜索"
        if (kw.isEmpty()) {
            renderRows(allItems);
            return;
        }
        // 统一转小写，实现不区分大小写的模糊匹配
        String lower = kw.toLowerCase();
        List<CheckItem> filtered = new ArrayList<CheckItem>();
        for (CheckItem item : allItems) {
            // 匹配规则：名称包含关键字 或 分类包含关键字（任一命中即保留）
            // 同时对 name/category 做 null 判断，避免 NPE
            boolean hit = (item.getName() != null && item.getName().toLowerCase().contains(lower))
                    || (item.getCategory() != null && item.getCategory().toLowerCase().contains(lower));
            if (hit) {
                filtered.add(item);
            }
        }
        // 将过滤后的子集渲染到表格
        renderRows(filtered);
    }

    /**
     * 将检查项列表渲染到表格并更新底部统计。
     *
     * <p>先通过 {@code tableModel.setRowCount(0)} 清空现有所有行，再遍历传入列表逐行 addRow。
     * 渲染完成后同步更新底部状态栏的记录数。</p>
     *
     * @param list 待展示的检查项列表（可能是全量 allItems，也可能是过滤后的子集）
     */
    private void renderRows(List<CheckItem> list) {
        // setRowCount(0) 会删除表格模型中的全部行，等价于清空表格，避免数据残留
        tableModel.setRowCount(0);
        for (CheckItem item : list) {
            // 将实体字段按列顺序组装成一行 Object 数组
            // 价格与创建时间可能为 null，做空值保护后再转字符串
            Object[] row = new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.getPrice() != null ? item.getPrice().toString() : "",
                    item.getDescription(),
                    item.getCreateTime() != null ? sdf.format(item.getCreateTime()) : ""
            };
            // addRow 将这一行追加到表格模型末尾
            tableModel.addRow(row);
        }
        // 更新底部状态栏，显示当前渲染的记录总数
        statusLabel.setText("  共 " + list.size() + " 条记录");
    }

    /**
     * 获取当前表格中选中的检查项对象。
     *
     * <p>通过 {@code tableModel.getValueAt(row, 0)} 取出第一列（ID），
     * 再在 {@link #allItems} 中按 ID 匹配出对应的 {@link CheckItem} 实体。</p>
     *
     * @return 选中的检查项实体；未选择或 ID 不匹配时返回 null（未选择时已弹窗提示）
     */
    private CheckItem getSelectedItem() {
        // getSelectedRow 返回当前选中行的索引，-1 表示没有选中任何行
        int row = checkItemTable.getSelectedRow();
        if (row < 0) {
            // 未选中时弹出警告提示框
            JOptionPane.showMessageDialog(this, "请先选择一条记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        // 从表格第一列取出选中行的 ID（第一列存的是 Integer 类型）
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        if (id == null) {
            return null;
        }
        // 在全量缓存中按 ID 查找对应的实体对象
        for (CheckItem item : allItems) {
            if (id.equals(item.getId())) {
                return item;
            }
        }
        // 正常情况下不会走到这里（ID 一定能在 allItems 中找到），兜底返回 null
        return null;
    }

    /**
     * 编辑选中的检查项（打开编辑对话框）。
     *
     * <p>流程：先调用 {@link #getSelectedItem()} 取出选中实体，若为空则直接返回；
     * 否则以该实体为初始数据打开 {@link CreateCheckItemDialog}（复用新增对话框做编辑），
     * 保存成功后通过回调刷新表格。</p>
     */
    private void editSelected() {
        // 获取选中项；若未选中，getSelectedItem 已弹窗提示，此处直接返回
        CheckItem item = getSelectedItem();
        if (item == null) {
            return;
        }
        // 以已存在的 item 作为初始数据打开对话框（构造器内部据此判断是新增还是编辑）
        CreateCheckItemDialog dialog = new CreateCheckItemDialog(this, item, new Runnable() {
            /**
             * 对话框保存成功后的回调：刷新表格数据。
             */
            @Override
            public void run() {
                loadTableData();
            }
        });
        dialog.setVisible(true);
    }

    /**
     * 删除选中的检查项（带确认提示）。
     *
     * <p>流程：取选中项 → 弹出 {@code showConfirmDialog} 二次确认 → 调用
     * {@link CheckItemDao#delete(Integer)} 删除 → 根据返回值提示成功/失败并刷新表格。
     * DAO 异常统一捕获并弹窗提示。</p>
     */
    private void deleteSelected() {
        // 获取选中项；若未选中则直接返回
        CheckItem item = getSelectedItem();
        if (item == null) {
            return;
        }
        // 弹出确认对话框，YES_NO_OPTION 表示提供"是/否"两个按钮
        int op = JOptionPane.showConfirmDialog(this,
                "确定删除检查项“" + item.getName() + "”吗？", "删除确认",
                JOptionPane.YES_NO_OPTION);
        // 用户选择"否"或关闭对话框，则取消删除
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            // 调用 DAO 按 ID 删除，返回受影响行数
            int result = checkItemDao.delete(item.getId());
            // 返回值 > 0 表示删除成功
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                // 删除成功后重新加载表格，刷新界面
                loadTableData();
            } else {
                // 返回 0 表示未删除到任何行（可能已被他人删除）
                JOptionPane.showMessageDialog(this, "删除失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            // 数据库异常（外键约束、连接错误等）统一弹窗提示
            JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
