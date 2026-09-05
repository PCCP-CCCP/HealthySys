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
 * <p>本类属于 <b>healthy-ui-shell（应用外壳层）</b>模块，继承 {@link JFrame}，
 * 是用户登录成功后的主操作窗口。</p>
 *
 * <p>角色导航规则：</p>
 * <ul>
 *   <li><b>医生视角</b>：检查项管理 / 检查组管理 / 录入结果 / 查看患者结果；</li>
 *   <li><b>患者视角</b>：预约 / 跟踪管理。</li>
 * </ul>
 *
 * <p>拥有医生角色的账号（{@code Session.userRole == "doctor"}）可通过右上角
 * 「一键切换角色」在两种视角间切换；仅患者角色账号不显示切换按钮。
 * 个人信息面板为两个视角共用，恒置于 Tab 最后。</p>
 *
 * <p>关键依赖：{@link Session}（会话状态：账号角色/当前视角）、{@link UITheme}（主题）、
 * {@link GradientPanel}（顶部渐变横幅）、{@link ModernTabbedPaneUI}（胶囊导航）、
 * ui-feature 层各功能面板。</p>
 *
 * @author HealthySys 应用外壳模块
 */
public class MainView extends JFrame {

    /** 中部角色导航 Tab 容器（根据当前视角动态添加/移除 Tab） */
    private JTabbedPane tabbedPane;

    // ---- 各功能面板（跨角色复用，按当前视角决定展示哪些） ----
    /** 检查项管理面板（医生视角） */
    private CheckItemManagePanel checkItemPanel;
    /** 检查组管理面板（医生视角） */
    private CheckGroupManagePanel checkGroupPanel;
    /** 录入结果面板（医生视角） */
    private RecordResultPanel recordResultPanel;
    /** 查看患者结果面板（医生视角） */
    private PatientResultPanel patientResultPanel;
    /** 预约面板（患者视角） */
    private AppointmentPanel appointmentPanel;
    /** 跟踪管理面板（患者视角） */
    private TrackingPanel trackingPanel;
    /** 个人信息管理面板（两个视角通用，恒显示） */
    private ProfilePanel profilePanel;

    /** 头部用户信息标签（显示当前用户名及当前视角） */
    private JLabel userLabel;
    /** 一键切换角色按钮（仅医生账号可见） */
    private JButton switchRoleButton;

    /**
     * 构造主界面：初始化 UI 并应用当前角色视图。
     */
    public MainView() {
        initUI();
    }

