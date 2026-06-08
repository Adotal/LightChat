import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/** Ejecuta un archivo .sql (sentencias separadas por ';') contra la BD de .env. */
public class RunSql {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: RunSql <archivo.sql>");
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
                st.execute(stmt);
                System.out.println("  [OK] " + stmt.lines().findFirst().orElse(stmt).trim());
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
