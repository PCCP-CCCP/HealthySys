package com.nd.ui.base;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * 现代胶囊导航 Tab UI：选中项为蓝渐变圆角胶囊 + 白色文字，未选中为浅灰胶囊。
 *
 * <p>用于主界面顶部的角色导航（检查项管理/检查组管理/预约/跟踪管理等），
 * 通过 {@code setUI(new ModernTabbedPaneUI())} 应用到 JTabbedPane。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class ModernTabbedPaneUI extends BasicTabbedPaneUI {

    /** 选中胶囊渐变顶部色 */
    private static final Color GRAD_TOP = new Color(59, 130, 246);
    /** 选中胶囊渐变底部色 */
    private static final Color GRAD_BOT = new Color(37, 99, 235);
    /** 未选中文字色 */
    private static final Color TEXT_UNSEL = new Color(71, 85, 105);
    /** 选中文字色 */
    private static final Color TEXT_SEL = Color.WHITE;

    /**
     * Swing 插件工厂方法。
     *
     * @param c 目标组件（TabbedPane）
     * @return UI 实例
     */
    public static ComponentUI createUI(JComponent c) {
        return new ModernTabbedPaneUI();
    }

    /**
     * 调整 Tab 区域与内容边框的内边距，形成胶囊悬浮效果。
     */
    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabAreaInsets = new Insets(10, 14, 0, 14);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        contentBorderInsets = new Insets(0, 0, 0, 0);
    }

    /**
     * 每个 Tab 的水平/垂直内边距。
     *
     * @param tabPlacement 位置
     * @param tabIndex     Tab 下标
     * @return 内边距
     */
    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return new Insets(9, 20, 9, 20);
    }

    /**
     * Tab 高度 = 字体高度 + 上下内边距。
     *
     * @param tabPlacement 位置
     * @param tabIndex     Tab 下标
     * @param fontHeight   字体高度
     * @return Tab 高度
     */
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return fontHeight + 20;
    }

    /**
     * 绘制 Tab 背景：选中为蓝渐变胶囊，未选中为半透明浅灰胶囊。
     *
     * @param g          画布
     * @param tabPlacement 位置
     * @param tabIndex   Tab 下标
     * @param x          左上 x
     * @param y          左上 y
     * @param w          宽
     * @param h          高
     * @param isSelected 是否选中
     */
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = h;
        if (isSelected) {
            g2.setPaint(new GradientPaint(x, y, GRAD_TOP, x, y + h, GRAD_BOT));
        } else {
            g2.setColor(new Color(203, 213, 225, 95));
        }
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.dispose();
    }

    /**
     * 不绘制 Tab 边框（胶囊本身即为全部外观）。
     */
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // 无边框
    }

    /**
     * 不绘制焦点框。
     */
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // 不绘制焦点框
    }

    /**
     * 不绘制内容区域边框。
     */
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // 无边框
    }

    /**
     * 居中绘制 Tab 文字，选中白色、未选中深灰。
     *
     * @param g          画布
     * @param tabPlacement 位置
     * @param font       字体
     * @param metrics    字体度量
     * @param tabIndex   Tab 下标
     * @param title      Tab 标题
     * @param textRect   文字区域
     * @param isSelected 是否选中
     */
    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        if (title == null) {
            return;
        }
        Rectangle r = rects[tabIndex];
        g.setFont(font);
        g.setColor(isSelected ? TEXT_SEL : TEXT_UNSEL);
        FontMetrics fm = g.getFontMetrics();
        int x = r.x + (r.width - fm.stringWidth(title)) / 2;
        int y = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(title, x, y);
    }
}
