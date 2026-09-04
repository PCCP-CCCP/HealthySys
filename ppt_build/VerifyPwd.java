import com.nd.common.util.PasswordUtil;
import com.nd.common.db.JdbcUtil;
import java.sql.ResultSet;

/** 一次性验证程序：密码加密逻辑自检 + 检查 users 表当前结构 */
public class VerifyPwd {
    public static void main(String[] args) throws Exception {
        // 1) 加密逻辑自检
        System.out.println("== PasswordUtil 自检 ==");
        String salt = PasswordUtil.generateSalt();
        String h1 = PasswordUtil.hash("123456", salt);
        String h2 = PasswordUtil.hash("123456", salt);
        String h3 = PasswordUtil.hash("1234567", salt);
        System.out.println("salt len=" + salt.length() + " hash len=" + h1.length());
        System.out.println("相同盐+同密码 -> 相同密文: " + h1.equals(h2));
        System.out.println("同盐不同密码 -> 不同密文: " + !h1.equals(h3));
        System.out.println("verify 正确密码: " + PasswordUtil.verify("123456", salt, h1));
        System.out.println("verify 错误密码: " + !PasswordUtil.verify("wrong", salt, h1));
        System.out.println("verify 空 salt: " + !PasswordUtil.verify("123456", null, h1));

        // 2) 数据库连接 + users 表结构检查
        System.out.println("== 数据库检查 ==");
        try {
            ResultSet rs = JdbcUtil.querySql("show columns from users", null);
            StringBuilder cols = new StringBuilder();
            while (rs.next()) {
                cols.append(rs.getString(1)).append(",");
            }
            JdbcUtil.close();
            System.out.println("users 列: " + cols);
            boolean hasSalt = cols.toString().contains("salt");
            System.out.println("已有 salt 列: " + hasSalt);
            if (!hasSalt) {
                System.out.println("结论: 当前库为旧结构，需执行 resources/init.sql 重建表");
            } else {
                ResultSet rs2 = JdbcUtil.querySql("select tel, pwd, salt from users", null);
                while (rs2.next()) {
                    System.out.println("用户 " + rs2.getString(1) + " pwd=" + rs2.getString(2) + " salt=" + rs2.getString(3));
                }
                JdbcUtil.close();
            }
        } catch (Exception e) {
            System.out.println("数据库检查失败: " + e.getMessage());
        }
    }
}
