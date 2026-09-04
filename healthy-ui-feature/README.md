# healthy-ui-feature · 功能面板模块

## 模块职责

承载系统的**全部业务功能面板**，按大功能分包组织。每个面板负责一个功能的界面交互，数据读写通过对应 DAO 完成（不直接持有连接细节）。本模块是多人协作时**拆分工作量最多的模块**。

## 包结构（按大功能分包）

```
com.nd.ui.feature
├── checkitem
│   ├── CheckItemManagePanel.java   # 检查项管理面板：查询/新建/编辑/删除/刷新
│   └── CreateCheckItemDialog.java  # 新建/编辑检查项对话框
├── checkgroup
│   ├── CheckGroupManagePanel.java  # 检查组管理面板：查询/新建/编辑/删除/刷新
│   └── CheckGroupEditDialog.java   # 选择多个检查项组建/修改检查组对话框
├── appointment
│   └── AppointmentPanel.java       # 患者预约面板：提交预约 + 我的预约 + 取消预约
├── record
│   ├── RecordResultPanel.java      # 医生录入结果面板：按患者姓名查预约 → 选择某次体检
│   └── RecordResultDialog.java     # 录入某次体检各检查项数值与结果状态对话框
├── tracking
│   ├── TrackingPanel.java          # 患者跟踪管理：历次结果总览 + 按检查项对比（内层双 Tab）
│   └── PatientResultPanel.java     # 医生查看患者结果（实现与 TrackingPanel 一致）
└── profile
    └── ProfilePanel.java           # 个人信息管理：资料维护（出生日期/性别/身高/体重）+ 修改密码
```

## 功能与角色对应

| 功能面板 | 使用角色 | 对应 DAO / Service |
| --- | --- | --- |
| 检查项管理 | 医生 | CheckItemDao |
| 检查组管理 | 医生 | CheckGroupDao |
| 录入结果 | 医生 | AppointmentDao（查预约）+ ExamResultDao（录入） |
| 查看患者结果 | 医生 | ExamResultDao.queryByUserName |
| 预约 | 患者 | AppointmentDao |
| 跟踪管理 | 患者 | ExamResultDao.queryByUser |
| 个人信息管理 | 医生 + 患者（共用） | UserService（getProfile/updateProfile/changePassword） |

## 关键说明

- 面板只负责「界面 + 交互 + 调用 DAO/Service」，**不写 SQL、不做事务**，事务在 DAO 层完成；个人信息与改密等业务规则调用 `UserService`（`healthy-service`），不在面板内处理密码逻辑。
- 每个面板实现统一的 `onShow()` 刷新方法，主界面切换 Tab 时自动调用，保证每次进入页面数据为最新。
- 录入结果流程：医生输入患者姓名 → 查询该患者预约列表 → 选中「已预约/已完成」的某次体检 → 在对话框中为该次体检的各检查项录入数值。
- 个人信息面板为两个视角共用的导航模块：左侧维护个人资料，右侧修改登录密码（改密前需校验原密码）。

## 依赖

- healthy-common（实体、Session）
- healthy-dao（各 DAO）
- healthy-service（UserService 等业务逻辑）
- healthy-ui-base（主题与自绘组件）

## 多人协作建议

每个功能包（checkitem / checkgroup / appointment / record / tracking / profile）可**分配给不同成员并行开发**，包之间无相互依赖。统一风格：面板布局复用 `RoundedPanel`/`ModernButtonUI`/`ModernTabbedPaneUI`，数据刷新统一走 `onShow()`。
