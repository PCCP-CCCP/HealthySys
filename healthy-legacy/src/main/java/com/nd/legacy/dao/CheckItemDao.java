package com.nd.legacy.dao;

import com.nd.common.db.JdbcUtil;
import com.nd.legacy.bean.CheckItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 【遗留】旧版检查项数据访问对象（未被当前主程序引用，仅保留参考）。
 *
 * <p>基于旧版 checkitem 表字段（cid/bh/cname/dw/ckfw/status）查询，
 * 使用公共模块的 {@link JdbcUtil} 访问数据库。</p>
 *
 * @author HealthySys 遗留模块
 */
public class CheckItemDao {

    /**
     * 查询状态为启用（status=0）的全部旧版检查项。
     *
     * @return 旧版检查项列表
     * @throws SQLException 数据库访问异常
     */
    public List<CheckItem> getCheckItemData() throws SQLException {
        String sql = "select * from checkitem where status = ?";
        Object[] param = {0};
        ResultSet rs = JdbcUtil.querySql(sql, param);
        List<CheckItem> list = new ArrayList<CheckItem>();
        while (rs.next()) {
            CheckItem checkItem = new CheckItem();
            checkItem.setCid(rs.getString("cid"));
            checkItem.setBh(rs.getString("bh"));
            checkItem.setDw(rs.getString("dw"));
            checkItem.setCname(rs.getString("cname"));
            checkItem.setCkfw(rs.getString("ckfw"));
            checkItem.setStatus(rs.getInt("status"));
            list.add(checkItem);
        }
        JdbcUtil.close();
        return list;
    }
}
