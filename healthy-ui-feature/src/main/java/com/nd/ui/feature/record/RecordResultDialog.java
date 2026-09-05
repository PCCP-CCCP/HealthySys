package com.nd.ui.feature.record;

import com.nd.common.entity.Appointment;
import com.nd.common.entity.CheckItem;
import com.nd.common.entity.ExamResult;
import com.nd.dao.AppointmentDao;
import com.nd.dao.CheckGroupDao;
import com.nd.dao.ExamResultDao;
import com.nd.ui.base.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 录入检查结果对话框（医生角色）。
 *
 * <p>针对某次预约，为该检查组内的每个检查项填写检测数值与判定结果（正常/异常）；
 * 若该预约已有结果则预填；保存时先清空旧结果再批量写入，并将预约状态置为"已完成"。</p>
 *
 * <p><b>所属模块</b>：healthy-ui-feature / record（体检结果录入）。</p>
 *
 * <p><b>核心功能点</b>：</p>
 * <ul>
 *   <li>顶部展示预约日期与检查组名称；</li>
 *   <li>中部逐项列出组内检查项，每项对应一个数值输入框 + 正常/异常下拉框；</li>
 *   <li>若该预约已有历史结果，则自动预填数值与判定；</li>
 *   <li>保存时调用 {@link ExamResultDao#record(Integer, List)} 批量写入结果，
 *       并调用 {@link AppointmentDao#updateStatus(Integer, String)} 将预约状态置为"已完成"。</li>
 * </ul>
 *
 * <p><b>关键依赖</b>：</p>
 * <ul>
 *   <li>DAO：{@link CheckGroupDao}（queryGroupItems 加载组内检查项）、
 *       {@link ExamResultDao}（queryByAppointment / record）、
 *       {@link AppointmentDao}（updateStatus）；</li>
 *   <li>实体：{@link Appointment}、{@link CheckItem}、{@link ExamResult}；</li>
 *   <li>UI 基类：{@link JDialog}、{@link JTextField}、{@link JComboBox}、
 *       {@link BoxLayout} 等；</li>
 *   <li>UI 主题：{@link UITheme#PRIMARY} 主色用于"保存"按钮。</li>
 * </ul>
 *
 * @author HealthySys 功能界面模块（录入结果）
 */
public class RecordResultDialog extends JDialog {

    /** 当前录入结果的预约对象，提供 groupId / appointmentId / 日期 / 检查组名等上下文 */
    private Appointment appointment;
    /** 该预约所属检查组包含的检查项列表（从 checkGroupDao.queryGroupItems 加载） */
    private List<CheckItem> items = new ArrayList<CheckItem>();
    /** 与检查项一一对应的数值输入框（索引与 items 对应） */
    private List<JTextField> valueFields = new ArrayList<JTextField>();
    /** 与检查项一一对应的结果下拉框（正常/异常），索引与 items 对应 */
    private List<JComboBox<String>> statusCombos = new ArrayList<JComboBox<String>>();
    /** 检查组数据访问对象，查询组内检查项 */
    private final CheckGroupDao checkGroupDao = new CheckGroupDao();
    /** 检查结果数据访问对象，查询/保存体检结果 */
    private final ExamResultDao examResultDao = new ExamResultDao();
    /** 预约数据访问对象，更新预约状态 */
    private final AppointmentDao appointmentDao = new AppointmentDao();

    /**
     * 构造录入结果对话框。
     *
     * @param owner       所属父窗口（通常为主界面 JFrame）
     * @param appointment 目标预约对象（确定检查组与日期）
     */
    public RecordResultDialog(Window owner, Appointment appointment) {
        // 调用父类构造器，窗口标题固定为"录入检查结果"，模态阻塞父窗口
        super(owner, "录入检查结果", ModalityType.APPLICATION_MODAL);
        // 保存预约上下文引用
        this.appointment = appointment;
        // 构建界面并加载检查项
        initUI();
    }

    /**
     * 初始化界面布局：预约信息 + 逐项录入区 + 按钮，并加载检查项。
     *
     * <p>整体采用 BorderLayout：NORTH 为预约信息展示，CENTER 为逐项录入区
     * （带滚动条），SOUTH 为"保存/取消"按钮。</p>
     */
    private void initUI() {
        // 对话框固定大小 560x520，禁止调整尺寸
        setSize(560, 520);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        // ---- 北部：预约信息展示 ----
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        // 展示预约日期与检查组名称
        JLabel info = new JLabel("预约日期：" + appointment.getExamDate()
                + "    检查组：" + appointment.getGroupName());
        info.setFont(new Font("微软雅黑", Font.BOLD, 14));
        infoPanel.add(info);
        add(infoPanel, BorderLayout.NORTH);

        // ---- 中部：结果录入区 ----
        JPanel center = new JPanel(new BorderLayout());
        // 顶部提示文字
        JLabel tip = new JLabel("请为每个检查项填写检测数值与结果：");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        tip.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 0));
        center.add(tip, BorderLayout.NORTH);

        // 垂直排列的录入行面板，外层套 JScrollPane
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        // 加载检查组内检查项，并为每项生成输入框与下拉框（含预填）
        loadItems(listPanel);
        center.add(new JScrollPane(listPanel), BorderLayout.CENTER);
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
        // 保存按钮：收集所有检查项结果并落库
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
    }

    /**
     * 加载检查组内检查项，并为每项生成数值输入框与结果下拉框；已有结果则预填。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>调用 {@link CheckGroupDao#queryGroupItems(Integer)} 查询组内检查项；</li>
     *   <li>调用 {@link ExamResultDao#queryByAppointment(Integer)} 查询该预约已有结果，
     *       按 itemId 存入 Map 便于 O(1) 查找；</li>
     *   <li>遍历检查项，每项生成一行：标签（固定宽度 160px）+ 数值输入框 + 正常/异常下拉框；</li>
     *   <li>若已有结果则预填数值与判定；</li>
     *   <li>revalidate/repaint 刷新界面。</li>
     * </ol>
     *
     * @param listPanel 承载录入行的垂直面板
     */
    private void loadItems(JPanel listPanel) {
        try {
            // 根据预约关联的 groupId 查询检查组包含的所有检查项
            items = checkGroupDao.queryGroupItems(appointment.getGroupId());
        } catch (Exception e) {
            // 加载失败弹窗提示并终止，避免后续空指针
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 已有结果缓存：key=检查项ID，value=历史结果，用于编辑模式下预填
        Map<Integer, ExamResult> existing = new HashMap<Integer, ExamResult>();
        try {
            // 查询该预约已录入的所有结果
            List<ExamResult> list = examResultDao.queryByAppointment(appointment.getId());
            // 按 itemId 建立索引，便于后续 O(1) 查找
            for (ExamResult er : list) {
                existing.put(er.getItemId(), er);
            }
        } catch (Exception ignored) {
            // 查询旧结果失败时跳过预填，不影响主流程（视为全新录入）
        }
        // 清空旧控件，准备重建
        valueFields.clear();
        statusCombos.clear();
        listPanel.removeAll();
        for (CheckItem item : items) {
            // 每一行使用 FlowLayout.LEFT，水平间距 10、垂直间距 6
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            // 检查项名称标签，固定宽度 160px，保证输入框对齐
            JLabel name = new JLabel(item.getName() + "：");
            name.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            name.setPreferredSize(new Dimension(160, 24));
            row.add(name);
            // 数值输入框，列宽 12
            JTextField valueField = new JTextField(12);
            valueField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            // 结果下拉框：固定选项"正常/异常"
            JComboBox<String> status = new JComboBox<String>(new String[]{"正常", "异常"});
            status.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            // 若该检查项已有历史结果，则预填数值与判定
            ExamResult er = existing.get(item.getId());
            if (er != null) {
                valueField.setText(er.getItemValue());
                status.setSelectedItem(er.getResultStatus());
            }
            row.add(valueField);
            row.add(status);
            listPanel.add(row);
            // 同步保存到 valueFields / statusCombos，保持索引与 items 对应
            valueFields.add(valueField);
            statusCombos.add(status);
        }
        // 容器内容变化后必须 revalidate+repaint 才能正确刷新布局
        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * 处理保存：收集所有检查项结果写入数据库，并将预约状态置为"已完成"。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>遍历 items，逐项从 valueFields / statusCombos 取值，构造 {@link ExamResult} 实体；</li>
     *   <li>调用 {@link ExamResultDao#record(Integer, List)} 批量写入结果
     *       （DAO 内部通常先删除旧结果再插入新结果）；</li>
     *   <li>调用 {@link AppointmentDao#updateStatus(Integer, String)} 将预约状态置为"已完成"；</li>
     *   <li>根据返回值提示成功/失败，成功则 dispose 关闭对话框。</li>
     * </ol>
     */
    private void handleSave() {
        // 收集所有检查项的结果到列表
        List<ExamResult> results = new ArrayList<ExamResult>();
        for (int i = 0; i < items.size(); i++) {
            CheckItem item = items.get(i);
            // 构造新的 ExamResult 实体
            ExamResult er = new ExamResult();
            er.setItemId(item.getId());
            // 读取数值输入框文本并去空白
            er.setItemValue(valueFields.get(i).getText().trim());
            // 读取下拉框选中的判定结果（正常/异常）
            er.setResultStatus((String) statusCombos.get(i).getSelectedItem());
            results.add(er);
        }
        try {
            // 批量写入结果（DAO 内部处理事务），返回写入条数
            int n = examResultDao.record(appointment.getId(), results);
            // 同时将预约状态更新为"已完成"
            appointmentDao.updateStatus(appointment.getId(), "已完成");
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                // 成功后关闭对话框
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "保存失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            // 数据库异常统一弹窗提示
            JOptionPane.showMessageDialog(this, "保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
