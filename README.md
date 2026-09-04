# 体检中心管理系统（HealthySys）· 多模块工程

基于 Java Swing + MySQL 的体检中心管理系统。本工程采用 **Maven 多模块聚合架构** 重构，按「大功能」拆分为 7 个独立模块，每个模块职责单一、依赖清晰，适合 **多人并行协作开发**。

---

## 一、功能总览

| 功能 | 说明 | 所属模块 |
| --- | --- | --- |
| 登录 / 注册 | 注册时可选「仅患者角色」或「双重角色（医生+患者）」 | healthy-ui-shell |
| 双角色切换 | 医生账号可在右上角「一键切换角色」在医生/患者视角间切换；仅患者账号无切换入口 | healthy-ui-shell |
| 检查项管理 | 检查项增删改查（名称/分类/价格/描述） | healthy-ui-feature / healthy-dao |
| 检查组管理 | 检查组创建、编辑、删除（含选择多个检查项组成检查组） | healthy-ui-feature / healthy-dao |
| 预约 | 患者选择检查组 + 日期提交预约；可取消「已预约」状态的预约 | healthy-ui-feature / healthy-dao |
| 录入结果 | 医生输入患者姓名 → 查询其预约 → 选择某次体检逐项录入结果 | healthy-ui-feature / healthy-dao |
| 跟踪管理 | 患者查看历次检查结果总览 + 按检查项对比 | healthy-ui-feature / healthy-dao |
| 查看患者结果 | 医生查看任意患者历次结果（实现与患者跟踪管理一致） | healthy-ui-feature / healthy-dao |

---

## 二、模块架构

```
                         ┌─────────────────────────────────────────────┐
                         │            healthy-ui-shell (壳层)           │
                         │  App 入口 / MainView / LoginView / 注册对话框  │
                         └───────┬──────────────┬──────────────────────┘
                                 │              │
                 ┌───────────────┘              └───────────────┐
                 ▼                                              ▼
       ┌──────────────────────┐                     ┌──────────────────────┐
       │   healthy-ui-feature │                     │    healthy-ui-base   │
       │ 功能面板（检查项/检查组 │                     │  UI 主题与自绘组件     │
       │ 预约/录入/跟踪/患者结果）│                     │  (无业务依赖)          │
       └───────┬──────────────┘                     └──────────┬───────────┘
               │                                               │
               ▼                                               ▼
       ┌──────────────────────┐                     ┌──────────────────────┐
       │      healthy-dao     │                     │     healthy-dao      │
       │  5 个数据访问对象      │ ──────────────────▶ │    （复用）           │
       └───────┬──────────────┘                     └──────────────────────┘
               │
               ▼
       ┌──────────────────────┐      ┌────────────────────────────────────┐
       │    healthy-service   │      │           healthy-common           │
       │  业务校验（登录/注册）  │ ───▶ │  JdbcUtil / 实体 / Session / DBConfig │
       └──────────────────────┘      └────────────────────────────────────┘
                                                          ▲
       ┌──────────────────────┐                           │
       │    healthy-legacy    │ ──────▶ 仅依赖 common，保留旧代码参考        │
       │  旧版代码归档（参考用） │
       └──────────────────────┘
```

### 依赖方向（自底向上，禁止反向依赖）

```
healthy-common  ←  所有模块都可依赖（唯一持有 JDBC 驱动）
healthy-dao     ←  common
healthy-service ←  common + dao
healthy-ui-base ←  无业务依赖（纯 UI 组件）
healthy-ui-feature ← common + dao + ui-base
healthy-ui-shell   ← common + dao + service + ui-base + ui-feature
healthy-legacy     ← common（仅历史参考，不参与主流程）
```

---

## 三、模块一览

| 模块 | 职责 | 关键类 | 说明文档 |
| --- | --- | --- | --- |
| healthy-common | 公共层：JDBC 工具、实体、会话 | `JdbcUtil`、`DBConfig`、4 个实体、`Session` | [README](healthy-common/README.md) |
| healthy-dao | 数据访问层：按大功能拆 5 个 DAO | `UserDao`、`CheckItemDao`、`CheckGroupDao`、`AppointmentDao`、`ExamResultDao` | [README](healthy-dao/README.md) |
| healthy-service | 业务逻辑层 | `UserService` | [README](healthy-service/README.md) |
| healthy-ui-base | UI 主题与自绘组件 | `UITheme`、`ModernButtonUI`、`ModernTabbedPaneUI`、`GradientPanel`、`RoundedPanel`、`RoundedField` 等 | [README](healthy-ui-base/README.md) |
| healthy-ui-feature | 功能面板（按功能分包） | 检查项 / 检查组 / 预约 / 录入 / 跟踪 / 患者结果 | [README](healthy-ui-feature/README.md) |
| healthy-ui-shell | 应用壳层：入口与主界面 | `App`、`MainView`、`LoginView`、`RegisterDialog` | [README](healthy-ui-shell/README.md) |
| healthy-legacy | 旧版代码归档（仅参考） | `CheckItem`、`IndexView` 等 | [README](healthy-legacy/README.md) |

