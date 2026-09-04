import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** 一次性工具：执行 resources/init.sql（重建表 + 写入加密种子数据） */
public class InitDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/?characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";
        String sqlFile = args[0];
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFile, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("--")) {
                    continue;
                }
                sb.append(line).append("\n");
            }
        }
        String[] stmts = sb.toString().split(";");
        try (Connection con = DriverManager.getConnection(url, "root", "Youlhsj19.");
             Statement st = con.createStatement()) {
            int ok = 0;
            for (String s : stmts) {
                if (s.trim().isEmpty()) {
                    continue;
                }
                st.execute(s.trim());
                ok++;
            }
            System.out.println("executed statements: " + ok);
        }
    }
}
