package com.nd.ui.feature.checkitem;

import com.nd.common.entity.CheckItem;
import com.nd.dao.CheckItemDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

/**
 * 创建/编辑检查项对话框（医生角色）。
 *
 * <p>支持两种模式：item 为 null 时创建新检查项，否则预填并编辑已有检查项；
 * 保存成功后通过 onSuccess 回调刷新所属面板。</p>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / checkitem（检查项管理）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>以模态对话框形式提供检查项的新增与编辑表单；</li>
 *   <li>表单字段包含：检查项名称、分类、价格、描述；</li>
 *   <li>客户端校验：名称必填、价格必须为非负数字；</li>
 *   <li>通过 {@link CheckItemDao#insert} 或 {@link CheckItemDao#update} 落库；</li>
 *   <li>保存成功后回调 onSuccess 刷新所属面板表格。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckItemDao}，提供 insert/update 方法；</li>
 *   <li>实体：{@link CheckItem}，封装名称/分类/价格/描述等字段；</li>
 *   <li>UI 基类：{@link JDialog}、{@link JTextField}、{@link JTextArea}、
 *       {@link JOptionPane} 等 Swing 控件；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY} 主蓝色，用于"完成"按钮配色；</li>
 *   <li>布局：{@link GridBagLayout} 实现表单标签与输入框的对齐排版。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（检查项管理）
 */
public class CreateCheckItemDialog extends JDialog {

    /** 检查项名称输入框（必填） */
    private JTextField nameField;
    /** 分类输入框（如：血常规、生化、影像等） */
    private JTextField categoryField;
    /** 价格输入框（元，允许为空，必须为非负数字） */
    private JTextField priceField;
    /** 描述多行输入框（支持自动换行） */
    private JTextArea descriptionArea;
    /** 「完成」按钮：校验表单并执行新增/修改 */
    private JButton completeButton;
    /** 「取消」按钮：关闭对话框且不保存 */
    private JButton cancelButton;
    /** 所属组件（用于定位对话框居中显示） */
    private Component owner;
    /** 保存成功回调（用于刷新所属面板表格）；可能为 null */
    private Runnable onSuccess;
    /** 正在编辑的检查项；null 表示新建模式，非 null 表示编辑模式 */
    private CheckItem editingItem;
    /** 检查项数据访问对象，执行 insert/update 数据库操作 */
    private final CheckItemDao checkItemDao = new CheckItemDao();

    /**
     * 简化的构造：新建检查项，无成功回调。
     *
     * <p>委托给完整构造器 {@link #CreateCheckItemDialog(Component, CheckItem, Runnable)}，
     * item 传 null（新建模式），onSuccess 传 null（无回调）。</p>
     *
     * @param owner 所属组件（通常是触发对话框的 JPanel 或父窗口）
     */
    public CreateCheckItemDialog(Component owner) {
        // 链式调用主构造器，item=null 表示新建，onSuccess=null 表示无回调
        this(owner, null, null);
    }

    /**
     * 编辑模式构造：传入待编辑的检查项用于预填数据。
     *
     * <p>委托给完整构造器，onSuccess 传 null（无回调）。</p>
     *
     * @param owner 所属组件
     * @param item  待编辑检查项（null 表示新建）
     */
    public CreateCheckItemDialog(Component owner, CheckItem item) {
        // 链式调用主构造器
        this(owner, item, null);
    }

    /**
     * 完整构造。
     *
     * <p>执行流程：通过 {@link SwingUtilities#getWindowAncestor(Component)} 找到 owner
     * 所在的顶层窗口作为 JDialog 的 parent；根据 item 是否为 null 决定窗口标题
     * "创建检查项" 或 "编辑检查项"；以 APPLICATION_MODAL 模态显示，阻塞父窗口。
     * 构造完成后调用 {@link #initUI()} 构建表单，若为编辑模式再调用
     * {@link #prefill(CheckItem)} 回填已有数据。</p>
     *
     * @param owner     所属组件
     * @param item      待编辑检查项（null 表示新建）
     * @param onSuccess 保存成功后的回调（用于刷新所属面板数据），可为 null
     */
    public CreateCheckItemDialog(Component owner, CheckItem item, Runnable onSuccess) {
        // 调用父类 JDialog 构造器：从 owner 向上追溯到顶层 Window 作为父窗口；
        // 根据 item 是否为空动态设置窗口标题；模态类型为 APPLICATION_MODAL，打开时阻塞父窗口
        super((Window) SwingUtilities.getWindowAncestor(owner),
                item == null ? "创建检查项" : "编辑检查项",
                ModalityType.APPLICATION_MODAL);
        // 保存外部传入的引用，供后续定位与回调使用
        this.owner = owner;
        this.onSuccess = onSuccess;
        this.editingItem = item;
        // 构建表单界面与按钮事件
        initUI();
        // 编辑模式：用已有数据回填表单字段
        if (item != null) {
            prefill(item);
        }
    }

