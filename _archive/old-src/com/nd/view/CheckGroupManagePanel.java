package com.nd.view;

import com.nd.view.entity.CheckGroup;
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
 * 检查组管理面板
 * 主界面一级导航模块之一，与「检查项管理」「预约」「跟踪管理」并列。
 */
public class CheckGroupManagePanel extends JPanel {

    private Window owner;
    private JTable groupTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton queryButton;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<CheckGroup> allGroups = new ArrayList<CheckGroup>();

    public CheckGroupManagePanel(Window owner) {
        this.owner = owner;
        initUI();
        loadGroups();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));

        JLabel title = new JLabel("检查组管理");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        top.add(title);
        top.add(Box.createHorizontalStrut(20));

        searchField = new JTextField(12);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        top.add(searchField);

        queryButton = buildToolButton("查询", null);
        top.add(queryButton);

        createButton = buildToolButton("创建检查组", com.nd.view.utils.UITheme.PRIMARY);
        top.add(createButton);

        editButton = buildToolButton("修改", null);
        top.add(editButton);

        deleteButton = buildToolButton("删除", null);
        top.add(deleteButton);

        refreshButton = buildToolButton("刷新", null);
        top.add(refreshButton);

        add(top, BorderLayout.NORTH);

        // 表格
        String[] columns = {"ID", "检查组名称", "组内检查项数", "创建时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        groupTable = new JTable(tableModel);
        groupTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        groupTable.setRowHeight(28);
        groupTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        com.nd.view.utils.UITheme.styleTable(groupTable);

        JScrollPane scrollPane = new JScrollPane(groupTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 底部状态栏
        statusLabel = new JLabel("  共 0 个检查组");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);

        // 事件监听
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
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
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadGroups();
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
        loadGroups();
    }

    private void loadGroups() {
        try {
            allGroups = JdbcUitl.queryCheckGroups();
            renderGroups(allGroups);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderGroups(List<CheckGroup> list) {
        tableModel.setRowCount(0);
        for (CheckGroup g : list) {
            Object[] row = new Object[]{
                    g.getId(),
                    g.getName(),
                    g.getItemCount(),
                    g.getCreateTime() != null ? sdf.format(g.getCreateTime()) : ""
            };
            tableModel.addRow(row);
        }
        statusLabel.setText("  共 " + list.size() + " 个检查组");
    }

    /**
     * 查询：按检查组名称或组内检查项名称匹配
     */
    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            renderGroups(allGroups);
            return;
        }
        String lower = kw.toLowerCase();
        List<CheckGroup> filtered = new ArrayList<CheckGroup>();
        for (CheckGroup g : allGroups) {
            boolean hit = g.getName() != null && g.getName().toLowerCase().contains(lower);
            if (!hit) {
                try {
                    List<CheckItem> items = JdbcUitl.queryGroupItems(g.getId());
                    for (CheckItem it : items) {
                        if (it.getName() != null && it.getName().toLowerCase().contains(lower)) {
                            hit = true;
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            if (hit) {
                filtered.add(g);
            }
        }
        renderGroups(filtered);
    }

    private CheckGroup getSelectedGroup() {
        int row = groupTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个检查组！", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        if (id == null) {
            return null;
        }
        for (CheckGroup g : allGroups) {
            if (id.equals(g.getId())) {
                return g;
            }
        }
        return null;
    }

    private void openCreateDialog() {
        CheckGroupEditDialog dlg = new CheckGroupEditDialog(owner, null);
        dlg.setVisible(true);
        loadGroups();
    }

    private void editSelected() {
        CheckGroup g = getSelectedGroup();
        if (g == null) {
            return;
        }
        CheckGroupEditDialog dlg = new CheckGroupEditDialog(owner, g);
        dlg.setVisible(true);
        loadGroups();
    }

    private void deleteSelected() {
        CheckGroup g = getSelectedGroup();
        if (g == null) {
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "确定删除检查组“" + g.getName() + "”吗？", "删除确认",
                JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int result = JdbcUitl.deleteCheckGroup(g.getId());
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
