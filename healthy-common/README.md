# healthy-common · 公共层模块

## 模块职责

无界面依赖的公共基础层，为所有其他模块提供：
- **JDBC 数据库访问**（连接创建 / 查询 / 增删改 / 关闭 / 回滚）
- **数据实体**（检查项、检查组、预约、体检结果）
- **会话状态**（当前登录用户与角色视角）
- **数据库连接配置**（集中管理连接串与账号）

## 包结构

```
com.nd.common
├── db
│   ├── DBConfig.java     # 数据库连接配置常量（URL / root / 密码）
│   └── JdbcUtil.java     # JDBC 工具类：getConnection / querySql / iudSql / close / rollback
├── entity
│   ├── CheckItem.java    # 检查项实体（id/name/category/price/description/createTime）
│   ├── CheckGroup.java   # 检查组实体（id/name/description 及项目明细）
│   ├── Appointment.java  # 预约实体（id/userTel/groupId/examDate/status 及联表字段 groupName）
│   └── ExamResult.java   # 体检结果实体（appointmentId/itemId/itemValue/resultStatus 及联表字段）
└── util
    └── Session.java      # 会话：当前手机号/姓名/账号角色/当前视角
```

## 关键说明

- **JdbcUtil 为静态工具类**（构造器私有），全工程统一通过它访问数据库：
  - `querySql(sql, params)`：执行查询，返回 `ResultSet`，**使用后必须调用 `close()`** 释放连接。
  - `iudSql(sql, params)`：执行增删改，返回受影响行数。
  - `close(con, ps, rs)` / `close()` / `rollback(con)`：资源释放与事务回滚。
- **连接配置**统一在 `DBConfig.java`，修改数据库地址/账号仅需改此处。
- **MySQL 驱动**以 `system scope` 引入，路径指向工程根 `lib/mysql-connector-java-8.0.14.jar`，因此本模块是唯一依赖驱动 jar 的模块。

## 依赖

- 无内部模块依赖（仅依赖 JDK 与 MySQL 驱动）。

## 多人协作建议

本模块是「地基」，实体与 JDBC 工具的改动会影响全部模块。建议由数据组先行稳定，**接口一经确定尽量少改**。
