package com.nd.ui.feature.checkitem;

import com.nd.common.entity.CheckItem;
import com.nd.dao.CheckItemDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

/**
 * 创建/编辑检查项对话框（医生角色）。
 *
 * <p>支持两种模式：item 为 null 时创建新检查项，否则预填并编辑已有检查项；
 * 保存成功后通过 onSuccess 回调刷新所属面板。</p>
 *
 * @author HealthySys 功能界面模块（检查项管理）
 */
public class CreateCheckItemDialog extends JDialog {

    /** 检查项名称输入框 */
    private JTextField nameField;
    /** 分类输入框 */
    private JTextField categoryField;
    /** 价格输入框 */
    private JTextField priceField;
    /** 描述多行输入框 */
    private JTextArea descriptionArea;
    /** 完成按钮 */
    private JButton completeButton;
    /** 取消按钮 */
    private JButton cancelButton;
    /** 所属组件（用于定位对话框） */
    private Component owner;
    /** 保存成功回调 */
    private Runnable onSuccess;
    /** 正在编辑的检查项（null=新建） */
    private CheckItem editingItem;
    /** 检查项数据访问对象 */
    private final CheckItemDao checkItemDao = new CheckItemDao();

    /**
     * 简化的构造：新建检查项，无成功回调。
     *
     * @param owner 所属组件
     */
    public CreateCheckItemDialog(Component owner) {
        this(owner, null, null);
    }

    /**
     * 编辑模式构造：传入待编辑的检查项用于预填数据。
     *
     * @param owner 所属组件
     * @param item  待编辑检查项（null 表示新建）
     */
    public CreateCheckItemDialog(Component owner, CheckItem item) {
        this(owner, item, null);
    }

    /**
     * 完整构造。
     *
     * @param owner     所属组件
     * @param item      待编辑检查项（null 表示新建）
     * @param onSuccess 保存成功后的回调（用于刷新所属面板数据）
     */
    public CreateCheckItemDialog(Component owner, CheckItem item, Runnable onSuccess) {
        super((Window) SwingUtilities.getWindowAncestor(owner),
                item == null ? "创建检查项" : "编辑检查项",
                ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.onSuccess = onSuccess;
        this.editingItem = item;
        initUI();
        if (item != null) {
            prefill(item);
        }
    }

    /**
     * 编辑模式下预填表单数据。
     *
     * @param item 待编辑的检查项
     */
    private void prefill(CheckItem item) {
        nameField.setText(item.getName());
        categoryField.setText(item.getCategory());
        if (item.getPrice() != null) {
            priceField.setText(item.getPrice().toPlainString());
        }
        descriptionArea.setText(item.getDescription());
    }

    /**
     * 初始化界面布局与事件绑定。
     */
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
        completeButton.setBackground(UITheme.PRIMARY);
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
     * 处理完成按钮点击：校验输入、执行新增或修改、刷新并关闭。
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
                result = checkItemDao.update(item);
                successMsg = "检查项修改成功！";
            } else {
                result = checkItemDao.insert(item);
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
