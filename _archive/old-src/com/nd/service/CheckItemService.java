package com.nd.service;

import com.nd.bean.CheckItem;
import com.nd.dao.CheckItemDao;

import java.sql.SQLException;
import java.util.List;

public class CheckItemService {
    CheckItemDao checkItemDao = new CheckItemDao();

    public List<CheckItem> getCheckItemData() throws SQLException {
        List<CheckItem> list = checkItemDao.getCheckItemData();
        if(list == null || list.size() == 0){
            throw new RuntimeException("暂无检查项，请先维护数据！");
        }
        return list;
    }

}
