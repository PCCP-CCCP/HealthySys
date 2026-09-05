package com.nd.ui.feature.checkgroup;

import com.nd.common.entity.CheckGroup;
import com.nd.common.entity.CheckItem;
import com.nd.dao.CheckGroupDao;
import com.nd.dao.CheckItemDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建/修改检查组对话框（医生角色）。
 *
 * <p>输入检查组名称并勾选组内检查项，保存时通过 DAO 在事务中写入
 * 检查组及其组内关联；编辑模式会预填名称并预勾选原有检查项。</p>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / checkgroup（检查组管理）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>表单顶部为检查组名称输入框；</li>
 *   <li>中部以复选框列表展示全部可选检查项，编辑模式下预勾选原有关联项；</li>
 *   <li>保存时根据模式调用 {@link CheckGroupDao#create} 或 {@link CheckGroupDao#update}，
 *       DAO 内部在事务中同时写入检查组主表与组-项关联表；</li>
 *   <li>客户端校验：检查组名称必填。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckGroupDao}（create/update/queryGroupItemIds）、
 *       {@link CheckItemDao}（queryAll 加载全部检查项）；</li>
 *   <li>实体：{@link CheckGroup}、{@link CheckItem}；</li>
 *   <li>UI 基类：{@link JDialog}、{@link JCheckBox}、{@link BoxLayout} 等；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY} 主色用于"保存"按钮。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（检查组管理）
 */
public class CheckGroupEditDialog extends JDialog {

    /** 所属父窗口（用于对话框居中定位） */
    private Window owner;
    /** 正在编辑的检查组；null 表示新建模式，非 null 表示编辑模式 */
    private CheckGroup editingGroup;
    /** 检查组名称输入框（必填） */
    private JTextField nameField;
    /** 检查项勾选面板：垂直排列所有 JCheckBox，外层套 JScrollPane */
    private JPanel itemPanel;
    /** 全部可选检查项列表（从 checkItemDao.queryAll() 加载） */
    private List<CheckItem> allItems = new ArrayList<CheckItem>();
    /** 与 allItems 一一对应的复选框列表，保存时通过索引对应取 ID */
    private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    /** 检查组数据访问对象，执行 create/update/queryGroupItemIds */
    private final CheckGroupDao checkGroupDao = new CheckGroupDao();
    /** 检查项数据访问对象，加载全部可选检查项 */
    private final CheckItemDao checkItemDao = new CheckItemDao();

