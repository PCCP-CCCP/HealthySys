package com.nd.ui.shell;

import com.nd.common.util.Session;
import com.nd.service.UserService;
import com.nd.ui.base.RoundedField;
import com.nd.ui.base.RoundedPanel;
import com.nd.ui.base.RoundedPasswordField;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * 登录界面（应用外壳）。
 *
 * <p>本类属于 <b>healthy-ui-shell（应用外壳层）</b>模块，负责用户登录界面的
 * 构建与登录验证流程。</p>
 *
 * <p>现代医疗风界面：柔和渐变光晕背景（{@link BgPanel}）+ 白色圆角毛玻璃卡片
 * （{@link RoundedPanel}）+ 渐变徽章 Logo（{@link UITheme#badgeIcon(int)}）+
 * 圆角输入框（{@link RoundedField}/{@link RoundedPasswordField}）+ 渐变登录按钮。</p>
 *
 * <p>登录流程：收集账号密码 → 调用 {@link UserService#login(String, String)} 验证 →
 * 写入 {@link Session}（currentTel/userRole/currentRole）→ 关闭登录窗口 → 打开
 * {@link MainView} 主界面。</p>
 *
 * <p>关键依赖：{@link UserService}（用户业务验证）、{@link Session}（登录会话状态）、
 * {@link UITheme}（全局主题）、{@link RoundedPanel}/{@link RoundedField}/
 * {@link RoundedPasswordField}（自定义控件）、{@link RegisterDialog}（注册弹窗）。</p>
 *
 * @author HealthySys 应用外壳模块
 */
public class LoginView {

    /**
     * 便捷启动入口（供 IDE 直接运行本类时使用）。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 在 EDT 上启动登录界面
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * 在 EDT 上执行 showLogin()。
             */
            public void run() {
                showLogin();
            }
        });
    }

    /**
     * 展示登录界面并返回窗口引用。
     *
     * <p>本方法完成全部登录界面构建：应用主题 → 创建主窗口 → 构建背景面板 →
     * 构建白色卡片（Logo、标题、账号密码输入框、登录按钮、注册入口）→
     * 绑定事件监听器 → 显示窗口。</p>
     *
     * @return 登录窗口 JFrame 实例
     */
    public static JFrame showLogin() {
        // ---- 步骤1：应用全局 UI 主题（必须在创建组件前调用） ----
        UITheme.apply();

        // ---- 步骤2：创建主窗口 ----
        final int W = 960, H = 600;  // 窗口宽高常量
        JFrame frame = new JFrame("体检中心管理系统 · 登录");
        // 关闭窗口即退出程序
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(W, H);
        // 窗口居中显示
        frame.setLocationRelativeTo(null);
        // 禁止调整窗口大小（固定布局）
        frame.setResizable(false);

        // ---- 步骤3：设置自绘渐变光晕背景面板 ----
        BgPanel bg = new BgPanel();
        // 空布局（绝对定位），便于卡片和装饰元素自由摆放
        bg.setLayout(null);
        frame.setContentPane(bg);

        // ============ 白色圆角卡片（登录表单容器） ============
        final int CW = 470, CH = 470;  // 卡片宽高
        // 创建白色圆角卡片（圆角26px，白色填充，开启投影）
        RoundedPanel card = new RoundedPanel(26, Color.WHITE, true);
        // 卡片内部使用 GridBagLayout 网格布局，便于垂直排列表单项
        card.setLayout(new GridBagLayout());
        // 绝对定位：卡片水平垂直居中
        card.setBounds((W - CW) / 2, (H - CH) / 2, CW, CH);
        bg.add(card);

        // GridBagLayout 布局约束对象
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;          // 起始列
        gbc.gridy = 0;          // 起始行
        gbc.gridwidth = 2;       // 默认跨 2 列
        gbc.insets = new Insets(2, 4, 2, 4);  // 组件间默认间距
        gbc.anchor = GridBagConstraints.CENTER; // 默认居中
        gbc.fill = GridBagConstraints.NONE;

        // ---- 徽章 Logo（居中） ----
        JLabel badge = new JLabel(UITheme.badgeIcon(70));  // 70px 品牌徽章
        card.add(badge, gbc);

        // ---- 系统标题 ----
        gbc.gridy = 1;
        gbc.insets = new Insets(16, 4, 2, 4);  // 上方留 16px 间距
        JLabel title = new JLabel("体检中心管理系统", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_MAIN);
        card.add(title, gbc);

        // ---- 副标语 ----
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 4, 18, 4);  // 下方留 18px 间距
        JLabel sub = new JLabel("专业体检 · 健康相伴", SwingConstants.CENTER);
        sub.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_SUB);
        card.add(sub, gbc);

        // ---- 账号输入行（标签 + 输入框，分两列） ----
        gbc.gridy = 3;
        gbc.gridwidth = 1;          // 每行恢复为 1 列
        gbc.anchor = GridBagConstraints.WEST;  // 标签左对齐
        gbc.insets = new Insets(6, 0, 4, 0);
        JLabel telLabel = new JLabel("账号");
        telLabel.setFont(UITheme.FONT_BODY);
        telLabel.setForeground(UITheme.TEXT_SUB);
        card.add(telLabel, gbc);
        // 第二列：圆角输入框
        gbc.gridx = 1;
        gbc.insets = new Insets(6, 16, 4, 0);
        RoundedField telField = new RoundedField(16, 12);  // 16列，圆角12px
        telField.setPreferredSize(new Dimension(290, 42)); // 固定高度 42px
        card.add(telField, gbc);

        // ---- 密码输入行 ----
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 4, 0);
        JLabel pwdLabel = new JLabel("密码");
        pwdLabel.setFont(UITheme.FONT_BODY);
        pwdLabel.setForeground(UITheme.TEXT_SUB);
        card.add(pwdLabel, gbc);
        // 第二列：圆角密码框
        gbc.gridx = 1;
        gbc.insets = new Insets(4, 16, 4, 0);
        RoundedPasswordField pwdField = new RoundedPasswordField(16, 12);
        pwdField.setPreferredSize(new Dimension(290, 42));
        card.add(pwdField, gbc);

        // ---- 登录按钮（全宽主色渐变按钮） ----
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;          // 跨两列
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(22, 0, 2, 0);  // 上方留 22px
        JButton loginBtn = UITheme.button("登    录", UITheme.PRIMARY);
        loginBtn.setPreferredSize(new Dimension(330, 46));  // 宽 330 高 46
        loginBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        card.add(loginBtn, gbc);

        // ---- 注册入口（一行内联：提示文字 + 注册按钮，避免重叠） ----
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 2, 0);
        // 使用 FlowLayout 水平排列提示文字与注册按钮
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        regRow.setOpaque(false);  // 透明背景，透出卡片白色
        JLabel noAccount = new JLabel("还没有账号？");
        noAccount.setForeground(UITheme.TEXT_SUB);
        regRow.add(noAccount);
        // 注册按钮（浅灰样式，文字主蓝色）
        JButton registerBtn = UITheme.button("注册新用户", null);
        registerBtn.setPreferredSize(new Dimension(110, 34));
        registerBtn.setForeground(UITheme.PRIMARY);
        regRow.add(registerBtn);
        card.add(regRow, gbc);

        // ============ 底部装饰文字 ============
        // 左下角版本号
        JLabel ver = new JLabel("v2.0 · 体检中心管理系统");
        ver.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        ver.setForeground(UITheme.TEXT_SUB);
        ver.setBounds(18, H - 34, 220, 26);
        bg.add(ver);

        // 右下角标语
        JLabel slogan = new JLabel("健康从这里开始");
        slogan.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        slogan.setForeground(UITheme.PRIMARY_DK);
        slogan.setHorizontalAlignment(SwingConstants.RIGHT);  // 右对齐
        slogan.setBounds(W - 180, H - 34, 168, 26);
        bg.add(slogan);

        // ============ 事件绑定 ============
        // 创建 UserService 业务对象用于登录验证
        final UserService userService = new UserService();
        // 登录按钮点击事件（也绑定到密码框回车）
        ActionListener loginAction = new ActionListener() {
            /**
             * 执行登录验证流程。
             *
             * @param e 动作事件（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                // 读取用户输入的账号与密码
                String tel = telField.getText();
                String pwd = new String(pwdField.getPassword());
                try {
                    // 调用业务层验证账号密码，返回角色字符串（doctor/patient）
                    String role = userService.login(tel, pwd);
                    // 登录成功：写入会话状态
                    Session.currentTel = tel;       // 当前登录账号
                    Session.userRole = role;        // 账号角色（doctor/patient）
                    Session.currentRole = role;     // 当前视角（初始与账号角色一致）
                    // 关闭登录窗口
                    frame.dispose();
                    // 打开主界面
                    new MainView().setVisible(true);
                } catch (Exception ex) {
                    // 登录失败（账号不存在/密码错误等）：弹出警告提示
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        // 绑定登录按钮点击事件
        loginBtn.addActionListener(loginAction);
        // 密码框按回车也触发登录
        pwdField.addActionListener(loginAction);

        // 注册按钮点击事件：打开注册对话框
        registerBtn.addActionListener(new ActionListener() {
            /**
             * 打开注册对话框。
             *
             * @param e 动作事件（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                // 以登录窗口为父窗口，模态打开注册对话框
                new RegisterDialog(frame).setVisible(true);
            }
        });

        // 显示登录窗口
        frame.setVisible(true);
        return frame;
    }

    /**
     * 背景面板：垂直渐变 + 柔和光晕色块（自绘）。
     *
     * <p>负责绘制登录窗口的装饰背景：顶部浅蓝→底部浅灰蓝垂直渐变、
     * 左上蓝色径向光晕、右下翠绿径向光晕、底部装饰弧线。</p>
     */
    static class BgPanel extends JPanel {
        /** 构造背景面板（空布局，便于子组件绝对定位） */
        BgPanel() {
            setLayout(null);
        }

        /**
         * 自绘渐变背景、两侧光晕与底部装饰弧线。
         *
         * <p>绘制顺序：①垂直渐变底色 → ②左上蓝色径向光晕 → ③右下翠绿径向光晕 →
         * ④底部 3 条半透明蓝色弧线装饰。</p>
         *
         * @param g 绘图上下文
         */
        @Override
        protected void paintComponent(Graphics g) {
            // 先调用父类绘制子组件（卡片、文字等）
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // 开启抗锯齿
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // ---- 步骤1：垂直渐变背景（浅蓝顶部 → 浅灰蓝底部） ----
            g2.setPaint(new GradientPaint(0, 0, new Color(226, 240, 254), 0, h, new Color(243, 247, 252)));
            g2.fillRect(0, 0, w, h);

            // ---- 步骤2：左上蓝色径向光晕（中心(150,90)，半径380） ----
            RadialGradientPaint rgp1 = new RadialGradientPaint(
                    new Point(150, 90), 380f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(96, 165, 250, 80), new Color(96, 165, 250, 0)});
            g2.setPaint(rgp1);
            g2.fillRect(0, 0, w, h);

            // ---- 步骤3：右下翠绿径向光晕（中心(w-140,h-80)，半径380） ----
            RadialGradientPaint rgp2 = new RadialGradientPaint(
                    new Point(w - 140, h - 80), 380f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(45, 212, 191, 60), new Color(45, 212, 191, 0)});
            g2.setPaint(rgp2);
            g2.fillRect(0, 0, w, h);

            // ---- 步骤4：底部装饰弧线（3 条半透明蓝色圆角弧线） ----
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(59, 130, 246, 60));
            for (int i = 0; i < 3; i++) {
                // 每条线向下偏移 5px，形成层次感
                int y = h - 8 + i * 5;
                g2.draw(new RoundRectangle2D.Double(0, y, w, 90, 30, 30));
            }
            g2.dispose();
        }
    }
}
