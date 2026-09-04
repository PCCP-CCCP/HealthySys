package com.nd.ui.feature.profile;

import com.nd.common.entity.User;
import com.nd.common.util.Session;
import com.nd.service.UserService;
import com.nd.ui.base.RoundedPanel;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

/**
 * 个人信息管理面板（医生 / 患者通用）。
 *
 * <p>主界面两个视角都会注册该模块，用于查看与维护当前登录用户的个人资料：</p>
 * <ul>
 *   <li>个人资料：账号（只读）、姓名、出生日期、性别、身高、体重，可保存修改；</li>
 *   <li>修改密码：校验原密码后更新为新密码（密码加密存储，由业务层处理）。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（个人信息）
 */
public class ProfilePanel extends JPanel {

    /** 所属父窗口 */
    private final Window owner;
    /** 用户业务服务 */
    private final UserService userService = new UserService();

    // ===== 个人资料 =====
    /** 账号（只读）标签 */
    private JLabel telLabel;
    /** 角色（只读）标签 */
    private JLabel roleLabel;
    /** 姓名输入框 */
    private JTextField nameField;
    /** 出生日期输入框 */
    private JTextField birthDateField;
    /** 性别下拉框 */
    private JComboBox<String> genderCombo;
    /** 身高输入框 */
    private JTextField heightField;
    /** 体重输入框 */
    private JTextField weightField;

    // ===== 修改密码 =====
    /** 原密码输入框 */
    private JPasswordField oldPwdField;
    /** 新密码输入框 */
    private JPasswordField newPwdField;
    /** 确认新密码输入框 */
    private JPasswordField confirmPwdField;

    /**
     * 构造个人信息管理面板。
     *
     * @param owner 所属父窗口
     */
    public ProfilePanel(Window owner) {
        this.owner = owner;
        initUI();
        loadProfile();
    }

