package com.nd.ui.base;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代按钮 UI：圆角 + 渐变 + 悬停/按下反馈，自动根据背景亮度选择文字颜色。
 *
 * <p>通过 {@code UIManager.put("ButtonUI", ...)} 全局应用到所有按钮，
 * 也可直接 {@code setUI(new ModernButtonUI())} 单独使用。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class ModernButtonUI extends BasicButtonUI {

    /** 共享实例（所有按钮共用同一 UI 实例，减少对象开销） */
    private static final ModernButtonUI SHARED = new ModernButtonUI();

    /**
     * Swing 插件工厂方法：返回共享实例。
     *
     * @param c 目标组件（按钮）
     * @return UI 实例
     */
    public static ComponentUI createUI(JComponent c) {
        return SHARED;
    }

    /**
     * 安装 UI 时调整按钮为"无填充自绘"模式并关闭焦点框。
     *
     * @param c 目标组件
     */
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

    /**
     * 自绘按钮外观：底部柔影 + 渐变主体 + 顶部高光，随状态变化。
     *
     * @param g 画布
     * @param c 目标组件（按钮）
     */
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

        // 主体渐变（随悬停/按下/禁用变化）
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

        // 文字颜色：根据背景亮度自动选择深色/白色，保证可读性
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

    /**
     * 颜色变亮。
     *
     * @param c 原色
     * @param f 亮化比例（0~1）
     * @return 亮化后的颜色
     */
    static Color lighten(Color c, float f) {
        return blend(c, f, 255);
    }

    /**
     * 颜色变暗。
     *
     * @param c 原色
     * @param f 暗化比例（0~1）
     * @return 暗化后的颜色
     */
    static Color darken(Color c, float f) {
        return blend(c, -f, 0);
    }

    /**
     * 向目标值方向混合颜色通道。
     *
     * @param c      原色
     * @param f      混合比例（正=亮，负=暗）
     * @param target 目标通道值（255=白，0=黑）
     * @return 混合后的颜色
     */
    private static Color blend(Color c, float f, int target) {
        float a = Math.abs(f);
        int r = Math.max(0, Math.min(255, (int) (c.getRed() + (target - c.getRed()) * a)));
        int g = Math.max(0, Math.min(255, (int) (c.getGreen() + (target - c.getGreen()) * a)));
        int b = Math.max(0, Math.min(255, (int) (c.getBlue() + (target - c.getBlue()) * a)));
        return new Color(r, g, b, c.getAlpha());
    }

    /**
     * 计算颜色相对亮度（用于决定文字用深色还是白色）。
     *
     * @param c 颜色
     * @return 亮度值（0~1）
     */
    static float luminance(Color c) {
        return (0.299f * c.getRed() + 0.587f * c.getGreen() + 0.114f * c.getBlue()) / 255f;
    }
}
