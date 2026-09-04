# healthy-service · 业务逻辑层模块

## 模块职责

承载业务规则与校验逻辑，向上为界面层提供「封装好的业务操作」，向下调用 `healthy-dao` 与 `healthy-common`。当前覆盖登录 / 注册 / 个人信息维护 / 密码修改等用户业务，并是**密码加密的唯一入口**。

## 包结构

```
com.nd.service
└── UserService.java   # 用户业务：登录校验、注册校验、资料查询/更新、修改密码
```

## 方法清单与功能说明

| 方法 | 功能 |
| --- | --- |
| `login(tel, pwd)` | 登录：参数非空校验 + 按账号取盐重算密文比对，成功返回角色并写入会话姓名 |
| `register(...)` | 注册：入参校验 + 手机号查重 + **生成随机盐并加密密码**后写入 |
| `getProfile(tel)` | 查询当前用户个人资料（含出生日期/性别/身高/体重） |
| `updateProfile(tel, name, birthDate, gender, height, weight)` | 修改个人资料：姓名非空/数值合法性校验后更新 |
| `changePassword(tel, oldPwd, newPwd, confirmPwd)` | 修改密码：校验原密码 → 换新盐新密文更新 |

## 关键说明

- 业务校验规则（非空、格式、唯一性、原密码正确性）集中在本层，界面层不直接拼 SQL 或做重复校验。
- **密码安全是本层的核心职责**：注册/登录/改密的加解密都经由 `PasswordUtil`（`healthy-common`）完成，明文密码仅在本层方法栈中短暂存在，不落库、不入会话。
- 后续如需扩展（如「预约日期不能早于今天」「同一用户同组同日期不可重复预约」），在此层新增方法即可。

## 依赖

- healthy-common（实体、Session、PasswordUtil）
- healthy-dao（UserDao 等）

## 多人协作建议

业务规则是多人协作中**最容易产生冲突**的地方。建议约定：新增业务方法一律走 `Service → Dao`，面板只做展示与交互，不写业务 SQL。业务层开发与数据层开发可并行，先约定方法签名即可。
