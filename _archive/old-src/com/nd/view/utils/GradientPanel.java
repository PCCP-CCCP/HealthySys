package com.nd.view.utils;

import javax.swing.*;
import java.awt.*;

/**
 * 渐变背景面板：垂直渐变，用于顶部横幅等。
 */
public class GradientPanel extends JPanel {

    private final Color top;
    private final Color bottom;

    public GradientPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
