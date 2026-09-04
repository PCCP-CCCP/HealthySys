package com.nd.view;

import com.nd.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterDialog extends JDialog {

    private JTextField telField;
    private JTextField nameField;
    private JPasswordField pwdField;
    private JPasswordField confirmField;
    private JRadioButton patientRadio;
    private JRadioButton doctorRadio;
    private JButton submitButton;
    private JButton cancelButton;
    private UserService userService = new UserService();

    public RegisterDialog(JFrame parent) {
        super(parent, "新用户注册", true);
        initUI();
    }

    private void initUI() {
        setSize(440, 430);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 手机号
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel telLabel = new JLabel("手机号:");
        telLabel.setFont(labelFont);
        mainPanel.add(telLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        telField = new JTextField(16);
        telField.setFont(fieldFont);
        mainPanel.add(telField, gbc);

        // 姓名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel nameLabel = new JLabel("姓名:");
        nameLabel.setFont(labelFont);
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        nameField = new JTextField(16);
        nameField.setFont(fieldFont);
        mainPanel.add(nameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel pwdLabel = new JLabel("密码:");
        pwdLabel.setFont(labelFont);
        mainPanel.add(pwdLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        pwdField = new JPasswordField(16);
        pwdField.setFont(fieldFont);
        mainPanel.add(pwdField, gbc);

        // 确认密码
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel confirmLabel = new JLabel("确认密码:");
        confirmLabel.setFont(labelFont);
        mainPanel.add(confirmLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        confirmField = new JPasswordField(16);
        confirmField.setFont(fieldFont);
        mainPanel.add(confirmField, gbc);

        // 角色选择
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel roleLabel = new JLabel("角色类型:");
        roleLabel.setFont(labelFont);
        mainPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        JPanel rolePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        patientRadio = new JRadioButton("仅患者角色（患者视角）");
        patientRadio.setFont(fieldFont);
        patientRadio.setSelected(true);
        doctorRadio = new JRadioButton("双重角色（医生+患者，可切换视角）");
        doctorRadio.setFont(fieldFont);
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(patientRadio);
        roleGroup.add(doctorRadio);
        rolePanel.add(patientRadio);
        rolePanel.add(doctorRadio);
        mainPanel.add(rolePanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        submitButton = new JButton("注册");
        submitButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitButton.setBackground(com.nd.view.utils.UITheme.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        buttonPanel.add(submitButton);

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 事件监听
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
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
     * 处理注册按钮点击
     */
    private void handleRegister() {
        String tel = telField.getText().trim();
        String name = nameField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String role = doctorRadio.isSelected() ? "doctor" : "patient";
        try {
            userService.register(tel, pwd, confirm, name, role);
            JOptionPane.showMessageDialog(this, "注册成功，请登录！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
}
