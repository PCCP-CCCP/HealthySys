package com.nd.view;

import com.nd.view.entity.CheckGroup;
import com.nd.view.entity.CheckItem;
import com.nd.view.utils.JdbcUitl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CheckGroupEditDialog extends JDialog {

    private Window owner;
    private CheckGroup editingGroup;
    private JTextField nameField;
    private JPanel itemPanel;
    private List<CheckItem> allItems = new ArrayList<CheckItem>();
    private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

    public CheckGroupEditDialog(Window owner, CheckGroup group) {
        super(owner, group == null ? "创建检查组" : "修改检查组", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.editingGroup = group;
        initUI();
    }

    private void initUI() {
        setSize(540, 520);
        setLocationRelativeTo(owner);
        setResizable(false);

        // 名称
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JLabel nameLabel = new JLabel("检查组名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        namePanel.add(nameLabel);
        nameField = new JTextField(26);
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        namePanel.add(nameField);
        add(namePanel, BorderLayout.NORTH);

        // 勾选检查项
        JPanel center = new JPanel(new BorderLayout());
        JLabel tip = new JLabel("请勾选要加入组内的检查项：");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        tip.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 0));
        center.add(tip, BorderLayout.NORTH);
        itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(itemPanel);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveBtn.setBackground(com.nd.view.utils.UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // 事件监听
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        loadCheckItems();
    }

    private void loadCheckItems() {
        try {
            allItems = JdbcUitl.queryCheckItems();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Integer> preset = new ArrayList<Integer>();
        if (editingGroup != null) {
            nameField.setText(editingGroup.getName());
            try {
                preset = JdbcUitl.queryGroupItemIds(editingGroup.getId());
            } catch (Exception e) {
                preset = new ArrayList<Integer>();
            }
        }
        checkBoxes.clear();
        itemPanel.removeAll();
        for (CheckItem item : allItems) {
            String text = item.getName() + (item.getCategory() != null ? "（" + item.getCategory() + "）" : "");
            JCheckBox cb = new JCheckBox(text);
            cb.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            if (preset.contains(item.getId())) {
                cb.setSelected(true);
            }
            checkBoxes.add(cb);
            itemPanel.add(cb);
        }
        itemPanel.revalidate();
        itemPanel.repaint();
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入检查组名称！", "提示", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        List<Integer> itemIds = new ArrayList<Integer>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                itemIds.add(allItems.get(i).getId());
            }
        }
        try {
            if (editingGroup == null) {
                int id = JdbcUitl.createCheckGroup(name, itemIds);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "创建失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                int result = JdbcUitl.updateCheckGroup(editingGroup.getId(), name, itemIds);
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
