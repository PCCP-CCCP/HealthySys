package com.nd.view;

import com.nd.view.entity.Appointment;
import com.nd.view.entity.CheckItem;
import com.nd.view.entity.ExamResult;
import com.nd.view.utils.JdbcUitl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordResultDialog extends JDialog {

    private Appointment appointment;
    private List<CheckItem> items = new ArrayList<CheckItem>();
    private List<JTextField> valueFields = new ArrayList<JTextField>();
    private List<JComboBox<String>> statusCombos = new ArrayList<JComboBox<String>>();

    public RecordResultDialog(Window owner, Appointment appointment) {
        super(owner, "录入检查结果", ModalityType.APPLICATION_MODAL);
        this.appointment = appointment;
        initUI();
    }

    private void initUI() {
        setSize(560, 520);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        // 标题信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel info = new JLabel("预约日期：" + appointment.getExamDate()
                + "    检查组：" + appointment.getGroupName());
        info.setFont(new Font("微软雅黑", Font.BOLD, 14));
        infoPanel.add(info);
        add(infoPanel, BorderLayout.NORTH);

        // 结果录入区
        JPanel center = new JPanel(new BorderLayout());
        JLabel tip = new JLabel("请为每个检查项填写检测数值与结果：");
        tip.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        tip.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 0));
        center.add(tip, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        loadItems(listPanel);
        center.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveBtn.setBackground(com.nd.view.utils.UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadItems(JPanel listPanel) {
        try {
            items = JdbcUitl.queryGroupItems(appointment.getGroupId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载检查项失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 已有结果，用于预填
        Map<Integer, ExamResult> existing = new HashMap<Integer, ExamResult>();
        try {
            List<ExamResult> list = JdbcUitl.queryExamResultsByAppointment(appointment.getId());
            for (ExamResult er : list) {
                existing.put(er.getItemId(), er);
            }
        } catch (Exception ignored) {
        }
        valueFields.clear();
        statusCombos.clear();
        listPanel.removeAll();
        for (CheckItem item : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            JLabel name = new JLabel(item.getName() + "：");
            name.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            name.setPreferredSize(new Dimension(160, 24));
            row.add(name);
            JTextField valueField = new JTextField(12);
            valueField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            JComboBox<String> status = new JComboBox<String>(new String[]{"正常", "异常"});
            status.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            ExamResult er = existing.get(item.getId());
            if (er != null) {
                valueField.setText(er.getItemValue());
                status.setSelectedItem(er.getResultStatus());
            }
            row.add(valueField);
            row.add(status);
            listPanel.add(row);
            valueFields.add(valueField);
            statusCombos.add(status);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void handleSave() {
        List<ExamResult> results = new ArrayList<ExamResult>();
        for (int i = 0; i < items.size(); i++) {
            CheckItem item = items.get(i);
            ExamResult er = new ExamResult();
            er.setItemId(item.getId());
            er.setItemValue(valueFields.get(i).getText().trim());
            er.setResultStatus((String) statusCombos.get(i).getSelectedItem());
            results.add(er);
        }
        try {
            int n = JdbcUitl.recordExamResults(appointment.getId(), results);
            JdbcUitl.updateAppointmentStatus(appointment.getId(), "已完成");
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "保存失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
