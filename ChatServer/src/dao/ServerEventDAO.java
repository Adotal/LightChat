package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla server_events: registro persistente de los eventos
 * generados por usuarios y por el servidor (RQNF90). Cada evento lleva tipo,
 * descripción y marca temporal (RQNF94).
 *
 * @author adotal
 */
public class ServerEventDAO extends DatabaseConnection {

    public ServerEventDAO() {
        super();
    }

    /** Inserta un evento y devuelve su marca temporal generada (o null). */
    public String insertEvent(String type, String description) {
        String sql = "INSERT INTO server_events (type, description) VALUES (?, ?)";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, type);
            ps.setString(2, description);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                String fetch = "SELECT created_at FROM server_events WHERE id_event = ?";
                try (PreparedStatement ps2 = getCon().prepareStatement(fetch)) {
                    ps2.setInt(1, id);
                    ResultSet rs = ps2.executeQuery();
                    if (rs.next()) {
                        return rs.getString("created_at");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[ServerEventDAO] Error al insertar evento: " + e.getMessage());
        }
        return null;
    }

    /** Devuelve los eventos más recientes (orden cronológico) para la vista admin. */
    public List<String> getRecentEvents(int limit) {
        List<String> events = new ArrayList<>();
        String sql = "SELECT created_at, type, description FROM server_events "
                + "ORDER BY id_event DESC LIMIT ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            List<String> reversed = new ArrayList<>();
            while (rs.next()) {
                reversed.add("[" + rs.getString("created_at") + "] "
                        + rs.getString("type") + " - " + rs.getString("description"));
            }
            // Devolver en orden cronológico ascendente
            for (int i = reversed.size() - 1; i >= 0; i--) {
                events.add(reversed.get(i));
            }
        } catch (SQLException e) {
            System.out.println("[ServerEventDAO] Error al obtener eventos: " + e.getMessage());
        }
        return events;
    }
}
