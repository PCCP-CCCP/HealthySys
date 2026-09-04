# healthy-dao · 数据访问层模块

## 模块职责

按「大功能」拆分的数据访问层，屏蔽 SQL 细节，为上层提供面向业务的数据读写方法。对应重构前 `JdbcUitl` 上帝类中按功能划分的全部数据库方法。

## 包结构

```
com.nd.dao
├── UserDao.java          # 用户：login / checkTelExists / register
├── CheckItemDao.java     # 检查项：queryAll / insert / update / delete
├── CheckGroupDao.java    # 检查组：queryAll / queryGroupItems / queryGroupItemIds / create / update / delete（含事务）
├── AppointmentDao.java   # 预约：create / queryByUser / queryByUserName / cancel / updateStatus
└── ExamResultDao.java    # 结果：queryByAppointment / record / queryByUserAndItem / queryByUser / queryByUserName / queryByNameAndItem
```

## 方法清单与功能说明

### UserDao（用户）
| 方法 | 功能 |
| --- | --- |
| `login(tel, pwd)` | 按手机号+密码查询用户，登录校验 |
| `checkTelExists(tel)` | 检查手机号是否已注册 |
| `register(...)` | 新增用户（含角色） |

### CheckItemDao（检查项管理）
| 方法 | 功能 |
| --- | --- |
| `queryAll()` | 查询全部检查项 |
| `insert(item)` | 新增检查项 |
| `update(item)` | 修改检查项 |
| `delete(id)` | 删除检查项 |

### CheckGroupDao（检查组管理）
| 方法 | 功能 |
| --- | --- |
| `queryAll()` | 查询全部检查组（含检查项名称列表） |
| `queryGroupItems(groupId)` | 查询某检查组的检查项明细 |
| `queryGroupItemIds(groupId)` | 查询某检查组的检查项 id 集合 |
| `create(name, desc, itemIds)` | 创建检查组并绑定检查项（**事务**） |
| `update(id, name, desc, itemIds)` | 修改检查组及其检查项（**事务**） |
| `delete(id)` | 删除检查组及其关联（**事务**） |

### AppointmentDao（预约）
| 方法 | 功能 |
| --- | --- |
| `create(...)` | 新增预约（状态=已预约） |
| `queryByUser(tel)` | 查询某用户的全部预约 |
| `queryByUserName(name)` | 按患者姓名查询预约（医生录入用） |
| `cancel(id, userTel)` | 取消预约（仅「已预约」状态可取消） |
| `updateStatus(id, status)` | 更新预约状态（已完成/已取消） |

### ExamResultDao（体检结果）
| 方法 | 功能 |
| --- | --- |
| `queryByAppointment(apptId)` | 按预约查询该次体检的全部结果 |
| `record(...)` | 录入一条体检结果 |
| `queryByUserAndItem(...)` | 按用户+检查项查询（跟踪对比用） |
| `queryByUser(tel)` | 查询某用户全部历次结果（结果总览） |
| `queryByUserName(name)` | 按患者姓名查询全部历次结果（医生查看患者结果） |
| `queryByNameAndItem(...)` | 按姓名+检查项查询（医生侧对比） |

## 关键说明

- **事务**：检查组创建/修改/删除涉及 `checkgroup` 与 `checkgroup_item` 两张表，使用 `Connection` + `rollback` 保证原子性，出错自动回滚。
- 所有方法通过 `com.nd.common.db.JdbcUtil` 访问数据库。

## 依赖

- healthy-common（JdbcUtil、实体）

## 多人协作建议

本模块可**按 DAO 拆给多人并行开发**（每人认领 1~2 个 DAO），类之间无相互依赖，冲突极小。开发时先参照 `resources/init.sql` 的表结构确认字段名。
