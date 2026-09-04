-- ============================================================
-- 体检中心管理系统 数据库初始化脚本
-- 数据库: newcenter (与 JdbcUitl 中的连接地址保持一致)
-- 说明: 本脚本会重建 checkitem / users 两张表 (先删后建)，
--       并写入示例数据，便于 MainView 启动后立即展示。
-- 请在 Navicat / MySQL Workbench / mysql 命令行中执行本文件。
-- ============================================================

CREATE DATABASE IF NOT EXISTS newcenter DEFAULT CHARACTER SET utf8mb4;
USE newcenter;

-- ------------------------------------------------------------
-- 检查项表 (MainView / CreateCheckItemDialog 使用)
-- 对应实体: com.nd.view.entity.CheckItem
-- ------------------------------------------------------------
DROP TABLE IF EXISTS checkitem;
CREATE TABLE checkitem (
    id          INT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100)   NOT NULL COMMENT '检查项名称',
    category    VARCHAR(50)             COMMENT '分类',
    price       DECIMAL(10, 2)          COMMENT '价格(元)',
    description VARCHAR(500)            COMMENT '描述',
    create_time DATETIME                COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '检查项';

INSERT INTO checkitem (name, category, price, description, create_time) VALUES
('血常规', '检验类', 30.00, '红细胞、白细胞、血小板等指标', NOW()),
('肝功能五项', '检验类', 80.00, 'ALT、AST、胆红素等指标', NOW()),
('胸部X光', '影像类', 120.00, '肺部及心脏影像检查', NOW()),
('心电图', '功能类', 45.00, '心脏电生理检查', NOW()),
('腹部彩超', '影像类', 150.00, '肝、胆、胰、脾、肾超声检查', NOW());

-- ------------------------------------------------------------
-- 用户表 (LoginView / UserDao 使用)
-- 字段: tel(账号) pwd(密码) name(姓名) role(角色: doctor=医生账号/可切换, patient=仅患者)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id   INT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tel  VARCHAR(20) NOT NULL COMMENT '账号(手机号)',
    pwd  VARCHAR(50) NOT NULL COMMENT '密码',
    name VARCHAR(50)          COMMENT '姓名',
    role VARCHAR(20) DEFAULT 'patient' COMMENT '角色: doctor=医生, patient=患者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tel (tel)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户';

INSERT INTO users (tel, pwd, name, role) VALUES
('183', '123456', '管理员', 'doctor');

-- ------------------------------------------------------------
-- 检查组表 & 检查组-检查项关联表 (检查组管理模块)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS checkgroup_item;
DROP TABLE IF EXISTS checkgroup;
CREATE TABLE checkgroup (
    id          INT NOT NULL AUTO_INCREMENT COMMENT '组ID',
    name        VARCHAR(100) NOT NULL COMMENT '检查组名称',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '检查组';

CREATE TABLE checkgroup_item (
    group_id INT NOT NULL COMMENT '组ID',
    item_id  INT NOT NULL COMMENT '检查项ID',
    PRIMARY KEY (group_id, item_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '检查组-检查项关联表';

INSERT INTO checkgroup (name, create_time) VALUES
('入职体检套餐', NOW()),
('中老年体检套餐', NOW());

INSERT INTO checkgroup_item (group_id, item_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 2), (2, 4), (2, 5);

-- ------------------------------------------------------------
-- 预约表 & 检查结果表 (预约与跟踪模块)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS exam_result;
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
    id          INT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    user_tel    VARCHAR(20) NOT NULL COMMENT '用户账号',
    group_id    INT NOT NULL COMMENT '检查组ID',
    exam_date   VARCHAR(10) NOT NULL COMMENT '预约日期',
    status      VARCHAR(20) DEFAULT '已预约' COMMENT '状态',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '体检预约';

CREATE TABLE exam_result (
    id             INT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    appointment_id INT NOT NULL COMMENT '预约ID',
    item_id        INT NOT NULL COMMENT '检查项ID',
    item_value     VARCHAR(50) COMMENT '检测数值',
    result_status  VARCHAR(20) COMMENT '正常/异常',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '检查结果';

INSERT INTO appointment (user_tel, group_id, exam_date, status, create_time) VALUES
('183', 1, '2026-01-15', '已完成', NOW()),
('183', 1, '2026-06-20', '已完成', NOW()),
('183', 2, '2026-09-01', '已预约', NOW());

INSERT INTO exam_result (appointment_id, item_id, item_value, result_status) VALUES
(1, 1, '5.2', '正常'),
(1, 2, '72', '正常'),
(1, 3, '11.5', '正常'),
(2, 1, '5.8', '正常'),
(2, 2, '95', '异常'),
(2, 3, '25.8', '异常');

-- 实现检查组管理模块：1.创建检查组: 勾选多个检查项形成检查组；2.检查组查询: 通过搜索检查组名称和组内检查项查询检查组；3.删除检查组分组：4.修改检查组分组：检查项从检查组中分离、检查项加入检查组；
-- 实现预约与跟踪：1.预约体检；2.体检检查组选择；3.用户历史检查结果对比与分析。