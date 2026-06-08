package dao;

/**
 *
 * @author GLENNCAMILOGOITIASAN
 */
import database.DatabaseConnection;
import model.FriendRequest;
import model.Request.RequestStatus;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla friendships.
 *
 * La tabla "friendships" maneja tres cosas en una sola:
 *  1. Solicitudes pendientes  (status = PENDING)
 *  2. Amistades confirmadas   (status = APPROVED)
 *  3. Solicitudes rechazadas  (status = DENIED)
 *
 * Estructura (ver schema.sql):
 *  id_friendship (PK), id_sender (FK users), id_receiver (FK users),
 *  created_at (timestamp), status ENUM(PENDING, APPROVED, DENIED)
 */
public class FriendshipDAO extends DatabaseConnection {

    public FriendshipDAO() {
        super();
    }

    // Crear --------------------------------------------------------------
    /*
        Inserta una solicitud en estado PENDING.
        Antes valida que no sean ya amigos (areFriends) y que no exista
        una solicitud previa entre ellos (requestAlreadyExists).
        Devuelve el id de la solicitud creada, o -1 si no se creó.
     */
    public int sendFriendRequest(int idSender, int idReceiver) {
        if (areFriends(idSender, idReceiver)) {
            System.out.println("[FriendshipDAO] Los usuarios " + idSender
                    + " y " + idReceiver + " ya son amigos.");
            return -1;
        }
        if (requestAlreadyExists(idSender, idReceiver)) {
            System.out.println("[FriendshipDAO] Ya existe una solicitud entre "
                    + idSender + " y " + idReceiver + ".");
            return -1;
        }

        String sql = "INSERT INTO friendships (id_sender, id_receiver, status) "
                + "VALUES (?, ?, 'PENDING')";
        try (PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idSender);
            ps.setInt(2, idReceiver);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                // El INSERT no afectó ninguna fila: la solicitud no se creó.
                return -1;
            }

