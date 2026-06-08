import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

/** Ejecuta SELECTs de un archivo .sql (separados por ';') e imprime resultados. Solo lectura. */
public class QuerySql {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: QuerySql <archivo.sql>");
            System.exit(1);
        }
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        }
        String url = props.getProperty("DB_URL", System.getenv("DB_URL"));
        String user = props.getProperty("DB_USER", System.getenv("DB_USER"));
        String password = props.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));

        String sql = new String(Files.readAllBytes(Path.of(args[0])));

        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("[INFO] Conectando a " + url + " ...");
        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement()) {
            for (String raw : sql.split(";")) {
                String stmt = stripComments(raw).trim();
                if (stmt.isEmpty()) continue;
                System.out.println("\n>>> " + stmt);
                try (ResultSet rs = st.executeQuery(stmt)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int n = md.getColumnCount();
                    StringBuilder head = new StringBuilder();
                    for (int i = 1; i <= n; i++) head.append(md.getColumnLabel(i)).append(" | ");
                    System.out.println(head);
                    while (rs.next()) {
                        StringBuilder row = new StringBuilder();
                        for (int i = 1; i <= n; i++) row.append(rs.getString(i)).append(" | ");
                        System.out.println(row);
                    }
                }
            }
            System.out.println("[INFO] Listo.");
        }
    }

    private static String stripComments(String s) {
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n")) {
            if (line.trim().startsWith("--")) continue;
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
