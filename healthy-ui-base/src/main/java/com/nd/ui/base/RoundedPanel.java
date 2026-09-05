package com.nd.ui.base;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角卡片面板：可选柔和投影，用于登录卡片、信息卡等。
 *
 * <p>本类属于 <b>healthy-ui-base（UI 基础层）</b>模块，继承 {@link JPanel}，
 * 通过重写 {@link #paintComponent(Graphics)} 自绘圆角矩形卡片外观。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>圆角矩形主体填充（颜色由构造器传入）；</li>
 *   <li>可选多层半透明投影，模拟卡片悬浮效果；</li>
 *   <li>开启抗锯齿使圆角边缘平滑。</li>
 * </ul>
 *
 * <p>典型用途：登录窗口中央的白色登录卡片（{@link com.nd.ui.shell.LoginView}）。</p>
 *
 * <p>关键依赖：{@link JPanel}（父类）、{@link Graphics2D}（抗锯齿绘制）、
 * {@link Graphics2D#fillRoundRect}（圆角矩形填充）。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class RoundedPanel extends JPanel {

    /** 圆角半径（像素），四角的圆弧大小 */
    private final int radius;
    /** 卡片填充色（如白色） */
    private final Color fill;
    /** 是否在卡片底部绘制多层柔和投影 */
    private final boolean shadow;

    /**
     * 构造圆角卡片。
     *
     * @param radius 圆角半径（像素）
     * @param fill   卡片填充色
     * @param shadow 是否绘制底部柔和投影
     */
    public RoundedPanel(int radius, Color fill, boolean shadow) {
        this.radius = radius;
        this.fill = fill;
        this.shadow = shadow;
        // 设为不透明背景，使自绘内容可见
        setOpaque(false);
    }

    /**
     * 自绘圆角卡片：先画多层半透明投影，再画主体圆角矩形。
     *
     * <p>绘制顺序：①若开启投影，循环绘制 4 层逐渐变浅的圆角矩形（向下偏移）→
     * ②绘制主体填充色圆角矩形 → ③调用父类绘制子组件。</p>
     *
     * @param g 绘图上下文
     */
    @Override
    protected void paintComponent(Graphics g) {
        // 创建 Graphics2D 副本用于高级绘制
        Graphics2D g2 = (Graphics2D) g.create();
        // 开启抗锯齿，使圆角边缘平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        // ---- 步骤1：若开启投影，绘制 4 层逐渐缩小的半透明圆角矩形形成柔和阴影 ----
        if (shadow) {
            // i 从 0 到 3，逐层向内缩小并降低透明度
            for (int i = 0; i < 4; i++) {
                // 颜色透明度从 22 递减到 7，形成渐隐效果
                g2.setColor(new Color(30, 64, 175, 22 - i * 5));
                // 每层向下偏移 i+3，左右向内缩 i，形成底部弥散阴影
                g2.fillRoundRect(i, i + 3, w - 2 * i, h - 2 * i, radius, radius);
            }
        }
        // ---- 步骤2：绘制卡片主体填充色 ----
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        g2.dispose();
        // ---- 步骤3：调用父类绘制子组件（卡片上的内容） ----
        super.paintComponent(g);
    }
}
