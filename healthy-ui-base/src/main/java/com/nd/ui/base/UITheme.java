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
 * <p>本类属于 <b>healthy-ui-base（UI 基础层）</b>模块，是整个 HealthySys 系统的
 * "唯一风格入口"，集中定义全部色值常量与字体常量；{@link #apply()} 通过
 * {@link UIManager#put(Object, Object)} 全局应用主题，需在创建界面组件前调用。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>集中管理全套配色（主蓝 #3B82F6、翠绿 #10B981、琥珀警告色等）与字体；</li>
 *   <li>{@link #apply()} 将上述配色/字体通过 UIManager 注册到全局 LookAndFeel；</li>
 *   <li>{@link #button(String, Color)} 工厂方法创建统一样式按钮；</li>
 *   <li>{@link #badgeIcon(int)} 程序化绘制品牌 Logo 徽章；</li>
 *   <li>{@link #styleTable(JTable)} 为 JTable 追加斑马纹、浅靛表头与内边距。</li>
 * </ul>
 *
 * <p>关键依赖：{@link UIManager}（全局 UI 属性表）、{@link ModernButtonUI}（现代按钮 UI）、
 * {@link GradientPaint}（渐变绘制）、{@link BufferedImage}（离屏绘制 Logo）。</p>
 *
 * @author HealthySys UI 基础模块
 */
public final class UITheme {

    // ---- 现代蓝配色（十六进制注释便于查阅） ----
    /** 主蓝 #3B82F6：品牌主色，用于主按钮、强调描边、选中高亮等 */
    public static final Color PRIMARY = new Color(59, 130, 246);
    /** 深蓝 #2563EB：主色加深版，用于渐变下端、标题强调文字 */
    public static final Color PRIMARY_DK = new Color(37, 99, 235);
    /** 翠绿 #10B981：成功/正向操作点缀色 */
    public static final Color ACCENT = new Color(16, 185, 129);
    /** 琥珀 #F59E0B：警告提示色 */
    public static final Color WARN = new Color(245, 158, 11);
    /** 红 #EF4444：危险/删除/登出等操作色 */
    public static final Color DANGER = new Color(239, 68, 68);
    /** 主文字石板色 #1E293B：正文标题、主要文字颜色 */
    public static final Color TEXT_MAIN = new Color(30, 41, 59);
    /** 次要文字 #64748B：标签、占位、辅助说明文字颜色 */
    public static final Color TEXT_SUB = new Color(100, 116, 139);
    /** 主背景 #F3F7FC：窗口/面板默认底色（极浅蓝白） */
    public static final Color BG_MAIN = new Color(243, 247, 252);
    /** 面板底色（纯白）：卡片、内容区白色背景 */
    public static final Color BG_PANEL = new Color(255, 255, 255);
    /** 表头浅靛 #EEF2FF：JTable 表头背景色 */
    public static final Color HEAD_BG = new Color(238, 242, 255);
    /** 表头深蓝字：JTable 表头文字色 */
    public static final Color HEAD_FG = new Color(29, 78, 216);
    /** 表格网格线 #EEF2F8：JTable 行分隔线颜色 */
    public static final Color GRID = new Color(238, 242, 248);
    /** 选中底色 #DBEAFE：列表/表格选中行浅蓝背景 */
    public static final Color SEL_BLUE = new Color(219, 234, 254);
    /** 普通按钮底色 #F1F5F9：次级按钮浅灰背景 */
    public static final Color BTN_BG = new Color(241, 245, 249);
    /** 表格斑马纹 #F8FAFC：JTable 偶数行交替背景色 */
    public static final Color ZEBRA = new Color(248, 250, 252);

    /** 页面大标题字体（微软雅黑 20号 粗体） */
    public static final Font FONT_TITLE = new Font("微软雅黑", Font.BOLD, 20);
    /** 模块标题/表头字体（微软雅黑 14号 粗体） */
    public static final Font FONT_HEAD = new Font("微软雅黑", Font.BOLD, 14);
    /** 正文默认字体（微软雅黑 13号 常规） */
    public static final Font FONT_BODY = new Font("微软雅黑", Font.PLAIN, 13);

    /**
     * 私有构造器：本类为纯静态工具类，禁止外部实例化。
     */
    private UITheme() {
    }

    /**
     * 全局应用现代主题（创建界面组件前调用，可重复调用）。
     *
     * <p>本方法通过 {@link UIManager#put(Object, Object)} 将统一字体、配色、按钮 UI
     * 注册到 Swing 全局默认表，后续所有新建组件自动继承这些设置。</p>
     *
     * <p>包括：统一中文渲染字体、现代圆角渐变按钮（{@link ModernButtonUI}）、
     * 表格/列表/输入框/下拉框/Tab 的配色与选中样式。</p>
     */
    public static void apply() {
        // ---- 步骤1：统一各类组件的字体为微软雅黑正文，保证中文渲染一致 ----
        // UIManager.put(key, value) 将键值对写入 Swing 全局默认属性表
        UIManager.put("Button.font", FONT_BODY);          // 按钮字体
        UIManager.put("Label.font", FONT_BODY);           // 标签字体
        UIManager.put("TextField.font", FONT_BODY);        // 单行输入框字体
        UIManager.put("PasswordField.font", FONT_BODY);   // 密码框字体
        UIManager.put("FormattedTextField.font", FONT_BODY); // 格式化输入框字体
        UIManager.put("TextArea.font", FONT_BODY);        // 多行文本域字体
        UIManager.put("ComboBox.font", FONT_BODY);        // 下拉框字体
        UIManager.put("Spinner.font", FONT_BODY);         // 数字步进器字体
        UIManager.put("List.font", FONT_BODY);            // 列表字体
        UIManager.put("Table.font", FONT_BODY);           // 表格内容字体
        UIManager.put("TableHeader.font", new Font("微软雅黑", Font.BOLD, 13)); // 表头字体略粗
        UIManager.put("TabbedPane.font", FONT_BODY);      // 选项卡字体
        UIManager.put("CheckBox.font", FONT_BODY);        // 复选框字体
        UIManager.put("RadioButton.font", FONT_BODY);     // 单选按钮字体
        UIManager.put("OptionPane.messageFont", FONT_BODY); // 弹窗消息字体

        // ---- 步骤2：将全局按钮 UI 替换为自定义现代圆角渐变按钮 ----
        // 指定 ButtonUI 的类名后，所有 JButton 默认使用 ModernButtonUI 渲染
        UIManager.put("ButtonUI", ModernButtonUI.class.getName());

        // ---- 步骤3：面板与标签的背景/前景色 ----
        UIManager.put("Panel.background", BG_MAIN);       // 面板默认底色
        UIManager.put("Panel.foreground", TEXT_MAIN);     // 面板默认前景色
        UIManager.put("Label.foreground", TEXT_MAIN);     // 标签文字色
        UIManager.put("Label.disabledText", TEXT_SUB);    // 标签禁用态文字色

        // ---- 步骤4：文本输入类组件配色（白底、蓝光标、浅蓝选中） ----
        UIManager.put("TextField.background", Color.WHITE);       // 输入框白底
        UIManager.put("TextField.foreground", TEXT_MAIN);          // 输入框文字色
        UIManager.put("TextField.caretForeground", PRIMARY_DK);     // 光标颜色（深蓝）
        UIManager.put("TextField.selectionBackground", SEL_BLUE);   // 选中文本背景
        UIManager.put("TextField.selectionForeground", TEXT_MAIN);  // 选中文本文字色
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

        // ---- 步骤5：下拉框与数字步进器配色 ----
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXT_MAIN);
        UIManager.put("ComboBox.selectionBackground", SEL_BLUE);
        UIManager.put("ComboBox.selectionForeground", TEXT_MAIN);
        UIManager.put("Spinner.background", Color.WHITE);
        UIManager.put("Spinner.foreground", TEXT_MAIN);

        // ---- 步骤6：滚动窗格与列表配色 ----
        UIManager.put("ScrollPane.background", Color.WHITE);
        UIManager.put("Viewport.background", Color.WHITE);
        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.foreground", TEXT_MAIN);
        UIManager.put("List.selectionBackground", SEL_BLUE);
        UIManager.put("List.selectionForeground", TEXT_MAIN);

        // ---- 步骤7：按钮默认底色/文字色（ModernButtonUI 会据此自绘渐变） ----
        UIManager.put("Button.background", BTN_BG);
        UIManager.put("Button.foreground", TEXT_MAIN);

        // ---- 步骤8：表格与表头配色 ----
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXT_MAIN);
        UIManager.put("Table.selectionBackground", SEL_BLUE);
        UIManager.put("Table.selectionForeground", new Color(30, 58, 138)); // 选中行深蓝文字
        UIManager.put("Table.gridColor", GRID);            // 表格网格线
        UIManager.put("TableHeader.background", HEAD_BG);   // 表头浅靛底
        UIManager.put("TableHeader.foreground", HEAD_FG);   // 表头深蓝字

        // ---- 步骤9：选项卡（TabbedPane）配色 ----
        UIManager.put("TabbedPane.background", BG_MAIN);
        UIManager.put("TabbedPane.foreground", TEXT_MAIN);
        UIManager.put("TabbedPane.unselectedBackground", new Color(233, 238, 244)); // 未选中浅灰
        UIManager.put("TabbedPane.selected", Color.WHITE);   // 选中 Tab 白底
        UIManager.put("TabbedPane.contentAreaColor", BG_MAIN);
        UIManager.put("TabbedPane.tabAreaBackground", BG_MAIN);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0)); // 去除焦点虚线框（透明）

        // ---- 步骤10：对话框/弹窗配色 ----
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
        UIManager.put("Dialog.background", Color.WHITE);
        UIManager.put("InternalFrame.optionDialogBackground", Color.WHITE);
    }

    /**
     * 创建统一样式按钮。
     *
     * <p>根据是否传入背景色决定按钮类型：传入主色则为实心彩色按钮（白字），
     * 传 null 则为浅灰次级按钮（深色字）。实际圆角渐变由 {@link ModernButtonUI} 完成。</p>
     *
     * @param text 按钮显示文字
     * @param bg   按钮背景色；为 null 时使用浅灰按钮（现代 UI 自动圆角渐变）
     * @return 样式化好的 JButton 实例
     */
    public static JButton button(String text, Color bg) {
        // 创建按钮并设置统一正文
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        if (bg != null) {
            // 传入背景色：实心彩色按钮，文字白色
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
        } else {
            // 未传背景色：浅灰次级按钮，文字深色
            b.setBackground(BTN_BG);
            b.setForeground(TEXT_MAIN);
        }
        return b;
    }

    /**
     * 加载 classpath 根下的图片资源并缩放为指定尺寸。
     *
     * @param path 资源路径（以 / 开头，如 /medical_bg.png），相对于 classpath 根目录
     * @param w    目标宽度（像素）
     * @param h    目标高度（像素）
     * @return 缩放后的 ImageIcon；若资源不存在或加载异常则返回 null
     */
    public static ImageIcon icon(String path, int w, int h) {
        try {
            // 通过 ClassLoader 从 classpath 加载图片
            Image img = new ImageIcon(UITheme.class.getResource("/" + path)).getImage();
            // SCALE_SMOOTH：高质量平滑缩放
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            // 资源缺失时静默返回 null，调用方自行处理
            return null;
        }
    }

    /**
     * 绘制品牌 Logo 徽章：圆形渐变徽章 + 白色医用十字。
     *
     * <p>使用 {@link BufferedImage} 离屏绘制，再封装为 {@link ImageIcon}，
     * 避免每次绘制都重复计算。</p>
     *
     * @param size 徽章边长（像素），结果为正方形
     * @return 包含渐变圆底+白色十字的 ImageIcon
     */
    public static ImageIcon badgeIcon(int size) {
        // 创建透明背景的离屏图像（ARGB 支持透明度）
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        // 获取绘图上下文
        Graphics2D g = img.createGraphics();
        // 开启抗锯齿，让圆形和圆角边缘平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置对角渐变（左上主蓝 → 右下深蓝）
        g.setPaint(new GradientPaint(0, 0, PRIMARY, size, size, PRIMARY_DK));
        // 绘制实心圆作为徽章底色
        g.fillOval(0, 0, size, size);
        // ---- 绘制白色医用十字 ----
        g.setColor(Color.WHITE);
        // t 为十字横竖条的粗细，至少 3 像素
        int t = Math.max(3, size / 5);
        // 竖条：居中水平位置，从上方 1/5 处到下方 1/5 处
        g.fillRoundRect(size / 2 - t / 2, size / 5, t, size * 3 / 5, t, t);
        // 横条：居中垂直位置，从左方 1/5 处到右方 1/5 处
        g.fillRoundRect(size / 5, size / 2 - t / 2, size * 3 / 5, t, t, t);
        // 释放绘图资源
        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * 通用精致表格样式：浅靛表头 + 斑马纹 + 内边距，叠加在全局主题之上。
     *
     * <p>在 {@link #apply()} 的全局表格属性基础上，进一步定制：
     * 行高、表头高度、表头下边框主蓝线、单元格内边距、奇偶行交替色。</p>
     *
     * @param t 需要美化的 JTable 实例
     */
    public static void styleTable(JTable t) {
        // 设置行高 34px，提升可读性
        t.setRowHeight(34);
        // 表格网格线颜色
        t.setGridColor(GRID);
        // 选中行背景与文字色
        t.setSelectionBackground(SEL_BLUE);
        t.setSelectionForeground(new Color(30, 58, 138));
        // 隐藏垂直列分隔线（仅保留行间距）
        t.setShowVerticalLines(false);
        // 设置单元格水平无间距、垂直 1px 间距
        t.setIntercellSpacing(new Dimension(0, 1));

        // ---- 定制表头外观 ----
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEAD_BG);
        h.setForeground(HEAD_FG);
        h.setFont(new Font("微软雅黑", Font.BOLD, 13));
        // 表头高度固定 36px
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 36));
        // 表头底部画 2px 主蓝色实线，作为标题与内容的分隔
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));

        // ---- 自定义单元格渲染器：斑马纹 + 内边距 ----
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            /**
             * 渲染单个表格单元格。
             *
             * @param table      所属表格
             * @param value      单元格数据值
             * @param isSelected 是否被选中
             * @param hasFocus   是否拥有焦点
             * @param row        行索引
             * @param column     列索引
             * @return 渲染后的组件（本方法返回自身）
             */
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                // 调用父类完成默认渲染（文字对齐、选中色等）
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // 给单元格添加左右 10px、上下 6px 的内边距，避免文字贴边
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(new EmptyBorder(6, 10, 6, 10));
                }
                // 未选中行使用斑马纹：偶数行白色，奇数行极浅灰
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
                }
                return c;
            }
        });
    }
}