---

## 四、环境要求

- **JDK 11+**（工程按 Java 11 编译，当前环境 JDK 25 可正常编译运行）
- **Maven 3.6+**
- **MySQL 8.x**，数据库名 `newcenter`，字符集 utf8mb4
- JDBC 驱动 `lib/mysql-connector-java-8.0.14.jar`（已随工程提供）

---

## 五、构建与运行

### 1. 准备数据库

执行 `resources/init.sql` 初始化库表与种子数据：

```sql
-- 在 MySQL 中执行 resources/init.sql
```

表结构（权威来源：`resources/init.sql`）：

| 表 | 说明 | 关键字段 |
| --- | --- | --- |
| users | 用户 | tel, pwd, name, role（doctor/patient） |
| checkitem | 检查项 | id, name, category, price, description, create_time |
| checkgroup | 检查组 | id, name, description, create_time |
| checkgroup_item | 检查组-检查项关联 | group_id, item_id |
| appointment | 预约 | id, user_tel, group_id, exam_date, status |
| exam_result | 体检结果 | id, appointment_id, item_id, item_value, result_status |

### 2. 编译

在工程根目录执行：

```bash
mvn clean compile
```

### 3. 运行

方式一（IDE）：打开根 `pom.xml` 导入为 Maven 工程，运行 `com.nd.ui.shell.App` 的 `main` 方法。

方式二（命令行）：

```bash
mvn -q -DskipTests package
java -cp "healthy-common/target/classes;healthy-dao/target/classes;healthy-service/target/classes;healthy-ui-base/target/classes;healthy-ui-feature/target/classes;healthy-ui-shell/target/classes;lib/mysql-connector-java-8.0.14.jar" com.nd.ui.shell.App
```

### 4. 测试账号

| 账号 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| 183 | 123456 | doctor | 「管理员」，可一键切换医生/患者视角 |
| 1 | 1 | patient | 仅患者视角 |

> 数据库连接配置集中在 `healthy-common` 的 `DBConfig.java`，如需修改请改该文件。

---

## 六、角色模型

- **账号角色**（`users.role`）：`doctor`（医生）或 `patient`（患者）。
- **注册**：可选「仅患者角色」或「双重角色（医生+患者）」。选双重角色时 `role` 记为 `doctor`（拥有双视角）。
- **视角**（`Session.currentRole`）：
  - 医生视角：检查项管理、检查组管理、录入结果、查看患者结果。
  - 患者视角：预约、跟踪管理。
- 医生账号可在主界面右上角「一键切换角色」；仅患者账号登录后不显示该按钮。

---

## 七、多人协作分工建议

按「模块」切分任务，模块间通过稳定的包接口解耦，冲突最小：

| 负责人角色 | 建议认领模块 | 说明 |
| --- | --- | --- |
| 后端/数据 | healthy-common、healthy-dao | JDBC、实体、数据访问，接口先行 |
| 后端/业务 | healthy-service | 登录注册校验等业务规则 |
| 前端/组件 | healthy-ui-base | UI 主题与自绘控件，全工程复用 |
| 前端/功能 | healthy-ui-feature | 各功能面板，可按包再拆分给多人 |
| 前端/集成 | healthy-ui-shell | 主界面、入口、角色切换 |
| 兼容维护 | healthy-legacy | 旧代码归档，一般不动 |

**协作约定**：
1. 每个函数/方法必须带 Javadoc 注释（本工程已全部落实）。
2. 每个模块须有 `README.md`（本工程已提供，新增功能时同步更新）。
3. 新增功能遵循「实体 → DAO → 面板」的依赖方向，禁止跨层反向调用。
4. 提交前执行 `mvn clean compile` 保证全工程编译通过。

---

## 八、目录结构

```
HealthySys/
├── pom.xml                      # 根聚合 POM（管理 7 个子模块）
├── resources/                   # 数据库初始化脚本 init.sql
├── lib/                         # MySQL JDBC 驱动
├── healthy-common/              # 公共层
├── healthy-dao/                 # 数据访问层
├── healthy-service/             # 业务逻辑层
├── healthy-ui-base/             # UI 主题与组件
├── healthy-ui-feature/          # 功能面板
├── healthy-ui-shell/            # 应用壳层
├── healthy-legacy/              # 旧代码归档
├── preview/multi/               # 界面验证截图
└── _archive/old-src/            # 重构前的单模块源码备份
```

> `_archive/old-src` 为重构前的旧单模块源码备份（含旧 `com.nd.view` 等包），仅作历史参考，不在 Maven 构建范围内。
