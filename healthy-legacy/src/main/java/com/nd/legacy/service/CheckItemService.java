package com.nd.legacy.service;

import com.nd.legacy.bean.CheckItem;
import com.nd.legacy.dao.CheckItemDao;

import java.sql.SQLException;
import java.util.List;

/**
 * 【遗留】旧版检查项业务逻辑层（未被当前主程序引用，仅保留参考）。
 *
 * @author HealthySys 遗留模块
 */
public class CheckItemService {

    /** 旧版检查项数据访问对象 */
    private final CheckItemDao checkItemDao = new CheckItemDao();

    /**
     * 获取启用状态的检查项列表；为空时抛出业务异常。
     *
     * @return 旧版检查项列表
     * @throws SQLException 数据库访问异常
     * @throws RuntimeException 暂无检查项数据时抛出
     */
    public List<CheckItem> getCheckItemData() throws SQLException {
        List<CheckItem> list = checkItemDao.getCheckItemData();
        if (list == null || list.size() == 0) {
            throw new RuntimeException("暂无检查项，请先维护数据！");
        }
        return list;
    }
}
