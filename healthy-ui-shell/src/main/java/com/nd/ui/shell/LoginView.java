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
 * <p>现代医疗风：柔和渐变光晕背景 + 白色圆角毛玻璃卡片 + 渐变徽章 Logo +
 * 圆角输入框 + 渐变登录按钮。登录成功后写入 {@link Session} 并进入主界面。</p>
 *
 * @author HealthySys 应用外壳模块
 */
public class LoginView {

    /**
     * 便捷启动入口。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showLogin();
            }
        });
    }

    /**
     * 展示登录界面并返回窗口引用。
     *
     * @return 登录窗口
     */
    public static JFrame showLogin() {
        UITheme.apply();

        final int W = 960, H = 600;
        JFrame frame = new JFrame("体检中心管理系统 · 登录");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(W, H);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // 背景面板：柔和渐变 + 光晕
        BgPanel bg = new BgPanel();
        bg.setLayout(null);
        frame.setContentPane(bg);

        // ============ 白色圆角卡片 ============
        final int CW = 470, CH = 470;
        RoundedPanel card = new RoundedPanel(26, Color.WHITE, true);
        card.setLayout(new GridBagLayout());
        card.setBounds((W - CW) / 2, (H - CH) / 2, CW, CH);
        bg.add(card);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // 渐变徽章 Logo
        JLabel badge = new JLabel(UITheme.badgeIcon(70));
        card.add(badge, gbc);

        // 标题
        gbc.gridy = 1;
        gbc.insets = new Insets(16, 4, 2, 4);
        JLabel title = new JLabel("体检中心管理系统", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_MAIN);
        card.add(title, gbc);

        // 副标语
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 4, 18, 4);
        JLabel sub = new JLabel("专业体检 · 健康相伴", SwingConstants.CENTER);
        sub.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_SUB);
        card.add(sub, gbc);

        // 账号
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 4, 0);
        JLabel telLabel = new JLabel("账号");
        telLabel.setFont(UITheme.FONT_BODY);
        telLabel.setForeground(UITheme.TEXT_SUB);
        card.add(telLabel, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(6, 16, 4, 0);
        RoundedField telField = new RoundedField(16, 12);
        telField.setPreferredSize(new Dimension(290, 42));
        card.add(telField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 4, 0);
        JLabel pwdLabel = new JLabel("密码");
        pwdLabel.setFont(UITheme.FONT_BODY);
        pwdLabel.setForeground(UITheme.TEXT_SUB);
        card.add(pwdLabel, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(4, 16, 4, 0);
        RoundedPasswordField pwdField = new RoundedPasswordField(16, 12);
        pwdField.setPreferredSize(new Dimension(290, 42));
        card.add(pwdField, gbc);

        // 登录按钮（全宽渐变）
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(22, 0, 2, 0);
        JButton loginBtn = UITheme.button("登    录", UITheme.PRIMARY);
        loginBtn.setPreferredSize(new Dimension(330, 46));
        loginBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        card.add(loginBtn, gbc);

        // 注册入口（一行内联：提示文字 + 注册按钮，避免重叠）
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 2, 0);
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        regRow.setOpaque(false);
        JLabel noAccount = new JLabel("还没有账号？");
        noAccount.setForeground(UITheme.TEXT_SUB);
        regRow.add(noAccount);
        JButton registerBtn = UITheme.button("注册新用户", null);
        registerBtn.setPreferredSize(new Dimension(110, 34));
        registerBtn.setForeground(UITheme.PRIMARY);
        regRow.add(registerBtn);
        card.add(regRow, gbc);

        // ============ 底部装饰 ============
        JLabel ver = new JLabel("v2.0 · 体检中心管理系统");
        ver.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        ver.setForeground(UITheme.TEXT_SUB);
        ver.setBounds(18, H - 34, 220, 26);
        bg.add(ver);

        JLabel slogan = new JLabel("健康从这里开始");
        slogan.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        slogan.setForeground(UITheme.PRIMARY_DK);
        slogan.setHorizontalAlignment(SwingConstants.RIGHT);
        slogan.setBounds(W - 180, H - 34, 168, 26);
        bg.add(slogan);

        // ============ 事件 ============
        final UserService userService = new UserService();
        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tel = telField.getText();
                String pwd = new String(pwdField.getPassword());
                try {
                    String role = userService.login(tel, pwd);
                    Session.currentTel = tel;
                    Session.userRole = role;
                    Session.currentRole = role;
                    frame.dispose();
                    new MainView().setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        loginBtn.addActionListener(loginAction);
        pwdField.addActionListener(loginAction);

        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterDialog(frame).setVisible(true);
            }
        });

        frame.setVisible(true);
        return frame;
    }

    /**
     * 背景面板：垂直渐变 + 柔和光晕色块（自绘）。
     */
    static class BgPanel extends JPanel {
        /** 构造背景面板（空布局） */
        BgPanel() {
            setLayout(null);
        }

        /**
         * 自绘渐变背景、两侧光晕与底部装饰弧线。
         *
         * @param g 画布
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // 垂直渐变背景
            g2.setPaint(new GradientPaint(0, 0, new Color(226, 240, 254), 0, h, new Color(243, 247, 252)));
            g2.fillRect(0, 0, w, h);

            // 左上蓝色光晕
            RadialGradientPaint rgp1 = new RadialGradientPaint(
                    new Point(150, 90), 380f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(96, 165, 250, 80), new Color(96, 165, 250, 0)});
            g2.setPaint(rgp1);
            g2.fillRect(0, 0, w, h);

            // 右下翠绿光晕
            RadialGradientPaint rgp2 = new RadialGradientPaint(
                    new Point(w - 140, h - 80), 380f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(45, 212, 191, 60), new Color(45, 212, 191, 0)});
            g2.setPaint(rgp2);
            g2.fillRect(0, 0, w, h);

            // 底部装饰弧线
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(59, 130, 246, 60));
            for (int i = 0; i < 3; i++) {
                int y = h - 8 + i * 5;
                g2.draw(new RoundRectangle2D.Double(0, y, w, 90, 30, 30));
            }
            g2.dispose();
        }
    }
}