    /**
     * 初始化界面布局：标题栏 + 左右两个圆角区块（个人资料 / 修改密码）。
     */
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);

        // 标题栏
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setOpaque(false);
        JLabel title = new JLabel("个人信息管理");
        title.setFont(UITheme.FONT_TITLE);
        top.add(title);
        JLabel tip = new JLabel("维护个人基本资料与登录密码（密码以加密形式存储）");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tip.setForeground(UITheme.TEXT_SUB);
        top.add(tip);
        add(top, BorderLayout.NORTH);

        // 中部左右两栏
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(6, 14, 14, 14));
        center.add(buildProfileCard());
        center.add(buildPasswordCard());
        add(center, BorderLayout.CENTER);
    }

    /**
     * 构建"个人资料"区块：只读账号/角色 + 可编辑的姓名、出生日期、性别、身高、体重。
     *
     * @return 个人资料卡片面板
     */
    private JPanel buildProfileCard() {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, true);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel head = new JLabel("个人资料");
        head.setFont(UITheme.FONT_HEAD);
        head.setForeground(UITheme.PRIMARY_DK);
        card.add(head, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        telLabel = addReadonlyRow(form, gbc, 0, "账号：");
        gbc.gridy = 1;
        roleLabel = addReadonlyRow(form, gbc, 1, "角色：");

        // 姓名
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(label("姓名："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(12);
        nameField.setFont(UITheme.FONT_BODY);
        form.add(nameField, gbc);

        // 出生日期
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("出生日期："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        birthDateField = new JTextField(12);
        birthDateField.setFont(UITheme.FONT_BODY);
        form.add(birthDateField, gbc);

        // 性别
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("性别："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        genderCombo = new JComboBox<String>(new String[]{"", "男", "女"});
        genderCombo.setFont(UITheme.FONT_BODY);
        form.add(genderCombo, gbc);

        // 身高
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("身高（cm）："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        heightField = new JTextField(12);
        heightField.setFont(UITheme.FONT_BODY);
        form.add(heightField, gbc);

        // 体重
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("体重（kg）："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        weightField = new JTextField(12);
        weightField.setFont(UITheme.FONT_BODY);
        form.add(weightField, gbc);

        // 保存按钮
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 6, 2, 6);
        JButton saveBtn = UITheme.button("保存修改", UITheme.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(140, 36));
        form.add(saveBtn, gbc);
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSaveProfile();
            }
        });

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    /**
     * 构建"修改密码"区块：原密码 / 新密码 / 确认新密码。
     *
     * @return 修改密码卡片面板
     */
    private JPanel buildPasswordCard() {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, true);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel head = new JLabel("修改密码");
        head.setFont(UITheme.FONT_HEAD);
        head.setForeground(UITheme.PRIMARY_DK);
        card.add(head, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("原密码："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        oldPwdField = new JPasswordField(14);
        oldPwdField.setFont(UITheme.FONT_BODY);
        form.add(oldPwdField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("新密码："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        newPwdField = new JPasswordField(14);
        newPwdField.setFont(UITheme.FONT_BODY);
        form.add(newPwdField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("确认新密码："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        confirmPwdField = new JPasswordField(14);
        confirmPwdField.setFont(UITheme.FONT_BODY);
        form.add(confirmPwdField, gbc);

        JLabel tip = new JLabel("新密码长度不能少于 6 位");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tip.setForeground(UITheme.TEXT_SUB);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(tip, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 6, 2, 6);
        JButton changeBtn = UITheme.button("修改密码", UITheme.ACCENT);
        changeBtn.setPreferredSize(new Dimension(140, 36));
        form.add(changeBtn, gbc);
        changeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doChangePassword();
            }
        });

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    /**
     * 添加一行只读信息（账号 / 角色）。
     *
     * @param form 表单面板
     * @param gbc  布局约束
     * @param row  行号
     * @param text 字段名（含冒号）
     * @return 只读值标签
     */
    private JLabel addReadonlyRow(JPanel form, GridBagConstraints gbc, int row, String text) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label(text), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel value = new JLabel("-");
        value.setFont(UITheme.FONT_BODY);
        value.setForeground(UITheme.TEXT_SUB);
        form.add(value, gbc);
        return value;
    }

    /**
     * 创建统一样式的字段标签。
     *
     * @param text 标签文字
     * @return 标签组件
     */
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    /**
     * 加载当前登录用户资料回填表单，并同步会话姓名。
     */
    private void loadProfile() {
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            return;
        }
        try {
            User u = userService.getProfile(Session.currentTel);
            if (u == null) {
                return;
            }
            telLabel.setText(u.getTel());
            roleLabel.setText("doctor".equals(u.getRole()) ? "医生（可切换视角）" : "患者");
            nameField.setText(u.getName() == null ? "" : u.getName());
            birthDateField.setText(u.getBirthDate() == null ? "" : u.getBirthDate());
            genderCombo.setSelectedItem(u.genderText());
            heightField.setText(formatDecimal(u.getHeight()));
            weightField.setText(formatDecimal(u.getWeight()));
            Session.currentName = u.getName() == null ? "" : u.getName();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载个人信息失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 保存个人资料：调用业务层更新姓名、出生日期、性别、身高、体重。
     */
    private void doSaveProfile() {
        try {
            userService.updateProfile(
                    Session.currentTel,
                    nameField.getText().trim(),
                    birthDateField.getText().trim(),
                    (String) genderCombo.getSelectedItem(),
                    heightField.getText().trim(),
                    weightField.getText().trim());
            JOptionPane.showMessageDialog(this, "个人资料已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 修改密码：调用业务层校验原密码并更新为新密码，成功后清空密码框。
     */
    private void doChangePassword() {
        try {
            userService.changePassword(
                    Session.currentTel,
                    new String(oldPwdField.getPassword()),
                    new String(newPwdField.getPassword()),
                    new String(confirmPwdField.getPassword()));
            oldPwdField.setText("");
            newPwdField.setText("");
            confirmPwdField.setText("");
            JOptionPane.showMessageDialog(this, "密码修改成功，请牢记新密码！", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 格式化可空数字为展示文本（null 返回空串，去掉无意义尾零）。
     *
     * @param d 数值（可空）
     * @return 展示文本
     */
    private String formatDecimal(BigDecimal d) {
        if (d == null) {
            return "";
        }
        return d.stripTrailingZeros().toPlainString();
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     */
    public void onShow() {
        loadProfile();
    }
}
