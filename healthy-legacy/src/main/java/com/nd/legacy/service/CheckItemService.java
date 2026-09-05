package com.nd.legacy.service;

import com.nd.legacy.bean.CheckItem;
import com.nd.legacy.dao.CheckItemDao;

import java.sql.SQLException;
import java.util.List;

/**
 * 【遗留模块】旧版检查项业务逻辑层（Service 层）。
 *
 * <p>所属模块：healthy-legacy（遗留模块，包含早期版本的代码，与新模块 healthy-common 等相互独立）。</p>
 *
 * <p>类的职责：作为 View 层与 DAO 层之间的中间层，封装检查项相关的业务逻辑，
 * 对 DAO 层返回的数据进行业务校验和异常处理。</p>
 *
 * <p>核心功能点：</p>
 * <ul>
 *   <li>调用 DAO 层获取检查项数据；</li>
 *   <li>对查询结果进行非空校验，若数据为空则抛出业务运行时异常提示用户。</li>
 * </ul>
 *
 * <p>关键依赖：</p>
 * <ul>
 *   <li>{@link CheckItemDao}：遗留模块的检查项数据访问对象，负责底层数据库查询；</li>
 *   <li>{@link CheckItem}：遗留模块的检查项实体类。</li>
 * </ul>
 *
 * <p>注意：本类未被当前主程序引用，仅保留参考。</p>
 *
 * @author HealthySys 遗留模块
 */
public class CheckItemService {

    /**
     * 旧版检查项数据访问对象，用于执行数据库查询操作。
     * 声明为 final 表示初始化后不再重新赋值。
     */
    private final CheckItemDao checkItemDao = new CheckItemDao();

    /**
     * 获取启用状态的检查项列表。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>调用 {@link CheckItemDao#getCheckItemData()} 从数据库获取检查项列表；</li>
     *   <li>对返回结果进行非空和非空集合校验；</li>
     *   <li>若列表为 null 或 size 为 0，抛出 {@link RuntimeException} 提示暂无数据；</li>
     *   <li>校验通过后返回检查项列表。</li>
     * </ol>
     *
     * @return 旧版检查项列表（非空）
     * @throws SQLException       数据库访问异常，由 DAO 层查询数据库时发生的错误触发
     * @throws RuntimeException   业务异常，当数据库中暂无检查项数据（list 为 null 或空集合）时抛出
     */
    public List<CheckItem> getCheckItemData() throws SQLException {
        // 调用 DAO 层从数据库查询启用状态的检查项数据
        List<CheckItem> list = checkItemDao.getCheckItemData();
        // 业务校验：判断查询结果是否为空（null 或集合大小为 0）
        if (list == null || list.size() == 0) {
            // 数据为空时抛出运行时异常，提示管理员先维护检查项数据
            throw new RuntimeException("暂无检查项，请先维护数据！");
        }
        // 校验通过，返回检查项列表
        return list;
    }
}