    /**
     * 初始化整体布局：顶部横幅 + 各功能面板 + 按角色渲染导航。
     *
     * <p>步骤：①应用全局主题 → 设置窗口基本属性 → 添加顶部横幅 →
     * 创建各功能面板实例 → 按当前角色渲染 Tab 导航 → 刷新头部角色信息。</p>
     */
    private void initUI() {
        // 应用全局 UI 主题
        UITheme.apply();

        // 设置窗口标题与关闭行为
        setTitle("体检中心管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 700);
        // 窗口居中
        setLocationRelativeTo(null);
        // 内容区背景设为主背景色
        getContentPane().setBackground(UITheme.BG_MAIN);

        // ---- 顶部渐变横幅（构建后添加到北部） ----
        add(buildHeader(), BorderLayout.NORTH);

        // ---- 创建各功能面板实例（跨角色复用，构造时只创建一次） ----
        checkItemPanel = new CheckItemManagePanel(this);
        checkGroupPanel = new CheckGroupManagePanel(this);
        recordResultPanel = new RecordResultPanel(this);
        patientResultPanel = new PatientResultPanel(this);
        appointmentPanel = new AppointmentPanel(this);
        trackingPanel = new TrackingPanel(this);
        profilePanel = new ProfilePanel(this);

        // ---- 按当前角色渲染 Tab 导航与模块 ----
        applyRoleView();

        // ---- 刷新头部用户信息与切换按钮文案 ----
        updateHeaderRoleUI();
    }

    /**
     * 构建顶部渐变横幅：徽章 + 系统名（左），用户/切换/登出（右）。
     *
     * <p>使用 {@link GradientPanel} 实现主蓝→深蓝渐变背景；
     * 左侧放置品牌徽章 Logo 与系统名称副标题；
     * 右侧放置当前用户标签、一键切换角色按钮、登出按钮。</p>
     *
     * @return 已构建好的头部横幅面板
     */
    private JPanel buildHeader() {
        // 创建渐变背景面板（主蓝→深蓝）
        GradientPanel header = new GradientPanel(UITheme.PRIMARY, UITheme.PRIMARY_DK);
        header.setLayout(new BorderLayout());
        // 横幅四周留内边距
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // ---- 左侧：徽章 + 标题 ----
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);  // 透明背景，透出渐变
        // 品牌徽章 Logo（42px）
        JLabel badge = new JLabel(UITheme.badgeIcon(42));
        left.add(badge);

        // 标题区：垂直排列系统名 + 副标题
        JPanel tt = new JPanel(new GridLayout(2, 1));
        tt.setOpaque(false);
        JLabel title = new JLabel("体检中心管理系统");
        title.setFont(new Font("微软雅黑", Font.BOLD, 21));
        title.setForeground(Color.WHITE);
        tt.add(title);
        JLabel sub = new JLabel("专业体检 · 健康相伴");
        sub.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        // 半透明白色副标题
        sub.setForeground(new Color(255, 255, 255, 200));
        tt.add(sub);
        left.add(tt);
        header.add(left, BorderLayout.WEST);

        // ---- 右侧：用户 / 切换角色 / 登出 ----
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // 当前用户标签
        userLabel = new JLabel("当前用户：" + displayName());
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(Color.WHITE);
        right.add(userLabel);

        // 一键切换角色按钮（浅灰样式，文字深蓝）
        switchRoleButton = UITheme.button("一键切换角色", null);
        switchRoleButton.setForeground(UITheme.PRIMARY_DK);
        right.add(switchRoleButton);
        // 绑定切换角色事件
        switchRoleButton.addActionListener(new ActionListener() {
            /**
             * 触发角色切换。
             *
             * @param e 动作事件（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                switchRole();
            }
        });

        // 登出按钮（红色实心）
        JButton logoutButton = UITheme.button("登出", UITheme.DANGER);
        right.add(logoutButton);
        // 绑定登出事件
        logoutButton.addActionListener(new ActionListener() {
            /**
             * 触发登出流程。
             *
             * @param e 动作事件（未使用）
             */
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
     *
     * <p>每次切换角色时调用：先移除旧 TabbedPane，再根据
     * {@link Session#currentRole} 重新创建并添加对应功能面板，
     * 最后添加共用的个人信息面板。</p>
     */
    private void applyRoleView() {
        // 如果旧 TabbedPane 存在，先从内容区移除
        if (tabbedPane != null) {
            getContentPane().remove(tabbedPane);
        }

        // 创建新的 TabbedPane（顶部标签）
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        // 应用现代胶囊导航 UI
        tabbedPane.setUI(new ModernTabbedPaneUI());
        tabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tabbedPane.setBackground(UITheme.BG_MAIN);
        tabbedPane.setForeground(UITheme.TEXT_MAIN);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ---- 根据当前视角添加对应功能 Tab ----
        if ("doctor".equals(Session.currentRole)) {
            // 医生视角：4 个管理模块
            tabbedPane.addTab("检查项管理", checkItemPanel);
            tabbedPane.addTab("检查组管理", checkGroupPanel);
            tabbedPane.addTab("录入结果", recordResultPanel);
            tabbedPane.addTab("查看患者结果", patientResultPanel);
        } else {
            // 患者视角：2 个业务模块
            tabbedPane.addTab("预约", appointmentPanel);
            tabbedPane.addTab("跟踪管理", trackingPanel);
        }
        // 个人信息管理为两个视角共用的导航模块，恒置于最后
        tabbedPane.addTab("个人信息", profilePanel);

        // Tab 切换时刷新当前面板数据
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            /**
             * Tab 选中状态变化时触发，刷新当前激活面板。
             *
             * @param e 变化事件（未使用）
             */
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                refreshActivePanel();
            }
        });

        // 将 TabbedPane 添加到内容区中央
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        // 重新布局与绘制
        getContentPane().validate();
        getContentPane().repaint();
    }

    /**
     * 一键切换角色：在医生视角与患者视角之间切换并重新渲染导航。
     *
     * <p>仅医生账号可见此按钮（见 {@link #updateHeaderRoleUI()}）。
     * 切换时修改 {@link Session#currentRole}（当前视角），不影响
     * {@link Session#userRole}（账号固有角色）。</p>
     */
    private void switchRole() {
        // 在 doctor 与 patient 视角之间互斥切换
        if ("doctor".equals(Session.currentRole)) {
            Session.currentRole = "patient";
        } else {
            Session.currentRole = "doctor";
        }
        // 重新渲染 Tab 导航（移除旧面板，添加新视角对应的 Tab）
        applyRoleView();
        // 刷新头部用户标签与按钮文案
        updateHeaderRoleUI();
    }

    /**
     * 刷新头部用户标签与切换按钮的可见性/文案。
     *
     * <p>根据 {@link Session#userRole} 判断是否为医生账号：
     * 医生账号显示切换按钮，并在按钮文案中提示目标视角；
     * 患者账号隐藏切换按钮，标签只显示"患者"。</p>
     */
    private void updateHeaderRoleUI() {
        // 判断账号固有角色是否为医生
        boolean isDoctorAccount = "doctor".equals(Session.userRole);
        // 仅医生账号显示切换按钮
        switchRoleButton.setVisible(isDoctorAccount);
        if (isDoctorAccount) {
            // 医生账号：标签显示当前用户 + 当前视角
            String viewName = "doctor".equals(Session.currentRole) ? "医生视角" : "患者视角";
            userLabel.setText("当前用户：" + displayName() + "（" + viewName + "）");
            // 按钮文案提示切换后的目标视角
            switchRoleButton.setText("doctor".equals(Session.currentRole) ? "一键切换为患者视角" : "一键切换为医生视角");
        } else {
            // 患者账号：标签只显示"患者"，无切换按钮
            userLabel.setText("当前用户：" + displayName() + "（患者）");
        }
    }

    /**
     * 头部展示用用户名：优先姓名，未设置时回退为账号（手机号）。
     *
     * @return 展示用户名（姓名或手机号）
     */
    private String displayName() {
        // 优先使用用户姓名
        if (Session.currentName != null && !Session.currentName.isEmpty()) {
            return Session.currentName;
        }
        // 未设置姓名时回退为登录账号
        return Session.currentTel;
    }

    /**
     * 切换模块时刷新该模块数据（调用各面板的 onShow）。
     *
     * <p>当用户切换 Tab 时，获取当前选中的组件，判断其属于哪个功能面板，
     * 并调用其 {@code onShow()} 方法刷新数据（如重新查询数据库）。</p>
     */
    private void refreshActivePanel() {
        if (tabbedPane == null) {
            return;
        }
        // 获取当前选中的组件
        Component c = tabbedPane.getSelectedComponent();
        // 根据组件引用判断属于哪个面板并调用 onShow()
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
     *
     * <p>清空 {@link Session} 中的当前用户信息，角色重置为 patient；
     * 关闭主窗口；在 EDT 上重新打开登录界面。</p>
     */
    private void logout() {
        // 清空会话状态
        Session.currentTel = "";
        Session.currentName = "";
        Session.userRole = "patient";     // 重置为患者角色
        Session.currentRole = "patient"; // 重置为患者视角
        // 关闭主窗口
        dispose();
        // 在 EDT 上重新打开登录界面
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * 在 EDT 上展示登录界面。
             */
            @Override
            public void run() {
                LoginView.showLogin();
            }
        });
    }

    /**
     * 便捷启动入口（等价于 App.main，供 IDE 直接运行 MainView 时使用）。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 在 EDT 上启动主界面
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * 在 EDT 上创建并显示 MainView。
             */
            @Override
            public void run() {
                new MainView().setVisible(true);
            }
        });
    }
}
