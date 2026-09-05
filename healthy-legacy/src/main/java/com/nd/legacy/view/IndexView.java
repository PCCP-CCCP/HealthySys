package com.nd.legacy.view;

import com.nd.legacy.bean.CheckItem;
import com.nd.legacy.service.CheckItemService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * 【遗留模块】旧版主界面视图（Swing 桌面应用）。
 *
 * <p>所属模块：healthy-legacy（遗留模块，包含早期版本的代码，与新模块 healthy-common 等相互独立）。</p>
 *
 * <p>类的职责：基于 Java Swing 构建旧版检查项管理主界面窗口，展示检查项数据表格。
 * 已被新版 {@code com.nd.ui.shell.MainView}（多模块、现代 UI）取代，此处仅保留供历史参考。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>创建并配置 JFrame 主窗口（标题、大小、位置、关闭行为）；</li>
 *   <li>创建带标题边框的 JPanel 面板，用于承载检查项管理区域；</li>
 *   <li>创建 JTable 表格组件，展示检查项 ID、编号、名称、单位、参考范围等列；</li>
 *   <li>隐藏 ID 列以避免暴露内部主键；</li>
 *   <li>调用 Service 层从数据库加载检查项数据并填充到表格中。</li>
 * </ul>
 *
 * <p>关键依赖：</p>
 * <ul>
 *   <li>{@link CheckItemService}：遗留模块的检查项业务服务，提供数据查询接口；</li>
 *   <li>{@link CheckItem}：遗留模块的检查项实体类。</li>
 * </ul>
 *
 * <p>技术栈：Java Swing（AWT/Swing 组件）+ JDBC 数据库访问。</p>
 *
 * <p>注意：本类未被当前主程序引用，仅保留参考。</p>
 *
 * @author HealthySys 遗留模块
 */
public class IndexView {

    /**
     * 旧版检查项业务服务实例，用于从数据库查询检查项数据并填充到表格中。
     * 声明为 final 表示初始化后不再重新赋值。
     */
    private final CheckItemService checkItemService = new CheckItemService();

    /**
     * 检查项数据表格组件（JTable），用于以表格形式展示检查项列表数据。
     * 初始为 null，在构造函数中初始化。
     */
    private JTable jTable = null;

