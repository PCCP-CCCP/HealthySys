package com.nd.ui.shell;

import com.nd.service.UserService;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 新用户注册对话框（应用外壳）。
 *
 * <p>本类属于 <b>healthy-ui-shell（应用外壳层）</b>模块，继承 {@link JDialog}，
 * 以模态对话框形式展示注册表单。</p>
 *
 * <p>注册时需选择账号角色：</p>
 * <ul>
 *   <li><b>仅患者角色</b>：只能使用患者视角（预约/跟踪管理）；</li>
 *   <li><b>双重角色（医生+患者）</b>：拥有医生角色，登录后可通过主界面右上角
 *       「一键切换角色」在医生视角（检查项管理/检查组管理/录入结果/查看患者结果）
 *       与患者视角间切换。</li>
 * </ul>
 *
 * <p>注册流程：收集手机号/姓名/密码/确认密码/角色 → 调用
 * {@link UserService#register(String, String, String, String, String)} 完成
 * 密码加密与数据库插入 → 成功提示并关闭对话框。</p>
 *
 * <p>关键依赖：{@link JDialog}（模态对话框父类）、{@link UserService}（业务层注册）、
 * {@link UITheme}（主色按钮样式）。</p>
 *
 * @author HealthySys 应用外壳模块
 */
public class RegisterDialog extends JDialog {

    /** 手机号输入框（作为登录账号） */
    private JTextField telField;
    /** 姓名输入框 */
    private JTextField nameField;
    /** 密码输入框 */
    private JPasswordField pwdField;
    /** 确认密码输入框（二次校验） */
    private JPasswordField confirmField;
    /** 仅患者角色单选按钮（默认选中） */
    private JRadioButton patientRadio;
    /** 双重角色（医生+患者）单选按钮 */
    private JRadioButton doctorRadio;
    /** 注册提交按钮 */
    private JButton submitButton;
    /** 取消按钮 */
    private JButton cancelButton;
    /** 用户业务服务对象（封装注册逻辑与数据库操作） */
    private UserService userService = new UserService();

    /**
     * 构造注册对话框。
     *
     * @param parent 父窗口（登录窗口），对话框居中于其父窗口之上
     */
    public RegisterDialog(JFrame parent) {
        // 模态对话框（true）：打开时阻塞父窗口
        super(parent, "新用户注册", true);
        // 初始化界面组件与布局
        initUI();
    }

    /**
     * 初始化界面布局：手机号/姓名/密码/确认密码 + 角色选择 + 按钮，并绑定事件。
     *
     * <p>使用 {@link GridBagLayout} 网格布局排列表单，底部使用 FlowLayout 排列按钮。</p>
     */
    private void initUI() {
        setSize(440, 430);                          // 对话框固定尺寸
        setLocationRelativeTo(getParent());          // 居中于父窗口
        setResizable(false);                         // 禁止调整大小

        // ---- 步骤1：主表单面板（GridBagLayout） ----
        JPanel mainPanel = new JPanel(new GridBagLayout());
        // 主面板四周留内边距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);        // 组件间距
        gbc.anchor = GridBagConstraints.WEST;        // 默认左对齐

        // 统一标签与输入框字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // ---- 手机号行 ----
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;          // 标签不拉伸
        gbc.weightx = 0;
        JLabel telLabel = new JLabel("手机号:");
        telLabel.setFont(labelFont);
        mainPanel.add(telLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;     // 输入框水平拉伸
        gbc.weightx = 1;                             // 占满剩余水平空间
        telField = new JTextField(16);
        telField.setFont(fieldFont);
        mainPanel.add(telField, gbc);

        // ---- 姓名行 ----
        gbc.gridx = 0; gbc.gridy = 1;
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

        // ---- 密码行 ----
        gbc.gridx = 0; gbc.gridy = 2;
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

        // ---- 确认密码行 ----
        gbc.gridx = 0; gbc.gridy = 3;
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

        // ---- 角色选择行 ----
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;    // 标签靠左上对齐
        JLabel roleLabel = new JLabel("角色类型:");
        roleLabel.setFont(labelFont);
        mainPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        // 角色选择面板：垂直排列两个单选按钮
        JPanel rolePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        patientRadio = new JRadioButton("仅患者角色（患者视角）");
        patientRadio.setFont(fieldFont);
        patientRadio.setSelected(true);  // 默认选中仅患者
        doctorRadio = new JRadioButton("双重角色（医生+患者，可切换视角）");
        doctorRadio.setFont(fieldFont);
        // ButtonGroup 保证两个单选按钮互斥
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(patientRadio);
        roleGroup.add(doctorRadio);
        rolePanel.add(patientRadio);
        rolePanel.add(doctorRadio);
        mainPanel.add(rolePanel, gbc);

        // 主表单面板放入对话框中央
        add(mainPanel, BorderLayout.CENTER);

        // ---- 步骤2：底部按钮面板 ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        // 注册按钮（主色实心）
        submitButton = new JButton("注册");
        submitButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitButton.setBackground(UITheme.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);          // 去除焦点框
        buttonPanel.add(submitButton);

        // 取消按钮（默认样式）
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        buttonPanel.add(cancelButton);

        // 按钮面板放入对话框南部
        add(buttonPanel, BorderLayout.SOUTH);

        // ---- 步骤3：绑定事件监听器 ----
        // 注册按钮：调用 handleRegister() 处理注册逻辑
        submitButton.addActionListener(new ActionListener() {
            /**
             * 触发注册处理。
             *
             * @param e 动作事件（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        // 取消按钮：关闭对话框
        cancelButton.addActionListener(new ActionListener() {
            /**
             * 关闭注册对话框（释放窗口资源）。
             *
             * @param e 动作事件（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                // dispose() 释放对话框窗口资源（不退出程序）
                dispose();
            }
        });
    }

    /**
     * 处理注册按钮点击：收集表单并调用业务服务注册，成功后提示并关闭。
     *
     * <p>收集手机号、姓名、密码、确认密码、角色类型 → 调用
     * {@link UserService#register(String, String, String, String, String)}
     * （内部完成密码加密与数据库插入）→ 成功提示并关闭对话框，
     * 失败则弹出警告信息。</p>
     */
    private void handleRegister() {
        // 收集表单数据（trim 去除首尾空格）
        String tel = telField.getText().trim();
        String name = nameField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        String confirm = new String(confirmField.getPassword());
        // 根据单选按钮决定角色：选中双重角色则为 doctor，否则为 patient
        String role = doctorRadio.isSelected() ? "doctor" : "patient";
        try {
            // 调用业务层注册（内部校验手机号格式、密码一致性、加密并写入数据库）
            userService.register(tel, pwd, confirm, name, role);
            // 注册成功提示
            JOptionPane.showMessageDialog(this, "注册成功，请登录！", "成功", JOptionPane.INFORMATION_MESSAGE);
            // 关闭注册对话框
            dispose();
        } catch (Exception ex) {
            // 注册失败（手机号已存在/两次密码不一致/手机号格式错误等）：显示错误信息
            JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
}
