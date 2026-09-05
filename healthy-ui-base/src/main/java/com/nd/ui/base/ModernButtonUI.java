package com.nd.ui.base;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 现代按钮 UI：圆角 + 渐变 + 悬停/按下反馈，自动根据背景亮度选择文字颜色。
 *
 * <p>本类属于 <b>healthy-ui-base（UI 基础层）</b>模块，继承 {@link BasicButtonUI}，
 * 通过自绘（{@link #paint(Graphics, JComponent)}）实现现代扁平风格按钮外观。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>圆角矩形按钮主体 + 底部柔影 + 顶部高光；</li>
 *   <li>根据按钮状态（正常/悬停/按下/禁用）自动调整渐变明暗；</li>
 *   <li>根据背景亮度自动选择深色或白色文字，保证对比度可读；</li>
 *   <li>通过 {@code UIManager.put("ButtonUI", ...)} 全局应用到所有按钮，
 *       也可直接 {@code setUI(new ModernButtonUI())} 单独使用。</li>
 * </ul>
 *
 * <p>关键依赖：{@link BasicButtonUI}（父类提供基础绘制）、{@link Graphics2D}（2D 绘图）、
 * {@link GradientPaint}（垂直渐变）、{@link RoundRectangle2D}（圆角矩形形状）、
 * {@link UITheme}（配色常量）。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class ModernButtonUI extends BasicButtonUI {

    /** 共享 UI 实例（所有按钮共用同一 UI 实例，减少对象开销；Swing PLAF 规范要求） */
    private static final ModernButtonUI SHARED = new ModernButtonUI();

    /**
     * Swing 插件工厂方法：UIManager 安装按钮 UI 时回调此方法。
     *
     * @param c 目标组件（按钮），本方法未使用该参数
     * @return 全局共享的 ModernButtonUI 实例
     */
    public static ComponentUI createUI(JComponent c) {
        return SHARED;
    }

    /**
     * 安装 UI 时调整按钮为"无填充自绘"模式并关闭焦点框。
     *
     * <p>将按钮设为透明不绘制系统默认背景，改由 {@link #paint} 自绘圆角渐变；
     * 同时关闭焦点虚线框、启用悬停效果、设置内边距。</p>
     *
     * @param c 目标组件（按钮）
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        // 转为 AbstractButton 以访问按钮模型与状态
        AbstractButton b = (AbstractButton) c;
        // 设为不透明：让自绘内容覆盖整个区域
        b.setOpaque(false);
        // 关闭 Swing 默认内容区填充，使用自绘背景
        b.setContentAreaFilled(false);
        // 不绘制焦点虚线框（现代风格不需要）
        b.setFocusPainted(false);
        // 启用悬停效果，使 m.isRollover() 可用于绘制反馈
        b.setRolloverEnabled(true);
        // 设置按钮文字与边缘的内边距（上下7px，左右16px）
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
    }

    /**
     * 自绘按钮外观：底部柔影 + 渐变主体 + 顶部高光，随状态变化。
     *
     * <p>绘制顺序：①偏移阴影 → ②垂直渐变主体 → ③顶部白色高光描边 →
     * ④按亮度自动选择文字色后委托父类绘制文字。</p>
     *
     * @param g 绘图上下文（会被转为 {@link Graphics2D}）
     * @param c 目标组件（按钮）
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        // 获取按钮状态模型（按下/悬停/选中/启用等）
        ButtonModel m = b.getModel();
        int w = c.getWidth(), h = c.getHeight();
        // 圆角半径取 14 和半高的较小值，保证按钮较矮时仍为胶囊形
        int arc = Math.min(14, h / 2);
        // 禁用态使用灰色；否则取按钮自身背景色
        Color base = c.isEnabled() ? c.getBackground() : new Color(203, 213, 225);
        // 若背景为 null 或完全透明，则回退到主题默认按钮底色
        if (base == null || base.getAlpha() == 0) {
            base = UITheme.BTN_BG;
        }

        // 创建副本 Graphics2D，避免修改原始绘图上下文
        Graphics2D g2 = (Graphics2D) g.create();
        // 开启抗锯齿，使圆角边缘平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ---- 步骤1：底部柔影（向下偏移2px的半透明深蓝圆角矩形） ----
        g2.setColor(new Color(15, 40, 100, 24));
        g2.fill(new RoundRectangle2D.Double(0, 2, w, h, arc, arc));

        // ---- 步骤2：主体渐变（根据按钮状态决定明暗方向） ----
        Color top, bottom;
        if (!c.isEnabled()) {
            // 禁用态：顶部原色，底部微暗
            top = base;
            bottom = darken(base, 0.06f);
        } else if (m.isPressed()) {
            // 按下态：整体变暗（按下下沉感）
            top = darken(base, 0.10f);
            bottom = darken(base, 0.18f);
        } else if (m.isRollover()) {
            // 悬停态：顶部提亮，底部原色（上浮感）
            top = lighten(base, 0.12f);
            bottom = base;
        } else {
            // 默认态：顶部微亮，底部微暗（轻微立体感）
            top = lighten(base, 0.06f);
            bottom = darken(base, 0.04f);
        }
        // 设置垂直方向渐变填充
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        // 绘制圆角按钮主体
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));

        // ---- 步骤3：顶部细高光（半透明白色描边，模拟光泽） ----
        g2.setColor(new Color(255, 255, 255, 80));
        g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, arc, arc));
        // 释放副本资源
        g2.dispose();

        // ---- 步骤4：自动选择文字颜色（根据背景亮度） ----
        Color fg = c.getForeground();
        // 获取 UIManager 中默认的按钮前景色
        Color def = UIManager.getColor("Button.foreground");
        // 如果未显式设置前景色，则按背景亮度自动选深色或白色
        if (fg == null || fg.equals(def)) {
            fg = luminance(base) > 0.62 ? UITheme.TEXT_MAIN : Color.WHITE;
        }
        // 禁用态文字用浅灰
        if (!c.isEnabled()) {
            fg = new Color(148, 163, 184);
        }
        // 临时修改按钮前景色后委托父类绘制文字，再还原
        Color old = b.getForeground();
        b.setForeground(fg);
        super.paint(g, c);   // 父类负责绘制按钮文字与图标
        b.setForeground(old);
    }

    /**
     * 颜色变亮：向白色方向混合。
     *
     * @param c 原色
     * @param f 亮化比例（0~1），越大越亮
     * @return 亮化后的新 Color 对象
     */
    static Color lighten(Color c, float f) {
        // 以白色(255)为目标色进行混合
        return blend(c, f, 255);
    }

    /**
     * 颜色变暗：向黑色方向混合。
     *
     * @param c 原色
     * @param f 暗化比例（0~1），越大越暗
     * @return 暗化后的新 Color 对象
     */
    static Color darken(Color c, float f) {
        // 以黑色(0)为目标色进行混合（f 传负值）
        return blend(c, -f, 0);
    }

    /**
     * 向目标值方向混合颜色各通道。
     *
     * @param c      原色
     * @param f      混合比例（正值=向 target 靠拢即亮化；负值=反向即暗化）
     * @param target 目标通道值（255=白，0=黑）
     * @return 混合后的新 Color 对象，保持原透明度
     */
    private static Color blend(Color c, float f, int target) {
        // 取绝对值作为混合强度
        float a = Math.abs(f);
        // 逐通道线性插值，并限制在 [0,255] 范围内
        int r = Math.max(0, Math.min(255, (int) (c.getRed() + (target - c.getRed()) * a)));
        int g = Math.max(0, Math.min(255, (int) (c.getGreen() + (target - c.getGreen()) * a)));
        int b = Math.max(0, Math.min(255, (int) (c.getBlue() + (target - c.getBlue()) * a)));
        // 保留原透明度
        return new Color(r, g, b, c.getAlpha());
    }

    /**
     * 计算颜色相对亮度（用于决定文字用深色还是白色）。
     *
     * <p>使用加权亮度公式（ITU-R BT.601）：Y = 0.299R + 0.587G + 0.114B。</p>
     *
     * @param c 颜色
     * @return 归一化亮度值（0=纯黑，1=纯白）
     */
    static float luminance(Color c) {
        return (0.299f * c.getRed() + 0.587f * c.getGreen() + 0.114f * c.getBlue()) / 255f;
    }
}
