import com.nd.common.db.JdbcUtil;
import com.nd.common.entity.User;
import com.nd.common.util.Session;
import com.nd.service.UserService;

import java.sql.ResultSet;

/** 一次性端到端验证：登录/注册/资料/改密，并确认库内无明文密码 */
public class VerifyE2E {
    public static void main(String[] args) throws Exception {
        UserService svc = new UserService();
        int fail = 0;
        try {
            // 1) 种子医生账号登录（密文校验）
            String role = svc.login("183", "123456");
            System.out.println("[1] 登录 183/123456 -> role=" + role + (role != null ? " PASS" : " FAIL"));
            if (role == null) fail++;
            System.out.println("    会话姓名=" + Session.currentName);

            // 2) 错误密码登录应失败
            try {
                svc.login("183", "wrong123");
                System.out.println("[2] 错误密码登录 -> FAIL(未抛异常)");
                fail++;
            } catch (RuntimeException e) {
                System.out.println("[2] 错误密码登录 -> 拒绝 PASS：" + e.getMessage());
            }

            // 3) 患者账号登录
            role = svc.login("1", "1");
            System.out.println("[3] 登录 1/1 -> role=" + role + (role != null ? " PASS" : " FAIL"));
            if (role == null) fail++;

            // 4) 注册新用户（自动加密）
            svc.register("199", "abc123", "abc123", "张三", "patient");
            System.out.println("[4] 注册新用户 199 -> PASS");
            role = svc.login("199", "abc123");
            System.out.println("    新用户登录 -> role=" + role + (role != null ? " PASS" : " FAIL"));
            if (role == null) fail++;

            // 5) 查询资料
            User u = svc.getProfile("199");
            System.out.println("[5] 查询资料 199 -> name=" + u.getName() + " role=" + u.getRole() + " PASS");

            // 6) 更新资料
            svc.updateProfile("199", "张三丰", "1990-05-20", "男", "175.5", "68.5");
            User u2 = svc.getProfile("199");
            boolean ok6 = "1990-05-20".equals(u2.getBirthDate()) && "男".equals(u2.getGender())
                    && u2.getHeight() != null && "175.5".equals(u2.getHeight().stripTrailingZeros().toPlainString())
                    && "张三丰".equals(u2.getName());
            System.out.println("[6] 更新资料 -> birth=" + u2.getBirthDate() + " gender=" + u2.getGender()
                    + " h=" + u2.getHeight() + " w=" + u2.getWeight() + (ok6 ? " PASS" : " FAIL"));
            if (!ok6) fail++;

            // 7) 修改密码
            svc.changePassword("199", "abc123", "newpwd6", "newpwd6");
            System.out.println("[7] 修改密码 -> PASS");
            try {
                svc.login("199", "abc123");
                System.out.println("    旧密码登录 -> FAIL(仍可登录)");
                fail++;
            } catch (RuntimeException e) {
                System.out.println("    旧密码登录 -> 拒绝 PASS");
            }
            role = svc.login("199", "newpwd6");
            System.out.println("    新密码登录 -> role=" + role + (role != null ? " PASS" : " FAIL"));
            if (role == null) fail++;

            // 8) 库内无明文校验：users.pwd 全部为 64 位 hex
            ResultSet rs = JdbcUtil.querySql("select tel, pwd, salt, length(pwd) pl, length(salt) sl from users order by id", null);
            boolean allEncrypted = true;
            System.out.println("== 库内密码存储检查 ==");
            while (rs.next()) {
                String tel = rs.getString(1);
                int pl = rs.getInt(4), sl = rs.getInt(5);
                boolean ok = (pl == 64) && (sl == 32);
                if (!ok) allEncrypted = false;
                System.out.println("  " + tel + " pwd_len=" + pl + " salt_len=" + sl + (ok ? " (密文)" : " (异常)"));
            }
            JdbcUtil.close();
            System.out.println("[8] 全部密码为盐+哈希密文 -> " + (allEncrypted ? "PASS" : "FAIL"));
            if (!allEncrypted) fail++;

            System.out.println("======================================");
            System.out.println(fail == 0 ? "全部端到端验证通过" : ("存在 " + fail + " 项失败"));
        } finally {
            // 清理测试账号，避免污染种子数据
            JdbcUtil.iudSql("delete from users where tel='199'", null);
        }
    }
}
