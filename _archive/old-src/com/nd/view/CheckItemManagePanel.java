package com.nd.view;

import com.nd.view.entity.CheckItem;
import com.nd.view.utils.JdbcUitl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查项管理面板
 * 主界面一级导航模块之一，与「检查组管理」「预约」「跟踪管理」并列。
 */
public class CheckItemManagePanel extends JPanel {

    private Window owner;
    private JTable checkItemTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton refreshButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton queryButton;
    private JTextField searchField;
    private JLabel statusLabel;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<CheckItem> allItems = new ArrayList<CheckItem>();

    public CheckItemManagePanel(Window owner) {
        this.owner = owner;
        initUI();
        loadTableData();
    }

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

        createButton = buildToolButton("创建检查项", com.nd.view.utils.UITheme.PRIMARY);
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
        com.nd.view.utils.UITheme.styleTable(checkItemTable);

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
     * 统一的工具栏按钮样式
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
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）
     */
    public void onShow() {
        loadTableData();
    }

    /**
     * 打开创建检查项对话框
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
     * 加载表格数据
     */
    public void loadTableData() {
        try {
            allItems = JdbcUitl.queryCheckItems();
            renderRows(allItems);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 根据关键字按名称/分类过滤
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
     * 将数据渲染到表格
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
     * 获取当前选中的检查项
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
     * 编辑选中的检查项
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
     * 删除选中的检查项
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
            int result = JdbcUitl.deleteCheckItem(item.getId());
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
