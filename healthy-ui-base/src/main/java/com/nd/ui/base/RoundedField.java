package com.nd.ui.base;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角输入框：白色圆角底 + 浅灰描边，聚焦时描边变蓝。
 *
 * <p>本类属于 <b>healthy-ui-base（UI 基础层）</b>模块，继承 {@link JTextField}，
 * 通过重写 {@link #paintComponent(Graphics)} 自绘圆角白底与描边。</p>
 *
 * <p>用于登录/注册等需要精致输入框的场景。聚焦时描边变为主题主蓝色，
 * 提供明确的交互反馈。</p>
 *
 * <p>关键依赖：{@link JTextField}（父类提供文本编辑功能）、
 * {@link Graphics2D}（抗锯齿与描边）、{@link UITheme#PRIMARY}（聚焦色）。</p>
 *
 * @author HealthySys UI 基础模块
 */
public class RoundedField extends JTextField {

    /** 圆角半径（像素） */
    private final int radius;

    /**
     * 构造圆角输入框。
     *
     * @param columns 建议显示列数（用于计算首选宽度）
     * @param radius  圆角半径（像素）
     */
    public RoundedField(int columns, int radius) {
        // 调用父类构造器设置列数
        super(columns);
        this.radius = radius;
        // 设为不透明，使自绘白色背景可见
        setOpaque(false);
        // 设置内边距（上下10px，左右14px），让文字不贴边
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    }

    /**
     * 自绘圆角底与描边：聚焦时描边变主蓝色。
     *
     * <p>绘制顺序：①白色圆角填充底 → ②根据聚焦状态设置描边颜色 →
     * ③1.4px 圆角描边 → ④调用父类绘制文字与光标。</p>
     *
     * @param g 绘图上下文
     */
    @Override
    protected void paintComponent(Graphics g) {
        // 创建 Graphics2D 副本
        Graphics2D g2 = (Graphics2D) g.create();
        // 开启抗锯齿，使圆角边缘平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        // ---- 步骤1：绘制白色圆角填充底 ----
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        // ---- 步骤2：设置 1.4px 描边粗细 ----
        g2.setStroke(new BasicStroke(1.4f));
        // ---- 步骤3：聚焦时主蓝色描边，否则浅灰描边 ----
        g2.setColor(hasFocus() ? UITheme.PRIMARY : new Color(203, 213, 225));
        // 绘制圆角描边（w-1, h-1 确保描边不被裁切）
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        g2.dispose();
        // ---- 步骤4：调用父类绘制文字、光标等 ----
        super.paintComponent(g);
    }
}
