package com.nd.ui.base;

import javax.swing.*;
import java.awt.*;

/**
 * 渐变背景面板：绘制自上而下的垂直渐变。
 *
 * <p>用于顶部横幅、主视觉等需要渐变底色的场景。</p>
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
     * @param top    顶部颜色
     * @param bottom 底部颜色
     */
    public GradientPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        setOpaque(false);
    }

    /**
     * 自绘垂直渐变背景，再交给父类绘制子组件。
     *
     * @param g 画布
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