    /**
     * 编辑模式下预填表单数据。
     *
     * <p>将 {@link CheckItem} 实体的各字段值回填到对应输入框，
     * 价格字段做 null 保护（使用 toPlainString 避免科学计数法）。</p>
     *
     * @param item 待编辑的检查项（非 null）
     */
    private void prefill(CheckItem item) {
        // 回填名称
        nameField.setText(item.getName());
        // 回填分类
        categoryField.setText(item.getCategory());
        // 价格可能为 null，判空后再填入；toPlainString 输出普通十进制字符串
        if (item.getPrice() != null) {
            priceField.setText(item.getPrice().toPlainString());
        }
        // 回填描述
        descriptionArea.setText(item.getDescription());
    }

    /**
     * 初始化界面布局与事件绑定。
     *
     * <p>整体采用 BorderLayout：CENTER 为表单区（{@link GridBagLayout} 对齐标签与输入框），
     * SOUTH 为按钮区（FlowLayout 右对齐）。表单包含名称、分类、价格、描述四行字段。
     * 同时为"完成""取消"按钮注册 ActionListener。</p>
     */
    private void initUI() {
        // 对话框固定大小 450x400，禁止用户调整尺寸（setResizable(false)）
        setSize(450, 400);
        // 相对于所属组件居中显示
        setLocationRelativeTo(owner);
        setResizable(false);

        // ---- 主面板：GridBagLayout 表单布局 ----
        // GridBagLayout 可精确控制每个组件在网格中的位置与拉伸方式
        JPanel mainPanel = new JPanel(new GridBagLayout());
        // 四周留白：上20、左20、下10、右20
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        // GridBagConstraints 控制每个组件在网格中的放置规则
        GridBagConstraints gbc = new GridBagConstraints();
        // 单元格内边距（上下8、左右5），避免组件紧贴
        gbc.insets = new Insets(8, 5, 8, 5);
        // 默认左对齐
        gbc.anchor = GridBagConstraints.WEST;

        // 统一字体：标签与输入框均使用微软雅黑 14 号
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // ---- 第一行：检查项名称 ----
        gbc.gridx = 0; // 第 0 列（标签列）
        gbc.gridy = 0; // 第 0 行
        JLabel nameLabel = new JLabel("检查项名称:");
        nameLabel.setFont(labelFont);
        mainPanel.add(nameLabel, gbc);

        // 名称输入框：第 1 列，水平拉伸并占据额外宽度
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平方向拉伸填满
        gbc.weightx = 1; // 水平方向可拉伸权重为 1
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        mainPanel.add(nameField, gbc);

        // ---- 第二行：分类 ----
        gbc.gridx = 0;
        gbc.gridy = 1;
        // 重置 fill 与 weightx：标签列不拉伸
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel categoryLabel = new JLabel("分      类:");
        categoryLabel.setFont(labelFont);
        mainPanel.add(categoryLabel, gbc);

        // 分类输入框：第 1 列，水平拉伸
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        categoryField = new JTextField(20);
        categoryField.setFont(fieldFont);
        mainPanel.add(categoryField, gbc);

        // ---- 第三行：价格 ----
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel priceLabel = new JLabel("价格(元):");
        priceLabel.setFont(labelFont);
        mainPanel.add(priceLabel, gbc);

        // 价格输入框：第 1 列，水平拉伸
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        priceField = new JTextField(20);
        priceField.setFont(fieldFont);
        mainPanel.add(priceField, gbc);

        // ---- 第四行：描述 ----
        gbc.gridx = 0;
        gbc.gridy = 3;
        // 标签改为靠左上角对齐（因为描述区较高）
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel descLabel = new JLabel("描      述:");
        descLabel.setFont(labelFont);
        mainPanel.add(descLabel, gbc);

        // 描述输入区：第 1 列，双向拉伸（水平+垂直），占据剩余空间
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1; // 垂直方向也可拉伸
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setFont(fieldFont);
        // 启用自动换行与按词换行，避免长文本横向溢出
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        // 将多行文本域放入滚动面板，内容超出时出现滚动条
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        mainPanel.add(descScroll, gbc);

        // 主面板放入对话框中央区域
        add(mainPanel, BorderLayout.CENTER);

        // ---- 底部按钮面板：FlowLayout 右对齐 ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // "完成"按钮：主色蓝底白字、粗体，固定大小 100x35
        completeButton = new JButton("完成");
        completeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        completeButton.setBackground(UITheme.PRIMARY);
        completeButton.setForeground(Color.WHITE);
        completeButton.setFocusPainted(false);
        completeButton.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(completeButton);

        // "取消"按钮：默认样式，固定大小 100x35
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        buttonPanel.add(cancelButton);

        // 按钮面板放到对话框南部
        add(buttonPanel, BorderLayout.SOUTH);

        // ---- 事件监听 ----
        // "完成"按钮：触发表单校验与保存
        completeButton.addActionListener(new ActionListener() {
            /**
             * 响应"完成"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                handleComplete();
            }
        });
        // "取消"按钮：直接关闭对话框，不保存数据
        cancelButton.addActionListener(new ActionListener() {
            /**
             * 响应"取消"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                // dispose 释放对话框资源并关闭窗口
                dispose();
            }
        });
    }

    /**
     * 处理完成按钮点击：校验输入、执行新增或修改、刷新并关闭。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>读取并 trim 各输入框内容；</li>
     *   <li>校验名称必填；</li>
     *   <li>校验价格格式（可空，非空时必须是非负数字）；</li>
     *   <li>构造 {@link CheckItem} 实体：编辑模式则回填原 ID 调用 update，新建模式调用 insert；</li>
     *   <li>根据 DAO 返回值提示成功/失败，成功则回调 onSuccess 并 dispose 关闭对话框。</li>
     * </ol>
     */
    private void handleComplete() {
        // 读取各输入框内容并去除首尾空白
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String priceStr = priceField.getText().trim();
        String description = descriptionArea.getText().trim();

        // ---- 验证必填项：名称不能为空 ----
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入检查项名称！", "提示", JOptionPane.WARNING_MESSAGE);
            // 将光标定位到名称输入框，方便用户直接输入
            nameField.requestFocus();
            return;
        }

        // ---- 验证价格格式 ----
        // price 为 null 表示用户未填写价格，允许为空
        BigDecimal price = null;
        if (!priceStr.isEmpty()) {
            try {
                // 将字符串解析为 BigDecimal，避免浮点精度问题
                price = new BigDecimal(priceStr);
                // compareTo < 0 表示价格为负数，不允许
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "价格不能为负数！", "提示", JOptionPane.WARNING_MESSAGE);
                    priceField.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                // 用户输入了非数字字符（如字母、逗号），解析失败
                JOptionPane.showMessageDialog(this, "价格格式不正确，请输入数字！", "提示", JOptionPane.WARNING_MESSAGE);
                priceField.requestFocus();
                return;
            }
        }

        // ---- 执行新增或修改 ----
        try {
            // 用表单数据构造新的 CheckItem 实体
            CheckItem item = new CheckItem(name, category, price, description);
            int result;
            String successMsg;
            if (editingItem != null) {
                // 编辑模式：把原 ID 回填到新实体，再调用 DAO.update 更新数据库记录
                item.setId(editingItem.getId());
                result = checkItemDao.update(item);
                successMsg = "检查项修改成功！";
            } else {
                // 新建模式：调用 DAO.insert 插入新记录
                result = checkItemDao.insert(item);
                successMsg = "检查项创建成功！";
            }
            // DAO 返回受影响行数，>0 表示操作成功
            if (result > 0) {
                JOptionPane.showMessageDialog(this, successMsg, "成功", JOptionPane.INFORMATION_MESSAGE);
                // 刷新所属面板数据（如父面板的表格重载）
                if (onSuccess != null) {
                    onSuccess.run();
                }
                // 关闭对话框，释放资源
                dispose();
            } else {
                // 返回 0 表示未更新/插入任何行（可能并发冲突）
                JOptionPane.showMessageDialog(this, "操作失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            // 数据库异常（约束冲突、连接失败等）统一弹窗提示
            JOptionPane.showMessageDialog(this, "操作失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