            // El éxito se determina por filas afectadas, no por la presencia de
            // clave generada: si la tabla no tiene AUTO_INCREMENT, el INSERT
            // igual es válido y no debe reportarse como fallo.
            int id = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
            if (id == -1) {
                // Sin clave autogenerada: recuperar el id real de la solicitud
                // recién insertada para devolver un identificador utilizable.
                id = getRequestId(idSender, idReceiver);
            }
            System.out.println("[FriendshipDAO] Solicitud " + id + " enviada de "
                    + idSender + " a " + idReceiver);
            return id;
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al enviar solicitud: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Devuelve el id de la solicitud (más reciente) entre dos usuarios en esa
     * dirección, o -1 si no existe. Sirve de respaldo cuando el driver no
     * devuelve la clave autogenerada del INSERT.
     */
    private int getRequestId(int idSender, int idReceiver) {
        String sql = "SELECT id_friendship FROM friendships "
                + "WHERE id_sender = ? AND id_receiver = ? "
                + "ORDER BY id_friendship DESC LIMIT 1";
        try (PreparedStatement ps = getCon().prepareStatement(sql)) {
            ps.setInt(1, idSender);
            ps.setInt(2, idReceiver);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_friendship");
                }
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al recuperar id de solicitud: " + e.getMessage());
        }
        return -1;
    }

    // Lectura ------------------------------------------------------------
    /*
        Devuelve la lista de amigos confirmados (APPROVED) de un usuario.
        Bidireccional: el usuario puede ser remitente o destinatario.
     */
    public List<User> getFriendsByUser(int idUser) {
        List<User> friends = new ArrayList<>();

        String sql
                = "SELECT "
                + "  CASE WHEN f.id_sender = ? THEN u2.id_user      ELSE u1.id_user      END AS id_user, "
                + "  CASE WHEN f.id_sender = ? THEN u2.name         ELSE u1.name         END AS name, "
                + "  CASE WHEN f.id_sender = ? THEN u2.email        ELSE u1.email        END AS email, "
                + "  CASE WHEN f.id_sender = ? THEN u2.is_connected ELSE u1.is_connected END AS is_connected, "
                + "  CASE WHEN f.id_sender = ? THEN u2.last_access  ELSE u1.last_access  END AS last_access "
                + "FROM friendships f "
                + "JOIN users u1 ON f.id_sender   = u1.id_user "
                + "JOIN users u2 ON f.id_receiver = u2.id_user "
                + "WHERE (f.id_sender = ? OR f.id_receiver = ?) "
                + "  AND f.status = 'APPROVED'";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            for (int i = 1; i <= 5; i++) {
                ps.setInt(i, idUser);
            }
            ps.setInt(6, idUser);
            ps.setInt(7, idUser);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                friends.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                ));
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al obtener amigos: " + e.getMessage());
        }
        return friends;
    }

    /*
        Solicitudes PENDIENTES recibidas por un usuario (es el receptor).
        Devuelve FriendRequest con el User remitente para la UI.
     */
    public List<FriendRequest> getPendingRequests(int idReceiver) {
        List<FriendRequest> requests = new ArrayList<>();

        String sql
                = "SELECT f.id_friendship, "
                + "       u.id_user, u.name, u.email, u.is_connected, u.last_access "
                + "FROM friendships f "
                + "JOIN users u ON f.id_sender = u.id_user "
                + "WHERE f.id_receiver = ? AND f.status = 'PENDING'";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idReceiver);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User sender = new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                );
                requests.add(new FriendRequest(
                        rs.getInt("id_friendship"),
                        sender,
                        null,
                        RequestStatus.PENDING
                ));
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al obtener solicitudes pendientes: " + e.getMessage());
        }
        return requests;
    }

    /*
        Solicitudes ENVIADAS por un usuario (es el remitente), en cualquier
        estado, para que vea PENDIENTE/ACEPTADO/RECHAZADO (RQF19/RQNF33).
        Devuelve FriendRequest con el User destinatario.
     */
    public List<FriendRequest> getSentRequests(int idSender) {
        List<FriendRequest> requests = new ArrayList<>();

        String sql
                = "SELECT f.id_friendship, f.status, "
                + "       u.id_user, u.name, u.email, u.is_connected, u.last_access "
                + "FROM friendships f "
                + "JOIN users u ON f.id_receiver = u.id_user "
                + "WHERE f.id_sender = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idSender);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User target = new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                );
                requests.add(new FriendRequest(
                        rs.getInt("id_friendship"),
                        null,
                        target,
                        RequestStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al obtener solicitudes enviadas: " + e.getMessage());
        }
        return requests;
    }

    /*
        Verifica si dos usuarios son amigos (APPROVED). Bidireccional.
     */
    public boolean areFriends(int idUser1, int idUser2) {
        String sql
                = "SELECT COUNT(*) FROM friendships "
                + "WHERE ((id_sender = ? AND id_receiver = ?) "
                + "    OR (id_sender = ? AND id_receiver = ?)) "
                + "  AND status = 'APPROVED'";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser1);
            ps.setInt(2, idUser2);
            ps.setInt(3, idUser2);
            ps.setInt(4, idUser1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al verificar amistad: " + e.getMessage());
        }
        return false;
    }

    /*
        Verifica si ya existe una solicitud entre dos usuarios en cualquier
        estado (ambas direcciones).
     */
    public boolean requestAlreadyExists(int idSender, int idReceiver) {
        String sql
                = "SELECT COUNT(*) FROM friendships "
                + "WHERE (id_sender = ? AND id_receiver = ?) "
                + "   OR (id_sender = ? AND id_receiver = ?)";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idSender);
            ps.setInt(2, idReceiver);
            ps.setInt(3, idReceiver);
            ps.setInt(4, idSender);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al verificar solicitud existente: " + e.getMessage());
        }
        return false;
    }

    /*
        Devuelve los dos ids de usuario implicados en una solicitud, o null.
        Útil para notificar a ambos al aceptar/rechazar.
     */
    public int[] getUsersOfRequest(int idFriendship) {
        String sql = "SELECT id_sender, id_receiver FROM friendships WHERE id_friendship = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idFriendship);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new int[]{rs.getInt("id_sender"), rs.getInt("id_receiver")};
            }
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al obtener usuarios de la solicitud: " + e.getMessage());
        }
        return null;
    }

    // Actualizar ---------------------------------------------------------
    public void acceptRequest(int idFriendship) {
        updateStatus(idFriendship, "APPROVED");
        System.out.println("[FriendshipDAO] Solicitud " + idFriendship + " aceptada.");
    }

    public void denyRequest(int idFriendship) {
        updateStatus(idFriendship, "DENIED");
        System.out.println("[FriendshipDAO] Solicitud " + idFriendship + " denegada.");
    }

    private void updateStatus(int idFriendship, String newStatus) {
        String sql = "UPDATE friendships SET status = ? WHERE id_friendship = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, idFriendship);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al actualizar estado: " + e.getMessage());
        }
    }

    // Eliminar -----------------------------------------------------------
    public void deleteFriendship(int idFriendship) {
        String sql = "DELETE FROM friendships WHERE id_friendship = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idFriendship);
            int rows = ps.executeUpdate();
            System.out.println("[FriendshipDAO] Amistad " + idFriendship
                    + " eliminada (" + rows + " fila/s).");
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al eliminar amistad: " + e.getMessage());
        }
    }
}
