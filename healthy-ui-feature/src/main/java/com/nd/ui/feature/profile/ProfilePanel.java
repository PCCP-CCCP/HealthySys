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
 * <p><b>所属模块</b>：healthy-ui-feature / profile（个人中心）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>使用左右两个 {@link RoundedPanel} 圆角卡片分别承载"个人资料"与"修改密码"；</li>
 *   <li>个人资料区：账号/角色只读展示，姓名、出生日期、性别、身高、体重可编辑；</li>
 *   <li>修改密码区：原密码 / 新密码 / 确认新密码三段输入，业务层校验一致性与长度；</li>
 *   <li>保存资料后同步更新 {@link Session#currentName}，保证主界面顶部显示最新姓名。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>业务层：{@link UserService}（getProfile / updateProfile / changePassword）；</li>
 *   <li>实体：{@link User}，承载用户资料；</li>
 *   <li>会话：{@link Session#currentTel}、{@link Session#currentName}；</li>
 *   <li>UI 基类：{@link RoundedPanel} 圆角卡片、{@link JPasswordField} 密码框、
 *       {@link JComboBox} 性别下拉；</li>
 *   <li>UI 主题：{@link UITheme#BG_MAIN} 背景色、{@link UITheme#PRIMARY}
 *       / {@link UITheme#ACCENT} 按钮色、{@link UITheme#TEXT_MAIN} /
 *       {@link UITheme#TEXT_SUB} 文字色、各字体常量。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（个人信息）
 */
public class ProfilePanel extends JPanel {

    /** 所属父窗口（保留引用，当前未直接使用） */
    private final Window owner;
    /** 用户业务服务，封装个人资料查询/更新与密码修改逻辑 */
    private final UserService userService = new UserService();

    // ===== 个人资料区控件 =====
    /** 账号（只读）标签，展示当前登录手机号 */
    private JLabel telLabel;
    /** 角色（只读）标签，展示"医生"或"患者" */
    private JLabel roleLabel;
    /** 姓名输入框 */
    private JTextField nameField;
    /** 出生日期输入框（字符串，格式 yyyy-MM-dd） */
    private JTextField birthDateField;
    /** 性别下拉框（空串/男/女） */
    private JComboBox<String> genderCombo;
    /** 身高输入框（cm） */
    private JTextField heightField;
    /** 体重输入框（kg） */
    private JTextField weightField;

    // ===== 修改密码区控件 =====
    /** 原密码输入框（JPasswordField 隐藏明文） */
    private JPasswordField oldPwdField;
    /** 新密码输入框 */
    private JPasswordField newPwdField;
    /** 确认新密码输入框（与新密码一致性由业务层校验） */
    private JPasswordField confirmPwdField;

    /**
     * 构造个人信息管理面板。
     *
     * <p>执行流程：保存父窗口引用 → 构建双卡片界面（{@link #initUI()}）
     * → 加载当前登录用户资料回填表单（{@link #loadProfile()}）。</p>
     *
     * @param owner 所属父窗口
     */
    public ProfilePanel(Window owner) {
        this.owner = owner;
        // 构建标题栏与左右两个圆角卡片
        initUI();
        // 加载当前登录用户资料回填表单
        loadProfile();
    }

    /**
     * 初始化界面布局：标题栏 + 左右两个圆角区块（个人资料 / 修改密码）。
     *
     * <p>整体采用 BorderLayout：NORTH 为标题栏，CENTER 为 GridLayout(1,2)
     * 左右两栏，分别放置个人资料卡片与修改密码卡片。背景使用 UITheme.BG_MAIN。</p>
     */
    private void initUI() {
        setLayout(new BorderLayout());
        // 设置主背景色
        setBackground(UITheme.BG_MAIN);

        // ---- 北部：标题栏 ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        // 标题栏面板透明，透出主背景
        top.setOpaque(false);
        // 主标题"个人信息管理"
        JLabel title = new JLabel("个人信息管理");
        title.setFont(UITheme.FONT_TITLE);
        top.add(title);
        // 副标题提示
        JLabel tip = new JLabel("维护个人基本资料与登录密码（密码以加密形式存储）");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        // 副标题使用次级文字色
        tip.setForeground(UITheme.TEXT_SUB);
        top.add(tip);
        add(top, BorderLayout.NORTH);

        // ---- 中部：左右两栏卡片 ----
        // GridLayout 1 行 2 列，水平间距 18
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(6, 14, 14, 14));
        // 左侧：个人资料卡片
        center.add(buildProfileCard());
        // 右侧：修改密码卡片
        center.add(buildPasswordCard());
        add(center, BorderLayout.CENTER);
    }

    /**
     * 构建"个人资料"区块：只读账号/角色 + 可编辑的姓名、出生日期、性别、身高、体重。
     *
     * <p>使用 {@link RoundedPanel} 白色圆角卡片，内部 BorderLayout：NORTH 卡片标题，
     * CENTER 为 GridBagLayout 表单。表单包含 8 行：账号、角色（只读）、姓名、
     * 出生日期、性别、身高、体重、保存按钮。</p>
     *
     * @return 个人资料卡片面板
     */
    private JPanel buildProfileCard() {
        // 创建白色圆角卡片，圆角半径 18，带阴影
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, true);
        card.setLayout(new BorderLayout());
        // 卡片四周内边距
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // 卡片标题"个人资料"
        JLabel head = new JLabel("个人资料");
        head.setFont(UITheme.FONT_HEAD);
        head.setForeground(UITheme.PRIMARY_DK);
        card.add(head, BorderLayout.NORTH);

        // 表单区使用 GridBagLayout
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        // 单元格内边距
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // 第 0 行：账号（只读标签）
        telLabel = addReadonlyRow(form, gbc, 0, "账号：");
        // 第 1 行：角色（只读标签）
        gbc.gridy = 1;
        roleLabel = addReadonlyRow(form, gbc, 1, "角色：");

        // 第 2 行：姓名输入框
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

        // 第 3 行：出生日期输入框
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

        // 第 4 行：性别下拉框（空串/男/女）
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

        // 第 5 行：身高输入框
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

        // 第 6 行：体重输入框
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

        // 第 7 行：保存按钮（跨两列居中）
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // 跨两列
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 6, 2, 6);
        // 使用 UITheme.button 工厂方法创建主色按钮
        JButton saveBtn = UITheme.button("保存修改", UITheme.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(140, 36));
        form.add(saveBtn, gbc);
        // 保存按钮事件：调用业务层更新个人资料
        saveBtn.addActionListener(new ActionListener() {
            /**
             * 响应"保存修改"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
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
     * <p>使用 {@link RoundedPanel} 白色圆角卡片，内部 BorderLayout：NORTH 卡片标题，
     * CENTER 为 GridBagLayout 表单。表单包含 5 行：原密码、新密码、确认新密码、
     * 提示文字、修改密码按钮。</p>
     *
     * @return 修改密码卡片面板
     */
    private JPanel buildPasswordCard() {
        // 创建白色圆角卡片，圆角半径 18
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, true);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // 卡片标题"修改密码"
        JLabel head = new JLabel("修改密码");
        head.setFont(UITheme.FONT_HEAD);
        head.setForeground(UITheme.PRIMARY_DK);
        card.add(head, BorderLayout.NORTH);

        // 表单区
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // 第 0 行：原密码
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label("原密码："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // JPasswordField 输入时显示掩码，不回显明文
        oldPwdField = new JPasswordField(14);
        oldPwdField.setFont(UITheme.FONT_BODY);
        form.add(oldPwdField, gbc);

        // 第 1 行：新密码
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

        // 第 2 行：确认新密码
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

        // 第 3 行：提示文字（新密码长度要求）
        JLabel tip = new JLabel("新密码长度不能少于 6 位");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tip.setForeground(UITheme.TEXT_SUB);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(tip, gbc);

        // 第 4 行：修改密码按钮（跨两列居中，使用 ACCENT 强调色）
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 6, 2, 6);
        // 使用 UITheme.ACCENT 强调色按钮，与"保存修改"的 PRIMARY 主色区分
        JButton changeBtn = UITheme.button("修改密码", UITheme.ACCENT);
        changeBtn.setPreferredSize(new Dimension(140, 36));
        form.add(changeBtn, gbc);
        // 修改密码按钮事件：调用业务层校验并更新密码
        changeBtn.addActionListener(new ActionListener() {
            /**
             * 响应"修改密码"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
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
     * <p>左侧为字段名标签，右侧为只读值标签（灰色）。</p>
     *
     * @param form 表单面板
     * @param gbc  布局约束（方法内部会修改 gridx/gridy/weightx/fill）
     * @param row  行号
     * @param text 字段名（含冒号）
     * @return 只读值标签，供调用方后续 setText 填充
     */
    private JLabel addReadonlyRow(JPanel form, GridBagConstraints gbc, int row, String text) {
        // 第 0 列：字段名标签
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(label(text), gbc);
        // 第 1 列：只读值标签（默认显示 "-"，后续由 loadProfile 填充）
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel value = new JLabel("-");
        value.setFont(UITheme.FONT_BODY);
        // 只读值使用次级文字色，视觉上与可编辑字段区分
        value.setForeground(UITheme.TEXT_SUB);
        form.add(value, gbc);
        return value;
    }

    /**
     * 创建统一样式的字段标签。
     *
     * @param text 标签文字
     * @return 使用 UITheme.FONT_BODY 与 TEXT_MAIN 颜色的标签
     */
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    /**
     * 加载当前登录用户资料回填表单，并同步会话姓名。
     *
     * <p>流程：未登录直接返回；调用 {@link UserService#getProfile(String)}
     * 按当前手机号查询用户 → 回填账号、角色、姓名、出生日期、性别、身高、体重 →
     * 同步更新 {@link Session#currentName}。</p>
     */
    private void loadProfile() {
        // 未登录直接返回
        if (Session.currentTel == null || Session.currentTel.isEmpty()) {
            return;
        }
        try {
            // 按当前登录手机号查询用户完整资料
            User u = userService.getProfile(Session.currentTel);
            if (u == null) {
                return;
            }
            // 回填账号（手机号）
            telLabel.setText(u.getTel());
            // 回填角色：doctor 显示"医生（可切换视角）"，否则显示"患者"
            roleLabel.setText("doctor".equals(u.getRole()) ? "医生（可切换视角）" : "患者");
            // 回填姓名（null 保护）
            nameField.setText(u.getName() == null ? "" : u.getName());
            // 回填出生日期
            birthDateField.setText(u.getBirthDate() == null ? "" : u.getBirthDate());
            // 回填性别（调用实体的 genderText 方法转成中文）
            genderCombo.setSelectedItem(u.genderText());
            // 回填身高/体重（BigDecimal 经 formatDecimal 转字符串，null 返回空串）
            heightField.setText(formatDecimal(u.getHeight()));
            weightField.setText(formatDecimal(u.getWeight()));
            // 同步更新 Session 中的当前用户名，保证主界面顶部显示最新姓名
            Session.currentName = u.getName() == null ? "" : u.getName();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载个人信息失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 保存个人资料：调用业务层更新姓名、出生日期、性别、身高、体重。
     *
     * <p>所有输入字段均 trim 后传给 {@link UserService#updateProfile}，
     * 业务层负责校验（如身高/体重数字格式）。成功/失败均弹窗提示。</p>
     */
    private void doSaveProfile() {
        try {
            // 调用业务层更新个人资料
            userService.updateProfile(
                    Session.currentTel,
                    nameField.getText().trim(),
                    birthDateField.getText().trim(),
                    (String) genderCombo.getSelectedItem(),
                    heightField.getText().trim(),
                    weightField.getText().trim());
            JOptionPane.showMessageDialog(this, "个人资料已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            // 业务层抛出的校验异常（如身高非数字）直接展示消息
            JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 修改密码：调用业务层校验原密码并更新为新密码，成功后清空密码框。
     *
     * <p>流程：调用 {@link UserService#changePassword(String, String, String, String)}
     * → 成功后清空三个密码输入框 → 弹窗提示成功。
     * 业务层负责校验原密码正确性、新密码长度、两次输入一致性。</p>
     */
    private void doChangePassword() {
        try {
            // 调用业务层修改密码；getPassword() 返回 char[]，转成 String 传给业务层
            userService.changePassword(
                    Session.currentTel,
                    new String(oldPwdField.getPassword()),
                    new String(newPwdField.getPassword()),
                    new String(confirmPwdField.getPassword()));
            // 成功后清空三个密码框，避免残留
            oldPwdField.setText("");
            newPwdField.setText("");
            confirmPwdField.setText("");
            JOptionPane.showMessageDialog(this, "密码修改成功，请牢记新密码！", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            // 业务层校验失败（原密码错误、两次不一致、新密码过短等）直接展示消息
            JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 格式化可空数字为展示文本（null 返回空串，去掉无意义尾零）。
     *
     * <p>使用 {@link BigDecimal#stripTrailingZeros()} 去掉 BigDecimal 末尾的多余零，
     * 例如 175.0 → "175"，70.50 → "70.5"。</p>
     *
     * @param d 数值（可空）
     * @return 展示文本；入参为 null 时返回空串
     */
    private String formatDecimal(BigDecimal d) {
        if (d == null) {
            return "";
        }
        // stripTrailingZeros 去掉尾零，toPlainString 输出普通十进制字符串（避免科学计数法）
        return d.stripTrailingZeros().toPlainString();
    }

    /**
     * 切到本模块时刷新数据（由主界面 Tab 切换触发）。
     */
    public void onShow() {
        // 重新加载用户资料，保证从其他模块修改后回到本面板时数据最新
        loadProfile();
    }
}
