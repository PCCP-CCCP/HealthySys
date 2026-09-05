package com.nd.ui.shell;

import javax.swing.SwingUtilities;

/**
 * 应用启动入口（Main Class）。
 *
 * <p>本类属于 <b>healthy-ui-shell（应用外壳层）</b>模块，是整个 HealthySys
 * 体检中心管理系统的程序入口。负责在 Swing 事件调度线程（EDT）上启动登录界面。</p>
 *
 * <p>运行方式：<code>mvn -pl healthy-ui-shell exec:java</code> 或直接运行本类 main。</p>
 *
 * <p>关键依赖：{@link SwingUtilities#invokeLater(Runnable)}（EDT 线程安全调度）、
 * {@link LoginView#showLogin()}（登录界面启动方法）。</p>
 *
 * @author HealthySys 应用外壳模块
 */
public class App {

    /**
     * 程序入口：在事件调度线程（EDT）上启动登录界面。
     *
     * <p>Swing 组件的创建与修改必须在 EDT 上执行，因此使用
     * {@link SwingUtilities#invokeLater(Runnable)} 将界面初始化任务排入 EDT 事件队列。</p>
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // invokeLater：将 Runnable 投递到 Swing 事件调度线程异步执行，保证线程安全
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * 在 EDT 上执行的任务：调用 LoginView 展示登录窗口。
             */
            @Override
            public void run() {
                // 启动登录界面（内部会调用 UITheme.apply() 并创建 JFrame）
                LoginView.showLogin();
            }
        });
    }
}
