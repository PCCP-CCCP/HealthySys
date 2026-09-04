package com.nd.ui.shell;

import javax.swing.SwingUtilities;

/**
 * 应用启动入口（Main Class）。
 *
 * <p>运行方式：<code>mvn -pl healthy-ui-shell exec:java</code> 或直接运行本类 main。</p>
 *
 * @author HealthySys 应用外壳模块
 */
public class App {

    /**
     * 程序入口：在事件调度线程（EDT）上启动登录界面。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginView.showLogin();
            }
        });
    }
}
