package com.nd.ui.base;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角输入框：白色圆角底 + 浅灰描边，聚焦时描边变蓝。
 *
 * <p>用于登录/注册等需要精致输入框的场景。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class RoundedField extends JTextField {

    /** 圆角半径 */
    private final int radius;

    /**
     * 构造圆角输入框。
     *
     * @param columns 建议显示列数
     * @param radius  圆角半径
     */
    public RoundedField(int columns, int radius) {
        super(columns);
        this.radius = radius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    }

    /**
     * 自绘圆角底与描边：聚焦时描边变主蓝色。
     *
     * @param g 画布
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(hasFocus() ? UITheme.PRIMARY : new Color(203, 213, 225));
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
