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
 * @author HealthySys 功能界面模块（检查项管理）
 */
public class CheckItemManagePanel extends JPanel {

    /** 所属父窗口（用于弹出对话框） */
    private Window owner;
    /** 检查项数据访问对象 */
    private final CheckItemDao checkItemDao = new CheckItemDao();
    /** 检查项表格 */
    private JTable checkItemTable;
    /** 表格数据模型 */
    private DefaultTableModel tableModel;
    /** 创建按钮 */
    private JButton createButton;
    /** 刷新按钮 */
    private JButton refreshButton;
    /** 编辑按钮 */
    private JButton editButton;
    /** 删除按钮 */
    private JButton deleteButton;
    /** 查询按钮 */
    private JButton queryButton;
    /** 关键字搜索框 */
    private JTextField searchField;
    /** 底部状态栏 */
    private JLabel statusLabel;
    /** 日期格式化器 */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /** 全量检查项缓存（用于本地过滤） */
    private List<CheckItem> allItems = new ArrayList<CheckItem>();

    /**
     * 构造检查项管理面板并加载数据。
     *
     * @param owner 所属父窗口
     */
    public CheckItemManagePanel(Window owner) {
        this.owner = owner;
        initUI();
        loadTableData();
    }

    /**
     * 初始化界面布局：顶部工具栏 + 表格 + 底部状态栏，并绑定事件。
     */
    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        JLabel titleLabel = new JLabel("检查项管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        topPanel.add(titleLabel);

        topPanel.add(Box.createHorizontalStrut(20));

        searchField = new JTextField(10);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        topPanel.add(searchField);

        queryButton = buildToolButton("查询", null);
        topPanel.add(queryButton);

        createButton = buildToolButton("创建检查项", UITheme.PRIMARY);
        topPanel.add(createButton);

        editButton = buildToolButton("编辑", null);
        topPanel.add(editButton);

        deleteButton = buildToolButton("删除", null);
        topPanel.add(deleteButton);

        refreshButton = buildToolButton("刷新", null);
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"ID", "检查项名称", "分类", "价格(元)", "描述", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        checkItemTable = new JTable(tableModel);
        checkItemTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        checkItemTable.setRowHeight(28);
        checkItemTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        checkItemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(checkItemTable);

        JScrollPane scrollPane = new JScrollPane(checkItemTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 底部状态栏
        statusLabel = new JLabel("  共 0 条记录");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);

        // 事件监听
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateDialog();
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelected();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTableData();
            }
        });
        // 回车也可触发查询
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
    }

    /**
     * 统一的工具栏按钮样式。
     *
     * @param text 按钮文字
     * @param bg   背景色（null 为默认浅灰）
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
        loadTableData();
    }

    /**
     * 打开创建检查项对话框（保存成功后刷新表格）。
     */
    private void openCreateDialog() {
        CreateCheckItemDialog dialog = new CreateCheckItemDialog(this, null, new Runnable() {
            @Override
            public void run() {
                loadTableData();
            }
        });
        dialog.setVisible(true);
    }

    /**
     * 加载全部检查项并渲染到表格。
     */
    public void loadTableData() {
        try {
            allItems = checkItemDao.queryAll();
            renderRows(allItems);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 根据关键字按名称/分类本地过滤。
     */
    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            renderRows(allItems);
            return;
        }
        String lower = kw.toLowerCase();
        List<CheckItem> filtered = new ArrayList<CheckItem>();
        for (CheckItem item : allItems) {
            boolean hit = (item.getName() != null && item.getName().toLowerCase().contains(lower))
                    || (item.getCategory() != null && item.getCategory().toLowerCase().contains(lower));
            if (hit) {
                filtered.add(item);
            }
        }
        renderRows(filtered);
    }

    /**
     * 将检查项列表渲染到表格并更新底部统计。
     *
     * @param list 待展示的检查项列表
     */
    private void renderRows(List<CheckItem> list) {
        tableModel.setRowCount(0);
        for (CheckItem item : list) {
            Object[] row = new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.getPrice() != null ? item.getPrice().toString() : "",
                    item.getDescription(),
                    item.getCreateTime() != null ? sdf.format(item.getCreateTime()) : ""
            };
            tableModel.addRow(row);
        }
        statusLabel.setText("  共 " + list.size() + " 条记录");
    }

    /**
     * 获取当前表格中选中的检查项对象。
     *
     * @return 选中的检查项；未选择或异常返回 null
     */
    private CheckItem getSelectedItem() {
        int row = checkItemTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        if (id == null) {
            return null;
        }
        for (CheckItem item : allItems) {
            if (id.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 编辑选中的检查项（打开编辑对话框）。
     */
    private void editSelected() {
        CheckItem item = getSelectedItem();
        if (item == null) {
            return;
        }
        CreateCheckItemDialog dialog = new CreateCheckItemDialog(this, item, new Runnable() {
            @Override
            public void run() {
                loadTableData();
            }
        });
        dialog.setVisible(true);
    }

    /**
     * 删除选中的检查项（带确认提示）。
     */
    private void deleteSelected() {
        CheckItem item = getSelectedItem();
        if (item == null) {
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "确定删除检查项“" + item.getName() + "”吗？", "删除确认",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int result = checkItemDao.delete(item.getId());
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
