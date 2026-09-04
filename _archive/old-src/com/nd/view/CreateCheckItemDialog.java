package com.nd.view;

import com.nd.view.entity.CheckItem;
import com.nd.view.utils.JdbcUitl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

public class CreateCheckItemDialog extends JDialog {

    private JTextField nameField;
    private JTextField categoryField;
    private JTextField priceField;
    private JTextArea descriptionArea;
    private JButton completeButton;
    private JButton cancelButton;
    private Component owner;
    private Runnable onSuccess;
    private CheckItem editingItem;

    public CreateCheckItemDialog(Component owner) {
        this(owner, null, null);
    }

    /**
     * 编辑模式：传入待编辑的检查项对象用于预填数据
     */
    public CreateCheckItemDialog(Component owner, CheckItem item) {
        this(owner, item, null);
    }

    /**
     * @param onSuccess 保存成功后的回调（用于刷新所属面板数据）
     */
    public CreateCheckItemDialog(Component owner, CheckItem item, Runnable onSuccess) {
        super((Window) SwingUtilities.getWindowAncestor(owner), item == null ? "创建检查项" : "编辑检查项", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.onSuccess = onSuccess;
        this.editingItem = item;
        initUI();
        if (item != null) {
            prefill(item);
        }
    }

    /**
     * 编辑时预填表单数据
     */
    private void prefill(CheckItem item) {
        nameField.setText(item.getName());
        categoryField.setText(item.getCategory());
        if (item.getPrice() != null) {
            priceField.setText(item.getPrice().toPlainString());
        }
        descriptionArea.setText(item.getDescription());
    }

    private void initUI() {
        setSize(450, 400);
        setLocationRelativeTo(owner);
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 检查项名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("检查项名称:");
        nameLabel.setFont(labelFont);
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        mainPanel.add(nameField, gbc);

        // 分类
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel categoryLabel = new JLabel("分      类:");
        categoryLabel.setFont(labelFont);
        mainPanel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        categoryField = new JTextField(20);
        categoryField.setFont(fieldFont);
        mainPanel.add(categoryField, gbc);

        // 价格
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel priceLabel = new JLabel("价格(元):");
        priceLabel.setFont(labelFont);
        mainPanel.add(priceLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        priceField = new JTextField(20);
        priceField.setFont(fieldFont);
        mainPanel.add(priceField, gbc);

        // 描述
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel descLabel = new JLabel("描      述:");
        descLabel.setFont(labelFont);
        mainPanel.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        mainPanel.add(descScroll, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        completeButton = new JButton("完成");
        completeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        completeButton.setBackground(com.nd.view.utils.UITheme.PRIMARY);
        completeButton.setForeground(Color.WHITE);
        completeButton.setFocusPainted(false);
        completeButton.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(completeButton);

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 事件监听
        completeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleComplete();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * 处理完成按钮点击
     */
    private void handleComplete() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String priceStr = priceField.getText().trim();
        String description = descriptionArea.getText().trim();

        // 验证必填项
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入检查项名称！", "提示", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 验证价格格式
        BigDecimal price = null;
        if (!priceStr.isEmpty()) {
            try {
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "价格不能为负数！", "提示", JOptionPane.WARNING_MESSAGE);
                    priceField.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "价格格式不正确，请输入数字！", "提示", JOptionPane.WARNING_MESSAGE);
                priceField.requestFocus();
                return;
            }
        }

        // 执行新增或修改
        try {
            CheckItem item = new CheckItem(name, category, price, description);
            int result;
            String successMsg;
            if (editingItem != null) {
                item.setId(editingItem.getId());
                result = JdbcUitl.updateCheckItem(item);
                successMsg = "检查项修改成功！";
            } else {
                result = JdbcUitl.insertCheckItem(item);
                successMsg = "检查项创建成功！";
            }
            if (result > 0) {
                JOptionPane.showMessageDialog(this, successMsg, "成功", JOptionPane.INFORMATION_MESSAGE);
                // 刷新所属面板数据
                if (onSuccess != null) {
                    onSuccess.run();
                }
                // 关闭对话框
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
