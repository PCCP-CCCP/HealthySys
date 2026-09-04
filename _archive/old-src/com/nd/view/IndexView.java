package com.nd.view;

import com.nd.bean.CheckItem;
import com.nd.service.CheckItemService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class IndexView {//主页
    CheckItemService checkItemService = new CheckItemService();
    JTable jTable = null;


    public IndexView(){//构造函数：创建对象时 执行此函数
        JFrame jFrame = new JFrame("主页");
        // 设置默认的关闭操作，这样当用户关闭窗口时程序会退出
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口x、y轴坐标位置，设置窗口大小宽、高
        jFrame.setBounds(500, 200, 800, 500);
        jFrame.getContentPane().setLayout(null);

        //创建面板
        JPanel jPanel = new JPanel();
        jPanel.setBounds(20, 30, 550, 420);
        jPanel.setLayout(null);
        //创建带标题的边框
        TitledBorder titledBorder = new TitledBorder(new TitledBorder("检查项管理"),"检查项管理",TitledBorder.LEFT,TitledBorder.TOP,null,new Color(255, 0, 0));
        //给面板设置边框
        jPanel.setBorder(titledBorder);

        //创建表格
        jTable = new JTable();
        jTable.setBounds(20,20,510,150);
        //可滚动的面板
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setBounds(20,20,510,150);

        //定义列头数组
        String[] columnNames = {"id","编号","检查项名称","单位","参考范围"};
        //所有数据
        Object[][] datas = {};
        //创建 表格中数据模型
        DefaultTableModel model = new DefaultTableModel(datas,columnNames);
        //将模型数据注入到表格中
        jTable.setModel(model);
        //将表格第一列宽度设置为0，目的是隐藏id
        DefaultTableColumnModel dcm = (DefaultTableColumnModel) jTable.getColumnModel();
        dcm.getColumn(0).setMaxWidth(0);
        dcm.getColumn(0).setMinWidth(0);
        //动态向表格追加数据
        addTableDatas();


        //将可滚动面板注入表格
        jScrollPane.setViewportView(jTable);
        //将边框注入到面板中
        jPanel.add(jScrollPane);
        //将面板添加到窗口中
        jFrame.getContentPane().add(jPanel);
        // 显示窗口
        jFrame.setVisible(true);
        //设置窗口可调节大小，默认可以调节，false是不可调节
        jFrame.setResizable(true);
    }

    //向表格中动态添加数据
    public void addTableDatas(){
        try {
            //获取表格中模板
            DefaultTableModel model = (DefaultTableModel) jTable.getModel();
            //设置从第几行开始
            model.setRowCount(0);

            //从数据库中获取数据
            List<CheckItem> list = checkItemService.getCheckItemData();
            //将数据库查询数据，添加到模板中
            for (CheckItem checkItem:list) {
                Vector rowData = new Vector();
                rowData.add(checkItem.getCid());
                rowData.add(checkItem.getBh());
                rowData.add(checkItem.getCname());
                rowData.add(checkItem.getDw());
                rowData.add(checkItem.getCkfw());
                model.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
