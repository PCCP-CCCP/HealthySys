package com.nd.view.utils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代按钮 UI：圆角 + 渐变 + 悬停/按下反馈，自动根据背景亮度选择文字颜色。
 * 通过 UIManager "ButtonUI" 全局应用到所有按钮。
 */
public class ModernButtonUI extends BasicButtonUI {

    private static final ModernButtonUI SHARED = new ModernButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return SHARED;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton b = (AbstractButton) c;
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel m = b.getModel();
        int w = c.getWidth(), h = c.getHeight();
        int arc = Math.min(14, h / 2);
        Color base = c.isEnabled() ? c.getBackground() : new Color(203, 213, 225);
        if (base == null || base.getAlpha() == 0) {
            base = UITheme.BTN_BG;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 底部柔影
        g2.setColor(new Color(15, 40, 100, 24));
        g2.fill(new RoundRectangle2D.Double(0, 2, w, h, arc, arc));

        // 主体渐变
        Color top, bottom;
        if (!c.isEnabled()) {
            top = base;
            bottom = darken(base, 0.06f);
        } else if (m.isPressed()) {
            top = darken(base, 0.10f);
            bottom = darken(base, 0.18f);
        } else if (m.isRollover()) {
            top = lighten(base, 0.12f);
            bottom = base;
        } else {
            top = lighten(base, 0.06f);
            bottom = darken(base, 0.04f);
        }
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));

        // 顶部细高光
        g2.setColor(new Color(255, 255, 255, 80));
        g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, arc, arc));
        g2.dispose();

        // 文字颜色
        Color fg = c.getForeground();
        Color def = UIManager.getColor("Button.foreground");
        if (fg == null || fg.equals(def)) {
            fg = luminance(base) > 0.62 ? UITheme.TEXT_MAIN : Color.WHITE;
        }
        if (!c.isEnabled()) {
            fg = new Color(148, 163, 184);
        }
        Color old = b.getForeground();
        b.setForeground(fg);
        super.paint(g, c);
        b.setForeground(old);
    }

    static Color lighten(Color c, float f) {
        return blend(c, f, 255);
    }

    static Color darken(Color c, float f) {
        return blend(c, -f, 0);
    }

    private static Color blend(Color c, float f, int target) {
        float a = Math.abs(f);
        int r = Math.max(0, Math.min(255, (int) (c.getRed() + (target - c.getRed()) * a)));
        int g = Math.max(0, Math.min(255, (int) (c.getGreen() + (target - c.getGreen()) * a)));
        int b = Math.max(0, Math.min(255, (int) (c.getBlue() + (target - c.getBlue()) * a)));
        return new Color(r, g, b, c.getAlpha());
    }

    static float luminance(Color c) {
        return (0.299f * c.getRed() + 0.587f * c.getGreen() + 0.114f * c.getBlue()) / 255f;
    }
}
