package com.nd.view.utils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * 现代胶囊导航 Tab UI：选中项为蓝渐变圆角胶囊 + 白色文字，未选中为浅灰胶囊。
 */
public class ModernTabbedPaneUI extends BasicTabbedPaneUI {

    private static final Color GRAD_TOP   = new Color(59, 130, 246);
    private static final Color GRAD_BOT   = new Color(37, 99, 235);
    private static final Color TEXT_UNSEL = new Color(71, 85, 105);
    private static final Color TEXT_SEL   = Color.WHITE;

    public static ComponentUI createUI(JComponent c) {
        return new ModernTabbedPaneUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabAreaInsets = new Insets(10, 14, 0, 14);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        contentBorderInsets = new Insets(0, 0, 0, 0);
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return new Insets(9, 20, 9, 20);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return fontHeight + 20;
    }

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

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // 无边框
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // 不绘制焦点框
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // 无边框
    }

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
