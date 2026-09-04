package com.nd.view.utils;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角密码输入框：白色圆角底 + 浅灰描边，聚焦时描边变蓝。
 */
public class RoundedPasswordField extends JPasswordField {

    private final int radius;

    public RoundedPasswordField(int columns, int radius) {
        super(columns);
        this.radius = radius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    }

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
