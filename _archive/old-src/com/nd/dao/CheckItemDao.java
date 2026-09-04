package com.nd.dao;

import com.nd.bean.CheckItem;
import com.nd.view.utils.JdbcUitl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CheckItemDao {
    JdbcUitl jdbcUitl = new JdbcUitl();

    public List<CheckItem> getCheckItemData() throws SQLException {
        String sql = "select * from checkitem where status = ?";
        Object[] param = {0};
        ResultSet rs = jdbcUitl.querySql(sql,param);
        List<CheckItem> list = new ArrayList<CheckItem>();
        while (rs.next()){
            CheckItem checkItem = new CheckItem();
            checkItem.setCid(rs.getString("cid"));
            checkItem.setBh(rs.getString("bh"));
            checkItem.setDw(rs.getString("dw"));
            checkItem.setCname(rs.getString("cname"));
            checkItem.setCkfw(rs.getString("ckfw"));
            checkItem.setStatus(rs.getInt("status"));
            list.add(checkItem);
        }
        jdbcUitl.close();
        return list;
    }

}