    /**
     * 构造函数：创建旧版检查项管理主窗口并加载表格数据。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>创建 JFrame 主窗口，设置标题为"主页"；</li>
     *   <li>配置窗口关闭行为、位置和大小；</li>
     *   <li>创建 JPanel 面板并设置带标题的红色边框；</li>
     *   <li>创建 JTable 表格及 JScrollPane 滚动面板；</li>
     *   <li>定义表格列头（id、编号、检查项名称、单位、参考范围）；</li>
     *   <li>创建 DefaultTableModel 数据模型并注入表格；</li>
     *   <li>隐藏 ID 列（将最大宽度和最小宽度设为 0）；</li>
     *   <li>调用 {@link #addTableDatas()} 从数据库加载数据填充表格；</li>
     *   <li>将表格添加到滚动面板，面板添加到窗口，最后显示窗口。</li>
     * </ol>
     */
    public IndexView() {
        // 创建主窗口 JFrame，设置窗口标题为"主页"
        JFrame jFrame = new JFrame("主页");
        // 设置窗口关闭操作：EXIT_ON_CLOSE 表示关闭窗口时程序直接退出
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口位置（x=500, y=200）和大小（宽=800, 高=500）
        jFrame.setBounds(500, 200, 800, 500);
        // 设置内容布局为 null（绝对定位布局，不使用布局管理器）
        jFrame.getContentPane().setLayout(null);

        // 创建 JPanel 面板，用于承载检查项管理区域
        JPanel jPanel = new JPanel();
        // 设置面板在窗口中的位置和大小（绝对定位）
        jPanel.setBounds(20, 30, 550, 420);
        // 设置面板内部布局为 null（绝对定位）
        jPanel.setLayout(null);
        // 创建带标题的边框：标题为"检查项管理"，标题文字颜色为红色（RGB 255,0,0），左对齐顶部显示
        TitledBorder titledBorder = new TitledBorder(new TitledBorder("检查项管理"), "检查项管理",
                TitledBorder.LEFT, TitledBorder.TOP, null, new Color(255, 0, 0));
        // 将带标题的边框设置到面板上
        jPanel.setBorder(titledBorder);

        // 创建 JTable 表格组件，初始无数据
        jTable = new JTable();
        // 设置表格的位置和大小（绝对定位）
        jTable.setBounds(20, 20, 510, 150);
        // 创建 JScrollPane 可滚动面板，用于包裹表格以支持滚动条
        JScrollPane jScrollPane = new JScrollPane();
        // 设置滚动面板在面板中的位置和大小（绝对定位）
        jScrollPane.setBounds(20, 20, 510, 150);

        // 定义表格列头数组：对应数据库中要展示的字段名
        String[] columnNames = {"id", "编号", "检查项名称", "单位", "参考范围"};
        // 初始化表格数据为二维空数组（初始无数据，后续通过 addTableDatas 动态填充）
        Object[][] datas = {};
        // 创建 DefaultTableModel 表格数据模型，传入空数据和列头定义
        DefaultTableModel model = new DefaultTableModel(datas, columnNames);
        // 将数据模型注入到 JTable 表格中，使表格使用该模型管理数据
        jTable.setModel(model);
        // 获取表格的列模型（DefaultTableColumnModel），用于控制列的显示属性
        DefaultTableColumnModel dcm = (DefaultTableColumnModel) jTable.getColumnModel();
        // 将第一列（id 列）的最大宽度设为 0，实现隐藏 ID 列的效果
        dcm.getColumn(0).setMaxWidth(0);
        // 将第一列（id 列）的最小宽度也设为 0，确保 ID 列完全不可见
        dcm.getColumn(0).setMinWidth(0);
        // 调用 addTableDatas() 方法，从数据库加载检查项数据并动态追加到表格中
        addTableDatas();

        // 将 JTable 表格设置为滚动面板的视口视图（使表格可滚动）
        jScrollPane.setViewportView(jTable);
        // 将滚动面板添加到 JPanel 面板中
        jPanel.add(jScrollPane);
        // 将 JPanel 面板添加到 JFrame 窗口的内容面板中
        jFrame.getContentPane().add(jPanel);
        // 设置窗口可见（显示窗口）
        jFrame.setVisible(true);
        // 设置窗口大小是否可调节：true 表示用户可以拖拽调整窗口大小
        jFrame.setResizable(true);
    }

    /**
     * 向表格中动态添加数据。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取 JTable 的 DefaultTableModel 数据模型；</li>
     *   <li>清空表格现有行数据（setRowCount(0)）；</li>
     *   <li>调用 Service 层从数据库查询检查项列表；</li>
     *   <li>遍历检查项列表，将每个 CheckItem 对象的字段值封装为 Vector 行数据；</li>
     *   <li>将每行数据追加到表格模型中；</li>
     *   <li>若查询过程中发生 SQLException，打印异常堆栈信息。</li>
     * </ol>
     *
     * @throws SQLException 在方法内部捕获处理，不向外抛出
     */
    public void addTableDatas() {
        try {
            // 获取 JTable 的数据模型（DefaultTableModel），用于操作表格数据
            DefaultTableModel model = (DefaultTableModel) jTable.getModel();
            // 清空表格所有现有行数据（将行数设为 0），为重新加载数据做准备
            model.setRowCount(0);

            // 调用 Service 层从数据库查询启用状态的检查项列表
            List<CheckItem> list = checkItemService.getCheckItemData();
            // 遍历检查项列表，逐行将数据填充到表格模型中
            for (CheckItem checkItem : list) {
                // 创建 Vector 作为一行数据的容器（DefaultTableModel 的 addRow 方法接受 Vector 参数）
                Vector rowData = new Vector();
                // 添加检查项 ID 到行数据
                rowData.add(checkItem.getCid());
                // 添加检查项编号到行数据
                rowData.add(checkItem.getBh());
                // 添加检查项名称到行数据
                rowData.add(checkItem.getCname());
                // 添加计量单位到行数据
                rowData.add(checkItem.getDw());
                // 添加参考范围到行数据
                rowData.add(checkItem.getCkfw());
                // 将当前行数据追加到表格模型中（表格会自动刷新显示新行）
                model.addRow(rowData);
            }
        } catch (SQLException e) {
            // 捕获数据库访问异常，打印异常堆栈到标准错误输出
            // 注意：此处仅打印堆栈，未做用户友好的错误提示处理
            e.printStackTrace();
        }
    }
}
