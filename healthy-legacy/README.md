# healthy-legacy · 旧版代码归档模块

## 模块职责

保留重构前的旧版代码（`com.nd.bean` / `com.nd.dao` / `com.nd.service` / `com.nd.view` 体系），**仅供历史参考**，不参与主流程，也不被任何新模块引用。

> 说明：旧版 `com.nd.bean.CheckItem` 与旧 `checkitem` 表字段（cid/bh/cname/dw/ckfw/status）与当前数据库结构（id/name/category/price/description/create_time）不一致，请勿直接使用其 SQL 逻辑。

## 包结构

```
com.nd.legacy
├── bean
│   └── CheckItem.java        # 旧版检查项实体（旧字段结构）
├── dao
│   └── CheckItemDao.java     # 旧版检查项数据访问
├── service
│   └── CheckItemService.java # 旧版检查项业务逻辑
└── view
    ├── IndexView.java        # 旧版主页（已被 MainView 取代）
    └── Demo.java             # JDBC 用法演示
```

## 迁移说明

归档时已做如下处理：
- 包名由 `com.nd.bean/dao/service/view` 调整为 `com.nd.legacy.*`，避免与新模块包名冲突。
- 旧代码对数据库访问工具 `JdbcUitl` 的**实例调用**已改为公共模块 `JdbcUtil` 的**静态调用**（`querySql`/`iudSql`/`close`），保证可编译。
- 重构前完整源码仍在 `_archive/old-src/` 中保留，未做任何改动。

## 依赖

- healthy-common（JdbcUtil）

## 多人协作建议

本模块一般**不再改动**。若确需参考旧实现，请以 `_archive/old-src/` 的原始文件为准；如需复用其中的逻辑，请迁移到 `healthy-dao` 与 `healthy-ui-feature` 对应包并适配新表结构。
