import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Aplica schema.sql a la base de datos configurada en ChatServer/.env usando el
 * driver MySQL incluido en /lib (no requiere el cliente mysql instalado).
 *
 * Uso (ver apply-schema.sh): se ejecuta desde el directorio ChatServer para que
 * encuentre .env y schema.sql con rutas relativas.
 */
public class ApplySchema {

    public static void main(String[] args) throws Exception {
        // Cargar .env (mismo formato que DatabaseConnection)
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        }
        String url = props.getProperty("DB_URL", System.getenv("DB_URL"));
        String user = props.getProperty("DB_USER", System.getenv("DB_USER"));
        String password = props.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));

        if (url == null || user == null) {
            System.err.println("[ERROR] Faltan DB_URL/DB_USER en .env");
            System.exit(1);
        }

        String sql = new String(Files.readAllBytes(Path.of("schema.sql")));

        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("[INFO] Conectando a " + url + " ...");
        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement()) {

            int aplicadas = 0;
            // Separar por ';' al final de sentencia (el esquema no usa procedimientos).
            for (String raw : sql.split(";")) {
                String stmt = stripComments(raw).trim();
                if (stmt.isEmpty()) {
                    continue;
                }
                st.execute(stmt);
                aplicadas++;
                String firstLine = stmt.lines().findFirst().orElse(stmt);
                System.out.println("  [OK] " + firstLine.trim());
            }
            System.out.println("[INFO] Listo. " + aplicadas + " sentencia(s) aplicada(s).");
        }
    }

    /** Quita líneas de comentario que empiezan con '--'. */
    private static String stripComments(String s) {
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) {
                continue;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
