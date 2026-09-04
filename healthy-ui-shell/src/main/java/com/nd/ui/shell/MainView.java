package com.nd.ui.shell;

import com.nd.common.util.Session;
import com.nd.ui.base.GradientPanel;
import com.nd.ui.base.ModernTabbedPaneUI;
import com.nd.ui.base.UITheme;
import com.nd.ui.feature.appointment.AppointmentPanel;
import com.nd.ui.feature.checkgroup.CheckGroupManagePanel;
import com.nd.ui.feature.checkitem.CheckItemManagePanel;
import com.nd.ui.feature.record.RecordResultPanel;
import com.nd.ui.feature.profile.ProfilePanel;
import com.nd.ui.feature.tracking.PatientResultPanel;
import com.nd.ui.feature.tracking.TrackingPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 主界面（应用外壳）：顶部渐变横幅 + 中部现代胶囊导航。
 *
 * <p>角色导航规则：</p>
 * <ul>
 *   <li>医生视角：检查项管理 / 检查组管理 / 录入结果 / 查看患者结果；</li>
 *   <li>患者视角：预约 / 跟踪管理。</li>
 * </ul>
 * 拥有医生角色的账号（{@code Session.userRole == "doctor"}）可通过右上角
 * 「一键切换角色」在两种视角间切换；仅患者角色账号不显示切换按钮。
 *
 * @author HealthySys 应用外壳模块
 */
public class MainView extends JFrame {

    /** 中部角色导航 Tab 容器 */
    private JTabbedPane tabbedPane;

    // 各功能面板（跨角色复用，按当前视角决定展示哪些）
    /** 检查项管理面板 */
    private CheckItemManagePanel checkItemPanel;
    /** 检查组管理面板 */
    private CheckGroupManagePanel checkGroupPanel;
    /** 录入结果面板 */
    private RecordResultPanel recordResultPanel;
    /** 查看患者结果面板 */
    private PatientResultPanel patientResultPanel;
    /** 预约面板 */
    private AppointmentPanel appointmentPanel;
    /** 跟踪管理面板 */
    private TrackingPanel trackingPanel;
    /** 个人信息管理面板（两个视角通用） */
    private ProfilePanel profilePanel;

    /** 头部用户信息标签 */
    private JLabel userLabel;
    /** 一键切换角色按钮 */
    private JButton switchRoleButton;

    /**
     * 构造主界面：初始化 UI 并应用当前角色视图。
     */
    public MainView() {
        initUI();
    }

    /**
     * 初始化整体布局：顶部横幅 + 各功能面板 + 按角色渲染导航。
     */
    private void initUI() {
        UITheme.apply();

        setTitle("体检中心管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);

        // ============ 顶部渐变横幅 ============
        add(buildHeader(), BorderLayout.NORTH);

        // ============ 创建各模块面板（跨角色复用） ============
        checkItemPanel = new CheckItemManagePanel(this);
        checkGroupPanel = new CheckGroupManagePanel(this);
        recordResultPanel = new RecordResultPanel(this);
        patientResultPanel = new PatientResultPanel(this);
        appointmentPanel = new AppointmentPanel(this);
        trackingPanel = new TrackingPanel(this);
        profilePanel = new ProfilePanel(this);

        // ============ 按角色渲染导航与模块 ============
        applyRoleView();

        // ============ 刷新头部角色信息 ============
        updateHeaderRoleUI();
    }

    /**
     * 构建顶部渐变横幅：徽章 + 系统名（左），用户/切换/登出（右）。
     *
     * @return 头部面板
     */
    private JPanel buildHeader() {
        GradientPanel header = new GradientPanel(UITheme.PRIMARY, UITheme.PRIMARY_DK);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // 左侧：徽章 + 标题
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel badge = new JLabel(UITheme.badgeIcon(42));
        left.add(badge);

        JPanel tt = new JPanel(new GridLayout(2, 1));
        tt.setOpaque(false);
        JLabel title = new JLabel("体检中心管理系统");
        title.setFont(new Font("微软雅黑", Font.BOLD, 21));
        title.setForeground(Color.WHITE);
        tt.add(title);
        JLabel sub = new JLabel("专业体检 · 健康相伴");
        sub.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 255, 255, 200));
        tt.add(sub);
        left.add(tt);
        header.add(left, BorderLayout.WEST);

