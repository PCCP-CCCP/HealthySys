package com.nd.ui.feature.checkgroup;

import com.nd.common.entity.CheckGroup;
import com.nd.common.entity.CheckItem;
import com.nd.dao.CheckGroupDao;
import com.nd.dao.CheckItemDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建/修改检查组对话框（医生角色）。
 *
 * <p>输入检查组名称并勾选组内检查项，保存时通过 DAO 在事务中写入
 * 检查组及其组内关联；编辑模式会预填名称并预勾选原有检查项。</p>
 *
 * @author HealthySys 功能界面模块（检查组管理）
 */
public class CheckGroupEditDialog extends JDialog {

    /** 所属父窗口 */
    private Window owner;
    /** 正在编辑的检查组（null=新建） */
    private CheckGroup editingGroup;
    /** 检查组名称输入框 */
    private JTextField nameField;
    /** 检查项勾选面板 */
    private JPanel itemPanel;
    /** 全部检查项列表 */
    private List<CheckItem> allItems = new ArrayList<CheckItem>();
    /** 与 allItems 一一对应的复选框列表 */
    private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    /** 检查组数据访问对象 */
    private final CheckGroupDao checkGroupDao = new CheckGroupDao();
    /** 检查项数据访问对象 */
    private final CheckItemDao checkItemDao = new CheckItemDao();

    /**
     * 构造对话框。
     *
     * @param owner 所属父窗口
     * @param group 待编辑检查组（null 表示新建）
     */
    public CheckGroupEditDialog(Window owner, CheckGroup group) {
        super(owner, group == null ? "创建检查组" : "修改检查组", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.editingGroup = group;
        initUI();
    }

    /**
     * 初始化界面布局：名称输入 + 检查项勾选列表 + 按钮，并加载检查项数据。
     */
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
        saveBtn.setBackground(UITheme.PRIMARY);
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

    /**
     * 加载全部检查项并生成复选框；编辑模式下预填名称并预勾选原有检查项。
     */
    private void loadCheckItems() {
        try {
            allItems = checkItemDao.queryAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Integer> preset = new ArrayList<Integer>();
        if (editingGroup != null) {
            nameField.setText(editingGroup.getName());
            try {
                preset = checkGroupDao.queryGroupItemIds(editingGroup.getId());
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

    /**
     * 处理保存：校验名称、收集勾选的检查项 ID，调用 DAO 创建或修改。
     */
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
                int id = checkGroupDao.create(name, itemIds);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "创建失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                int result = checkGroupDao.update(editingGroup.getId(), name, itemIds);
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