    /**
     * 构造对话框。
     *
     * <p>根据 group 是否为 null 决定窗口标题"创建检查组"或"修改检查组"，
     * 以 APPLICATION_MODAL 模态显示，然后调用 {@link #initUI()} 构建界面。</p>
     *
     * @param owner 所属父窗口（通常为主界面 JFrame）
     * @param group 待编辑检查组；null 表示新建模式
     */
    public CheckGroupEditDialog(Window owner, CheckGroup group) {
        // 调用父类构造器，动态设置标题，模态阻塞父窗口
        super(owner, group == null ? "创建检查组" : "修改检查组", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.editingGroup = group;
        // 构建名称输入区、勾选列表、按钮区，并加载检查项数据
        initUI();
    }

    /**
     * 初始化界面布局：名称输入 + 检查项勾选列表 + 按钮，并加载检查项数据。
     *
     * <p>整体采用 BorderLayout：NORTH 为名称输入行，CENTER 为带滚动条的复选框列表，
     * SOUTH 为"保存/取消"按钮。末尾调用 {@link #loadCheckItems()} 加载检查项数据。</p>
     */
    private void initUI() {
        // 对话框固定大小 540x520，禁止调整尺寸
        setSize(540, 520);
        setLocationRelativeTo(owner);
        setResizable(false);

        // ---- 北部：检查组名称输入行 ----
        // FlowLayout.LEFT 左对齐，水平间距 10、垂直间距 12
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JLabel nameLabel = new JLabel("检查组名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        namePanel.add(nameLabel);
        // 名称输入框，列宽 26
        nameField = new JTextField(26);
        nameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        namePanel.add(nameField);
        add(namePanel, BorderLayout.NORTH);

        // ---- 中部：检查项勾选列表 ----
        JPanel center = new JPanel(new BorderLayout());
        // 顶部提示文字
        JLabel tip = new JLabel("请勾选要加入组内的检查项：");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        tip.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 0));
        center.add(tip, BorderLayout.NORTH);
        // 使用 BoxLayout.Y_AXIS 让复选框垂直排列
        itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        // 外层套 JScrollPane，检查项很多时可滚动
        JScrollPane scroll = new JScrollPane(itemPanel);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---- 南部：保存/取消按钮 ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        // 保存按钮：主色蓝底白字粗体
        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        // 取消按钮：默认样式
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // ---- 事件监听 ----
        // 保存按钮：触发表单校验与落库
        saveBtn.addActionListener(new ActionListener() {
            /**
             * 响应"保存"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });
        // 取消按钮：直接关闭对话框
        cancelBtn.addActionListener(new ActionListener() {
            /**
             * 响应"取消"按钮点击事件。
             *
             * @param e 动作事件对象（未使用）
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // 界面构建完成后，从数据库加载全部检查项并生成复选框
        loadCheckItems();
    }

    /**
     * 加载全部检查项并生成复选框；编辑模式下预填名称并预勾选原有检查项。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>调用 {@link CheckItemDao#queryAll()} 拉取全部检查项到 {@link #allItems}；</li>
     *   <li>编辑模式：回填名称输入框，并调用
     *       {@link CheckGroupDao#queryGroupItemIds(Integer)} 查出原有关联项 ID 集合；</li>
     *   <li>清空旧复选框，遍历 allItems 逐个创建 JCheckBox，
     *       若 ID 在 preset 集合中则默认勾选；</li>
     *   <li>revalidate/repaint 刷新界面。</li>
     * </ol>
     */
    private void loadCheckItems() {
        try {
            // 从数据库加载全部检查项
            allItems = checkItemDao.queryAll();
        } catch (Exception e) {
            // 加载失败时弹窗提示并终止，避免后续空指针
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // preset 保存编辑模式下原有的检查项 ID 集合，用于预勾选
        List<Integer> preset = new ArrayList<Integer>();
        if (editingGroup != null) {
            // 编辑模式：回填名称
            nameField.setText(editingGroup.getName());
            try {
                // 查询该检查组原有关联的检查项 ID 列表
                preset = checkGroupDao.queryGroupItemIds(editingGroup.getId());
            } catch (Exception e) {
                // 查询失败则视为空集合，不阻断界面构建
                preset = new ArrayList<Integer>();
            }
        }
        // 清空旧复选框列表与面板，准备重建
        checkBoxes.clear();
        itemPanel.removeAll();
        for (CheckItem item : allItems) {
            // 复选框文本格式：检查项名称（分类）；分类为空时不显示括号
            String text = item.getName() + (item.getCategory() != null ? "（" + item.getCategory() + "）" : "");
            JCheckBox cb = new JCheckBox(text);
            cb.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            // 若该检查项在编辑模式的原有关联集合中，则默认勾选
            if (preset.contains(item.getId())) {
                cb.setSelected(true);
            }
            // 同时保存到 checkBoxes 列表与 UI 面板，保持索引对应
            checkBoxes.add(cb);
            itemPanel.add(cb);
        }
        // 容器内容变化后必须 revalidate+repaint 才能正确刷新布局
        itemPanel.revalidate();
        itemPanel.repaint();
    }

    /**
     * 处理保存：校验名称、收集勾选的检查项 ID，调用 DAO 创建或修改。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>校验名称非空；</li>
     *   <li>遍历 checkBoxes，收集被勾选的检查项 ID 到 itemIds；</li>
     *   <li>新建模式调用 {@link CheckGroupDao#create(String, List)}；
     *       编辑模式调用 {@link CheckGroupDao#update(Integer, String, List)}；</li>
     *   <li>根据返回值提示成功/失败，成功则 dispose 关闭对话框。</li>
     * </ol>
     */
    private void handleSave() {
        // 读取名称并去除首尾空白
        String name = nameField.getText().trim();
        // 名称必填校验
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入检查组名称！", "提示", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        // 遍历复选框，收集所有被勾选的检查项 ID
        // 利用 checkBoxes 与 allItems 的索引一一对应关系
        List<Integer> itemIds = new ArrayList<Integer>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                itemIds.add(allItems.get(i).getId());
            }
        }
        try {
            if (editingGroup == null) {
                // 新建模式：调用 DAO.create，返回新生成的检查组 ID
                int id = checkGroupDao.create(name, itemIds);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    // 成功后关闭对话框（父面板在 setVisible 返回后会自行刷新）
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "创建失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // 编辑模式：调用 DAO.update，返回受影响行数
                int result = checkGroupDao.update(editingGroup.getId(), name, itemIds);
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            // 数据库异常（约束冲突、连接错误等）统一弹窗提示
            JOptionPane.showMessageDialog(this, "保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
