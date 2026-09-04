package com.nd.view.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Demo {

    public static void main(String[] args) throws SQLException {
        JdbcUitl jdbcUitl = new JdbcUitl();

//        Object param[] = {"183"};
//        String sql = "select * from users where tel = ?";
//        ResultSet rs = jdbcUitl.querySql(sql,param);
//        while (rs.next()){
//            System.out.println(rs.getString("name"));
//        }
        Object param[] = {"888","133"};
        String sql = "update users set pwd = ? where tel = ?";
        int num = jdbcUitl.iudSql(sql,param);

        if(num == 0){
            System.out.println("0000");
        }else {
            System.out.println("111");
        }

    }

}
