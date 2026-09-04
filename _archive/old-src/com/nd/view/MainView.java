package com.nd.view;

import com.nd.view.utils.GradientPanel;
import com.nd.view.utils.ModernTabbedPaneUI;
import com.nd.view.utils.Session;
import com.nd.view.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 主界面（现代医疗风）
 * 顶部渐变横幅（徽章 Logo + 系统名 + 用户/切换/登出），中部现代胶囊导航：
 * - 医生视角：检查项管理 / 检查组管理 / 录入结果 / 查看患者结果
 * - 患者视角：预约 / 跟踪管理
 * 拥有医生角色的账号可通过「一键切换角色」切换视角；仅患者账号不可切换。
 */
public class MainView extends JFrame {

    private JTabbedPane tabbedPane;

    private CheckItemManagePanel checkItemPanel;
    private CheckGroupManagePanel checkGroupPanel;
    private RecordResultPanel recordResultPanel;
    private PatientResultPanel patientResultPanel;
    private AppointmentPanel appointmentPanel;
    private TrackingPanel trackingPanel;

    private JLabel userLabel;
    private JButton switchRoleButton;

    public MainView() {
        initUI();
    }

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

        // ============ 按角色渲染导航与模块 ============
        applyRoleView();

        // ============ 刷新头部角色信息 ============
        updateHeaderRoleUI();
    }

    /**
     * 顶部渐变横幅：徽章 + 系统名 + 用户/切换/登出
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

        userLabel = new JLabel("当前用户：" + Session.currentTel);
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
     * 根据当前视角角色渲染 Tab 导航
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
     * 一键切换角色
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
     * 刷新头部用户标签与切换按钮
     */
    private void updateHeaderRoleUI() {
        boolean isDoctorAccount = "doctor".equals(Session.userRole);
        switchRoleButton.setVisible(isDoctorAccount);
        if (isDoctorAccount) {
            String viewName = "doctor".equals(Session.currentRole) ? "医生视角" : "患者视角";
            userLabel.setText("当前用户：" + Session.currentTel + "（" + viewName + "）");
            switchRoleButton.setText("doctor".equals(Session.currentRole) ? "一键切换为患者视角" : "一键切换为医生视角");
        } else {
            userLabel.setText("当前用户：" + Session.currentTel + "（患者）");
        }
    }

    /**
     * 切换模块时刷新该模块数据
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
        }
    }

    /**
     * 登出，返回登录界面
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainView().setVisible(true);
            }
        });
    }
}
