import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/** Inspecciona la estructura real de la tabla friendships en la BD de .env. */
public class DescribeFriendships {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        }
        String url = props.getProperty("DB_URL", System.getenv("DB_URL"));
        String user = props.getProperty("DB_USER", System.getenv("DB_USER"));
        String password = props.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement()) {
            System.out.println("=== DESCRIBE friendships ===");
            try (ResultSet rs = st.executeQuery("DESCRIBE friendships")) {
                while (rs.next()) {
                    System.out.printf("%-20s %-20s key=%s extra=%s%n",
                            rs.getString("Field"), rs.getString("Type"),
                            rs.getString("Key"), rs.getString("Extra"));
                }
            }
        }
    }
}
