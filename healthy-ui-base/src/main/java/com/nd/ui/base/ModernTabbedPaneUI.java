package com.nd.ui.base;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * 现代胶囊导航 Tab UI：选中项为蓝渐变圆角胶囊 + 白色文字，未选中为浅灰胶囊。
 *
 * <p>本类属于 <b>healthy-ui-base（UI 基础层）</b>模块，继承 {@link BasicTabbedPaneUI}，
 * 通过重写 Tab 背景、边框、文字等绘制方法，将传统选项卡改造为"胶囊导航"风格。</p>
 *
 * <p>用于主界面（{@link com.nd.ui.shell.MainView}）顶部的角色导航
 * （检查项管理/检查组管理/预约/跟踪管理等），通过
 * {@code setUI(new ModernTabbedPaneUI())} 应用到 JTabbedPane。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>选中 Tab：自上而下蓝色渐变圆角胶囊，白色文字；</li>
 *   <li>未选中 Tab：半透明浅灰胶囊，深灰文字；</li>
 *   <li>去除 Tab 边框、焦点框、内容区边框，保持胶囊悬浮感。</li>
 * </ul>
 *
 * <p>关键依赖：{@link BasicTabbedPaneUI}（父类提供 Tab 布局计算）、
 * {@link GradientPaint}（选中胶囊渐变）、{@link Graphics2D}（抗锯齿绘制）。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class ModernTabbedPaneUI extends BasicTabbedPaneUI {

    /** 选中胶囊渐变顶部色（主蓝 #3B82F6） */
    private static final Color GRAD_TOP = new Color(59, 130, 246);
    /** 选中胶囊渐变底部色（深蓝 #2563EB） */
    private static final Color GRAD_BOT = new Color(37, 99, 235);
    /** 未选中 Tab 文字色（深灰） */
    private static final Color TEXT_UNSEL = new Color(71, 85, 105);
    /** 选中 Tab 文字色（白色） */
    private static final Color TEXT_SEL = Color.WHITE;

    /**
     * Swing 插件工厂方法：UIManager 安装 TabbedPane UI 时回调。
     *
     * @param c 目标组件（JTabbedPane），本方法未使用
     * @return 新建的 ModernTabbedPaneUI 实例
     */
    public static ComponentUI createUI(JComponent c) {
        return new ModernTabbedPaneUI();
    }

    /**
     * 安装 UI 默认属性：调整 Tab 区域与内容边框的内边距，形成胶囊悬浮效果。
     *
     * <p>设置 Tab 区域上下左右留白，选中 Tab 无额外内边距，内容区无额外边框。</p>
     */
    @Override
    protected void installDefaults() {
        super.installDefaults();
        // Tab 整体区域的外边距（上10 左右14）
        tabAreaInsets = new Insets(10, 14, 0, 14);
        // 选中 Tab 额外内边距设为 0（不撑开）
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        // 内容区域边框内边距设为 0（去除默认间隙）
        contentBorderInsets = new Insets(0, 0, 0, 0);
    }

    /**
     * 每个 Tab 的水平/垂直内边距。
     *
     * @param tabPlacement Tab 位置（TOP/BOTTOM/LEFT/RIGHT）
     * @param tabIndex     Tab 下标
     * @return 每个 Tab 自身的内边距（上下9px，左右20px）
     */
    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return new Insets(9, 20, 9, 20);
    }

    /**
     * 计算 Tab 高度 = 字体高度 + 上下内边距之和。
     *
     * @param tabPlacement Tab 位置
     * @param tabIndex     Tab 下标
     * @param fontHeight   当前字体高度
     * @return Tab 总高度
     */
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        // 上下各 10px，共 20px 额外高度
        return fontHeight + 20;
    }

    /**
     * 绘制 Tab 背景：选中为蓝渐变胶囊，未选中为半透明浅灰胶囊。
     *
     * @param g          绘图上下文
     * @param tabPlacement Tab 位置
     * @param tabIndex   Tab 下标
     * @param x          Tab 区域左上角 x 坐标
     * @param y          Tab 区域左上角 y 坐标
     * @param w          Tab 宽度
     * @param h          Tab 高度
     * @param isSelected 是否为当前选中 Tab
     */
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // 创建 Graphics2D 副本用于高级绘制
        Graphics2D g2 = (Graphics2D) g.create();
        // 开启抗锯齿，使胶囊边缘平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 圆角半径等于 Tab 高度，形成完全胶囊形（两端半圆）
        int arc = h;
        if (isSelected) {
            // 选中 Tab：垂直方向从上到下蓝渐变填充
            g2.setPaint(new GradientPaint(x, y, GRAD_TOP, x, y + h, GRAD_BOT));
        } else {
            // 未选中 Tab：半透明浅灰填充
            g2.setColor(new Color(203, 213, 225, 95));
        }
        // 绘制圆角矩形作为胶囊背景
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.dispose();
    }

    /**
     * 不绘制 Tab 边框（胶囊本身即为全部外观，无需额外边框线）。
     *
     * @param g          绘图上下文（未使用）
     * @param tabPlacement Tab 位置（未使用）
     * @param tabIndex   Tab 下标（未使用）
     * @param x          左上角 x（未使用）
     * @param y          左上角 y（未使用）
     * @param w          宽（未使用）
     * @param h          高（未使用）
     * @param isSelected 是否选中（未使用）
     */
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // 无边框：空实现
    }

    /**
     * 不绘制焦点指示器（去除键盘焦点虚线框）。
     *
     * @param g          绘图上下文（未使用）
     * @param tabPlacement Tab 位置（未使用）
     * @param rects      Tab 矩形数组（未使用）
     * @param tabIndex   Tab 下标（未使用）
     * @param iconRect   图标区域（未使用）
     * @param textRect   文字区域（未使用）
     * @param isSelected 是否选中（未使用）
     */
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // 不绘制焦点框：空实现
    }

    /**
     * 不绘制内容区域边框（去除 Tab 与内容区之间的默认线条）。
     *
     * @param g            绘图上下文（未使用）
     * @param tabPlacement 位置（未使用）
     * @param selectedIndex 当前选中 Tab 下标（未使用）
     */
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // 无边框：空实现
    }

    /**
     * 居中绘制 Tab 文字，选中白色、未选中深灰。
     *
     * @param g          绘图上下文
     * @param tabPlacement Tab 位置
     * @param font       Tab 字体
     * @param metrics    字体度量（未使用，重新从 g 获取）
     * @param tabIndex   Tab 下标
     * @param title      Tab 标题文字
     * @param textRect   文字区域矩形（未使用，自行居中计算）
     * @param isSelected 是否选中
     */
    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        // 标题为空则不绘制
        if (title == null) {
            return;
        }
        // 取当前 Tab 的整体矩形区域
        Rectangle r = rects[tabIndex];
        g.setFont(font);
        // 选中用白色，未选中用深灰
        g.setColor(isSelected ? TEXT_SEL : TEXT_UNSEL);
        // 获取字体度量用于计算文字尺寸
        FontMetrics fm = g.getFontMetrics();
        // 水平居中：(Tab宽 - 文字宽) / 2 + Tab x
        int x = r.x + (r.width - fm.stringWidth(title)) / 2;
        // 垂直居中：基线位置 = Tab中心 + 字体上升量
        int y = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(title, x, y);
    }
}
