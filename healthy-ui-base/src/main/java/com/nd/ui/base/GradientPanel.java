package com.nd.ui.base;

import javax.swing.*;
import java.awt.*;

/**
 * 渐变背景面板：绘制自上而下的垂直渐变。
 *
 * <p>本类属于 <b>healthy-ui-base（UI 基础层）</b>模块，继承 {@link JPanel}，
 * 通过重写 {@link #paintComponent(Graphics)} 使用 {@link GradientPaint}
 * 绘制垂直方向渐变背景。</p>
 *
 * <p>用于顶部横幅、主视觉等需要渐变底色的场景。典型用途：
 * {@link com.nd.ui.shell.MainView} 的顶部渐变横幅（主蓝→深蓝）。</p>
 *
 * <p>关键依赖：{@link JPanel}（父类）、{@link GradientPaint}（线性渐变填充）。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class GradientPanel extends JPanel {

    /** 渐变顶部颜色 */
    private final Color top;
    /** 渐变底部颜色 */
    private final Color bottom;

    /**
     * 构造渐变面板。
     *
     * @param top    渐变起始色（顶部）
     * @param bottom 渐变结束色（底部）
     */
    public GradientPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        // 设为不透明背景，使自绘渐变可见
        setOpaque(false);
    }

    /**
     * 自绘垂直渐变背景，再交给父类绘制子组件。
     *
     * <p>使用 {@link GradientPaint} 从面板顶部到底部做线性渐变，
     * 填充整个面板区域后调用 super 绘制子组件。</p>
     *
     * @param g 绘图上下文
     */
    @Override
    protected void paintComponent(Graphics g) {
        // 创建 Graphics2D 副本
        Graphics2D g2 = (Graphics2D) g.create();
        // 设置垂直渐变：从 (0,0) 顶部 top 色 → (0,height) 底部 bottom 色
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        // 填充整个面板为渐变色
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        // 调用父类绘制子组件（如横幅上的文字、按钮等）
        super.paintComponent(g);
    }
}