        // 右侧：用户 / 切换 / 登出
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        userLabel = new JLabel("当前用户：" + displayName());
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(Color.WHITE);
        right.add(userLabel);

        switchRoleButton = UITheme.button("一键切换角色", null);
        switchRoleButton.setForeground(UITheme.PRIMARY_DK);
        right.add(switchRoleButton);
        switchRoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchRole();
            }
        });

        JButton logoutButton = UITheme.button("登出", UITheme.DANGER);
        right.add(logoutButton);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * 根据当前视角角色渲染 Tab 导航（医生视角 4 个模块 / 患者视角 2 个模块）。
     */
    private void applyRoleView() {
        if (tabbedPane != null) {
            getContentPane().remove(tabbedPane);
        }

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setUI(new ModernTabbedPaneUI());
        tabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tabbedPane.setBackground(UITheme.BG_MAIN);
        tabbedPane.setForeground(UITheme.TEXT_MAIN);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        if ("doctor".equals(Session.currentRole)) {
            tabbedPane.addTab("检查项管理", checkItemPanel);
            tabbedPane.addTab("检查组管理", checkGroupPanel);
            tabbedPane.addTab("录入结果", recordResultPanel);
            tabbedPane.addTab("查看患者结果", patientResultPanel);
        } else {
            tabbedPane.addTab("预约", appointmentPanel);
            tabbedPane.addTab("跟踪管理", trackingPanel);
        }
        // 个人信息管理为两个视角共用的导航模块，恒置于最后
        tabbedPane.addTab("个人信息", profilePanel);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                refreshActivePanel();
            }
        });

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().validate();
        getContentPane().repaint();
    }

    /**
     * 一键切换角色：在医生视角与患者视角之间切换并重新渲染导航。
     */
    private void switchRole() {
        if ("doctor".equals(Session.currentRole)) {
            Session.currentRole = "patient";
        } else {
            Session.currentRole = "doctor";
        }
        applyRoleView();
        updateHeaderRoleUI();
    }

    /**
     * 刷新头部用户标签与切换按钮的可见性/文案。
     */
    private void updateHeaderRoleUI() {
        boolean isDoctorAccount = "doctor".equals(Session.userRole);
        switchRoleButton.setVisible(isDoctorAccount);
        if (isDoctorAccount) {
            String viewName = "doctor".equals(Session.currentRole) ? "医生视角" : "患者视角";
            userLabel.setText("当前用户：" + displayName() + "（" + viewName + "）");
            switchRoleButton.setText("doctor".equals(Session.currentRole) ? "一键切换为患者视角" : "一键切换为医生视角");
        } else {
            userLabel.setText("当前用户：" + displayName() + "（患者）");
        }
    }

    /**
     * 头部展示用用户名：优先姓名，未设置时回退为账号。
     *
     * @return 展示用户名
     */
    private String displayName() {
        if (Session.currentName != null && !Session.currentName.isEmpty()) {
            return Session.currentName;
        }
        return Session.currentTel;
    }

    /**
     * 切换模块时刷新该模块数据（调用各面板的 onShow）。
     */
    private void refreshActivePanel() {
        if (tabbedPane == null) {
            return;
        }
        Component c = tabbedPane.getSelectedComponent();
        if (c == checkItemPanel) {
            checkItemPanel.onShow();
        } else if (c == checkGroupPanel) {
            checkGroupPanel.onShow();
        } else if (c == recordResultPanel) {
            recordResultPanel.onShow();
        } else if (c == patientResultPanel) {
            patientResultPanel.onShow();
        } else if (c == appointmentPanel) {
            appointmentPanel.onShow();
        } else if (c == trackingPanel) {
            trackingPanel.onShow();
        } else if (c == profilePanel) {
            profilePanel.onShow();
        }
    }

    /**
     * 登出：清空会话并返回登录界面。
     */
    private void logout() {
        Session.currentTel = "";
        Session.currentName = "";
        Session.userRole = "patient";
        Session.currentRole = "patient";
        dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginView.showLogin();
            }
        });
    }

    /**
     * 便捷启动入口（等价于 App.main，供 IDE 直接运行）。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainView().setVisible(true);
            }
        });
    }
}
