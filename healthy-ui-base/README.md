# healthy-ui-base · UI 主题与自绘组件模块

## 模块职责

集中管理全工程的视觉风格：主题色常量、程序化 Logo、以及一套**现代美颜风**自绘 Swing 组件。所有功能面板与壳层窗口都复用本模块组件，保证全系统风格统一。

> 本模块**无任何业务依赖**，可独立演进 UI 风格而不影响业务逻辑。

## 包结构

```
com.nd.ui.base
├── UITheme.java            # 主题常量（主色/辅助色/文字色/背景色等）+ badgeIcon 程序化 Logo
├── ModernButtonUI.java     # 圆角渐变按钮外观（主按钮/次按钮/危险按钮）
├── ModernTabbedPaneUI.java # 蓝色胶囊 Tab 外观
├── GradientPanel.java      # 顶部渐变横幅面板
├── RoundedPanel.java       # 圆角 + 投影卡片面板
├── RoundedField.java       # 圆角、聚焦蓝描边的文本框
└── RoundedPasswordField.java # 圆角、聚焦蓝描边的密码框
```

## 资源文件

`src/main/resources/` 下存放界面素材副本（`bck.png`、`medical_bg.png`、`medical_cross.png`、`medical_cross_white.png`），当前代码以程序化绘制为主，素材作为可选装饰保留。

## 主题色板（UITheme）

| 常量 | 色值 | 用途 |
| --- | --- | --- |
| PRIMARY | #3B82F6 | 主色（蓝） |
| PRIMARY_DK | #2563EB | 主色深 |
| ACCENT | #10B981 | 强调/成功 |
| WARN | #F59E0B | 警告 |
| DANGER | #EF4444 | 危险/删除 |
| TEXT_MAIN | #1E293B | 主文字 |
| TEXT_SUB | #64748B | 次要文字 |
| BG_MAIN | #F3F7FC | 页面背景 |
| HEAD_BG | #EEF2FF | 头部背景 |
| HEAD_FG | #1D4ED8 | 头部文字 |
| GRID | #EEF2F8 | 表格线 |
| SEL_BLUE | #DBEAFE | 选中色 |
| BTN_BG | #F1F5F9 | 按钮底色 |
| ZEBRA | #F8FAFC | 表格斑马纹 |

## 依赖

- 无（仅依赖 JDK Swing/AWT）

## 多人协作建议

UI 组件库由「前端/组件」同学维护。**组件接口（构造参数、对外方法）稳定后**，其余成员即可并行开发功能面板。若要整体换肤，只需改 `UITheme` 常量与各 UI 类，功能面板无需改动。
