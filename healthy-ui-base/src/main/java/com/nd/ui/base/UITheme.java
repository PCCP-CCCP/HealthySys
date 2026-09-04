package com.nd.ui.base;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 现代医疗 UI 主题（清新蓝配色）：统一字体、配色、圆角按钮与精致表格样式。
 *
 * <p>本类是 UI 的"唯一风格入口"，集中定义全部色值与字体；
 * {@link #apply()} 通过 UIManager 全局应用主题，需在创建界面组件前调用。</p>
 *
 * @author HealthySys UI 基础模块
 */
public final class UITheme {

    // ---- 现代蓝配色（十六进制注释便于查阅） ----
    /** 主蓝 #3B82F6 */
    public static final Color PRIMARY = new Color(59, 130, 246);
    /** 深蓝 #2563EB */
    public static final Color PRIMARY_DK = new Color(37, 99, 235);
    /** 翠绿 #10B981 */
    public static final Color ACCENT = new Color(16, 185, 129);
    /** 琥珀 #F59E0B */
    public static final Color WARN = new Color(245, 158, 11);
    /** 红 #EF4444 */
    public static final Color DANGER = new Color(239, 68, 68);
    /** 主文字石板色 #1E293B */
    public static final Color TEXT_MAIN = new Color(30, 41, 59);
    /** 次要文字 #64748B */
    public static final Color TEXT_SUB = new Color(100, 116, 139);
    /** 主背景 #F3F7FC */
    public static final Color BG_MAIN = new Color(243, 247, 252);
    /** 面板底色（纯白） */
    public static final Color BG_PANEL = new Color(255, 255, 255);
    /** 表头浅靛 #EEF2FF */
    public static final Color HEAD_BG = new Color(238, 242, 255);
    /** 表头深蓝字 */
    public static final Color HEAD_FG = new Color(29, 78, 216);
    /** 表格网格线 #EEF2F8 */
    public static final Color GRID = new Color(238, 242, 248);
    /** 选中底色 #DBEAFE */
    public static final Color SEL_BLUE = new Color(219, 234, 254);
    /** 普通按钮底色 #F1F5F9 */
    public static final Color BTN_BG = new Color(241, 245, 249);
    /** 表格斑马纹 #F8FAFC */
    public static final Color ZEBRA = new Color(248, 250, 252);

    /** 页面大标题字体 */
    public static final Font FONT_TITLE = new Font("微软雅黑", Font.BOLD, 20);
    /** 模块标题/表头字体 */
    public static final Font FONT_HEAD = new Font("微软雅黑", Font.BOLD, 14);
    /** 正文默认字体 */
    public static final Font FONT_BODY = new Font("微软雅黑", Font.PLAIN, 13);

    /** 工具类私有构造，禁止实例化 */
    private UITheme() {
    }

    /**
     * 全局应用现代主题（创建界面组件前调用，可重复调用）。
     *
     * <p>包括：统一中文渲染字体、现代圆角渐变按钮（ModernButtonUI）、
     * 表格/列表/输入框/下拉框/Tab 的配色与选中样式。</p>
     */
    public static void apply() {
        // 统一字体（中文渲染）
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("FormattedTextField.font", FONT_BODY);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Spinner.font", FONT_BODY);
        UIManager.put("List.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", new Font("微软雅黑", Font.BOLD, 13));
        UIManager.put("TabbedPane.font", FONT_BODY);
        UIManager.put("CheckBox.font", FONT_BODY);
        UIManager.put("RadioButton.font", FONT_BODY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);

        // 现代圆角渐变按钮（应用到全部按钮）
        UIManager.put("ButtonUI", ModernButtonUI.class.getName());

        UIManager.put("Panel.background", BG_MAIN);
        UIManager.put("Panel.foreground", TEXT_MAIN);
        UIManager.put("Label.foreground", TEXT_MAIN);
        UIManager.put("Label.disabledText", TEXT_SUB);

        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT_MAIN);
        UIManager.put("TextField.caretForeground", PRIMARY_DK);
        UIManager.put("TextField.selectionBackground", SEL_BLUE);
        UIManager.put("TextField.selectionForeground", TEXT_MAIN);
        UIManager.put("FormattedTextField.background", Color.WHITE);
        UIManager.put("FormattedTextField.foreground", TEXT_MAIN);
        UIManager.put("FormattedTextField.caretForeground", PRIMARY_DK);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", TEXT_MAIN);
        UIManager.put("PasswordField.caretForeground", PRIMARY_DK);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", TEXT_MAIN);
        UIManager.put("EditorPane.background", Color.WHITE);
        UIManager.put("EditorPane.foreground", TEXT_MAIN);

        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXT_MAIN);
        UIManager.put("ComboBox.selectionBackground", SEL_BLUE);
        UIManager.put("ComboBox.selectionForeground", TEXT_MAIN);
        UIManager.put("Spinner.background", Color.WHITE);
        UIManager.put("Spinner.foreground", TEXT_MAIN);

        UIManager.put("ScrollPane.background", Color.WHITE);
        UIManager.put("Viewport.background", Color.WHITE);
        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.foreground", TEXT_MAIN);
        UIManager.put("List.selectionBackground", SEL_BLUE);
        UIManager.put("List.selectionForeground", TEXT_MAIN);

        UIManager.put("Button.background", BTN_BG);
        UIManager.put("Button.foreground", TEXT_MAIN);

        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXT_MAIN);
        UIManager.put("Table.selectionBackground", SEL_BLUE);
        UIManager.put("Table.selectionForeground", new Color(30, 58, 138));
        UIManager.put("Table.gridColor", GRID);
        UIManager.put("TableHeader.background", HEAD_BG);
        UIManager.put("TableHeader.foreground", HEAD_FG);

        UIManager.put("TabbedPane.background", BG_MAIN);
        UIManager.put("TabbedPane.foreground", TEXT_MAIN);
        UIManager.put("TabbedPane.unselectedBackground", new Color(233, 238, 244));
        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.contentAreaColor", BG_MAIN);
        UIManager.put("TabbedPane.tabAreaBackground", BG_MAIN);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));

        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
        UIManager.put("Dialog.background", Color.WHITE);
        UIManager.put("InternalFrame.optionDialogBackground", Color.WHITE);
    }

    /**
     * 创建统一样式按钮。
     *
     * @param text 按钮文字
     * @param bg   背景色；为 null 时使用浅灰按钮（现代 UI 自动圆角渐变）
     * @return 样式化按钮
     */
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        if (bg != null) {
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(BTN_BG);
            b.setForeground(TEXT_MAIN);
        }
        return b;
    }

    /**
     * 加载 classpath 根下的图片资源并缩放。
     *
     * @param path 资源路径（以 / 开头，如 /medical_bg.png）
     * @param w    目标宽度
     * @param h    目标高度
     * @return 缩放后的图标；加载失败返回 null
     */
    public static ImageIcon icon(String path, int w, int h) {
        try {
            Image img = new ImageIcon(UITheme.class.getResource("/" + path)).getImage();
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 绘制品牌 Logo：圆形渐变徽章 + 白色医用十字。
     *
     * @param size 徽章边长（像素）
     * @return 徽章图标
     */
    public static ImageIcon badgeIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, PRIMARY, size, size, PRIMARY_DK));
        g.fillOval(0, 0, size, size);
        // 白色十字
        g.setColor(Color.WHITE);
        int t = Math.max(3, size / 5);
        g.fillRoundRect(size / 2 - t / 2, size / 5, t, size * 3 / 5, t, t);
        g.fillRoundRect(size / 5, size / 2 - t / 2, size * 3 / 5, t, t, t);
        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * 通用精致表格样式：浅靛表头 + 斑马纹 + 内边距，叠加在全局主题之上。
     *
     * @param t 需要美化的表格
     */
    public static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setGridColor(GRID);
        t.setSelectionBackground(SEL_BLUE);
        t.setSelectionForeground(new Color(30, 58, 138));
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = t.getTableHeader();
        h.setBackground(HEAD_BG);
        h.setForeground(HEAD_FG);
        h.setFont(new Font("微软雅黑", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 36));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(new EmptyBorder(6, 10, 6, 10));
                }
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
                }
                return c;
            }
        });
    }
}
