package com.nd.ui.base;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角卡片面板：可选柔和投影，用于登录卡片、信息卡等。
 *
 * @author HealthySys UI 基础模块
 */
public class RoundedPanel extends JPanel {

    /** 圆角半径 */
    private final int radius;
    /** 卡片填充色 */
    private final Color fill;
    /** 是否绘制底部柔和投影 */
    private final boolean shadow;

    /**
     * 构造圆角卡片。
     *
     * @param radius 圆角半径
     * @param fill   填充色
     * @param shadow 是否绘制投影
     */
    public RoundedPanel(int radius, Color fill, boolean shadow) {
        this.radius = radius;
        this.fill = fill;
        this.shadow = shadow;
        setOpaque(false);
    }

    /**
     * 自绘圆角卡片：先画多层半透明投影，再画主体圆角矩形。
     *
     * @param g 画布
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (shadow) {
            for (int i = 0; i < 4; i++) {
                g2.setColor(new Color(30, 64, 175, 22 - i * 5));
                g2.fillRoundRect(i, i + 3, w - 2 * i, h - 2 * i, radius, radius);
            }
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
