# healthy-common · 公共层模块

## 模块职责

无界面依赖的公共基础层，为所有其他模块提供：
- **JDBC 数据库访问**（连接创建 / 查询 / 增删改 / 关闭 / 回滚）
- **数据实体**（用户、检查项、检查组、预约、体检结果）
- **会话状态**（当前登录用户与角色视角）
- **密码安全**（随机盐 + SHA-256 单向加密，供注册/登录/改密使用）
- **数据库连接配置**（集中管理连接串与账号）

## 包结构

```
com.nd.common
├── db
│   ├── DBConfig.java     # 数据库连接配置常量（URL / root / 密码）
│   └── JdbcUtil.java     # JDBC 工具类：getConnection / querySql / iudSql / close / rollback
├── entity
│   ├── User.java         # 用户实体（tel/pwd密文/salt/name/role 及出生日期/性别/身高/体重）
│   ├── CheckItem.java    # 检查项实体（id/name/category/price/description/createTime）
│   ├── CheckGroup.java   # 检查组实体（id/name/description 及项目明细）
│   ├── Appointment.java  # 预约实体（id/userTel/groupId/examDate/status 及联表字段 groupName）
│   └── ExamResult.java   # 体检结果实体（appointmentId/itemId/itemValue/resultStatus 及联表字段）
└── util
    ├── Session.java      # 会话：当前手机号/姓名/账号角色/当前视角
    └── PasswordUtil.java # 密码安全：generateSalt / hash / verify（SHA-256 加盐单向哈希）
```

## 关键说明

- **JdbcUtil 为静态工具类**（构造器私有），全工程统一通过它访问数据库：
  - `querySql(sql, params)`：执行查询，返回 `ResultSet`，**使用后必须调用 `close()`** 释放连接。
  - `iudSql(sql, params)`：执行增删改，返回受影响行数。
  - `close(con, ps, rs)` / `close()` / `rollback(con)`：资源释放与事务回滚。
- **密码加密**（`PasswordUtil`）：密码不落明文，统一以「随机盐 + SHA-256」单向哈希存储。
  `pwd` 列存 64 位 hex 摘要、`salt` 列存 32 位随机盐；登录/改密时用 `verify()` 重算比对。
- **连接配置**统一在 `DBConfig.java`，修改数据库地址/账号仅需改此处。
- **MySQL 驱动**以 `system scope` 引入，路径指向工程根 `lib/mysql-connector-java-8.0.14.jar`，因此本模块是唯一依赖驱动 jar 的模块。

## 依赖

- 无内部模块依赖（仅依赖 JDK 与 MySQL 驱动）。

## 多人协作建议

本模块是「地基」，实体与 JDBC 工具的改动会影响全部模块。建议由数据组先行稳定，**接口一经确定尽量少改**。密码加密方案（`PasswordUtil`）一经确定全工程统一，勿在各 DAO/面板中各自实现加密逻辑。
