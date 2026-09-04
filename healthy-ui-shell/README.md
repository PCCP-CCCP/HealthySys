# healthy-ui-shell · 应用壳层模块

## 模块职责

整个应用的**入口与主框架**：程序启动、登录、注册、主界面布局、角色切换与登出。壳层将各功能面板组装进主界面，是「把一切串起来」的模块。

## 包结构

```
com.nd.ui.shell
├── App.java           # 启动入口（main）：进入登录界面
├── MainView.java      # 主界面：顶部导航/用户信息/角色切换/登出 + 按角色组装功能 Tab（含共用「个人信息」）
├── LoginView.java     # 登录界面（现代卡片式），含「注册新用户」入口
└── RegisterDialog.java # 注册对话框：选择「仅患者角色」或「双重角色（医生+患者）」
```

## 角色与界面组装

主界面按 `Session.currentRole` 组装 Tab，两个视角均追加共用的「个人信息」模块：

| 当前视角 | 顶部 Tab |
| --- | --- |
| 医生视角 | 检查项管理 · 检查组管理 · 录入结果 · 查看患者结果 · 个人信息 |
| 患者视角 | 预约 · 跟踪管理 · 个人信息 |

- **一键切换角色**：仅当账号角色为 `doctor`（双重角色）时显示按钮，点击在医生/患者视角间切换并重建 Tab；仅患者账号登录后无此按钮。
- **登出**：清空 Session，返回登录界面。
- **头部用户信息**：登录成功后由 `UserService` 将当前用户姓名写入 `Session.currentName`，主界面头部优先显示姓名（未设置时回退为账号）。

## 启动方式

方式一（IDE）：在 `App` 上直接运行 `main`。

方式二（命令行，在工程根目录执行）：

```bash
mvn -q -DskipTests package
java -cp "healthy-common/target/classes;healthy-dao/target/classes;healthy-service/target/classes;healthy-ui-base/target/classes;healthy-ui-feature/target/classes;healthy-ui-shell/target/classes;lib/mysql-connector-java-8.0.14.jar" com.nd.ui.shell.App
```

## 依赖

- healthy-common（Session、实体）
- healthy-dao（UserDao）
- healthy-service（UserService）
- healthy-ui-base（主题组件）
- healthy-ui-feature（各功能面板）

## 多人协作建议

壳层负责「集成」，改动影响全局，建议由**集成负责人**维护。功能面板由 feature 模块并行开发，壳层只需按包名引用即可；新增功能时在此注册新的 Tab。
