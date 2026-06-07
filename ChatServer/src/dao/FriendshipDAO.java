
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
 * DAO para la tabla amistades.
 *
 * La tabla "amistades" maneja dos cosas distintas en una sola:
 *   1. Las solicitudes de amistad pendientes (estado = PENDING)
 *   2. Las amistades ya confirmadas        (estado = APPROVED)
 *   3. Las solicitudes rechazadas          (estado = DENIED)
 *
 * Estructura de la tabla:
 *   ID_amistad    (PK)
 *   ID_remitente  (FK → Usuarios) — quien envía la solicitud
 *   ID_destinatario (FK → Usuarios) — quien la recibe
 *   fecha         (timestamp) — fecha de la solicitud
 *   estado        (enum: PENDING, APPROVED, DENIED)
 *
 * Responsabilidades de esta clase:
 *   1. Enviar solicitud de amistad         (sendFriendRequest)
 *   2. Obtener amigos confirmados          (getFriendsByUser)
 *   3. Obtener solicitudes pendientes      (getPendingRequests)
 *   4. Verificar si dos usuarios son amigos (areFriends)
 *   5. Verificar si ya existe una solicitud (requestAlreadyExists)
 *   6. Aceptar solicitud                   (acceptRequest)
 *   7. Denegar solicitud                   (denyRequest)
 *   8. Eliminar amistad                    (deleteFriendship)
 */
public class FriendshipDAO {
 
    // Crear
 
    /* 
        Inserta una solicitud de amistad en estado PENDING pendiente
        Antes de insertar verifica: que no sean amigas ya (areFriends) y que no exista ya una solicitud de amistad pendiente entre ellos (requestAlreadyExist)
        esto para evitar duplicados
    */
    public void sendFriendRequest(int idRemitente, int idDestinatario) {
        if (areFriends(idRemitente, idDestinatario)) {
            System.out.println("[FriendshipDAO] Los usuarios " + idRemitente
                    + " y " + idDestinatario + " ya son amigos.");
            return;
        }
        if (requestAlreadyExists(idRemitente, idDestinatario)) {
            System.out.println("[FriendshipDAO] Ya existe una solicitud pendiente entre "
                    + idRemitente + " y " + idDestinatario + ".");
            return;
        }
 
        String sql = "INSERT INTO amistades (ID_remitente, ID_destinatario, fecha, estado) " +
                     "VALUES (?, ?, NOW(), 'PENDING')";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idRemitente);
            stmt.setInt(2, idDestinatario);
            stmt.executeUpdate();
            System.out.println("[FriendshipDAO] Solicitud enviada de " + idRemitente
                    + " a " + idDestinatario);
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al enviar solicitud: " + e.getMessage());
        }
    }
 
    // Lectura
    /* 
        Devuelve la lista de amigos confirmados (estado APPROVED) de un usuario
        Las solicitudes son bidireccionales, un usuario puede ser remitente o desrtinatario, por eso en el WHERE hay un OR. 
        Hace JOIN con Usuarios para devolver objetos USER del amigo del usuario y agragarlo a amigos
        Sirve para cargar el panel de amigos conectados y desconectados al iniciar sesion
    */
    public List<User> getFriendsByUser(int idUsuario) {
        List<User> friends = new ArrayList<>();
 
        // Si el usuario es remitente, el amigo es el destinatario y viceversa
        String sql =
            "SELECT " +
            "  CASE WHEN a.ID_remitente = ? THEN u2.id_usuario ELSE u1.id_usuario END AS id_usuario, " +
            "  CASE WHEN a.ID_remitente = ? THEN u2.Nombre     ELSE u1.Nombre     END AS Nombre, " +
            "  CASE WHEN a.ID_remitente = ? THEN u2.Email      ELSE u1.Email      END AS Email, " +
            "  CASE WHEN a.ID_remitente = ? THEN u2.Contraseña ELSE u1.Contraseña END AS Contraseña, " +
            "  CASE WHEN a.ID_remitente = ? THEN u2.Estado     ELSE u1.Estado     END AS Estado, " +
            "  CASE WHEN a.ID_remitente = ? THEN u2.Ultimo_acceso ELSE u1.Ultimo_acceso END AS Ultimo_acceso " +
            "FROM amistades a " +
            "JOIN Usuarios u1 ON a.ID_remitente    = u1.id_usuario " +
            "JOIN Usuarios u2 ON a.ID_destinatario = u2.id_usuario " +
            "WHERE (a.ID_remitente = ? OR a.ID_destinatario = ?) " +
            "  AND a.estado = 'APPROVED'";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            // 6 parámetros CASE y 2 parámetros WHERE
            for (int i = 1; i <= 6; i++) stmt.setInt(i, idUsuario);
            stmt.setInt(7, idUsuario);
            stmt.setInt(8, idUsuario);
 
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                friends.add(new User(
                    rs.getInt("id_usuario"),
                    rs.getString("Nombre"),
                    rs.getString("Email"),
                    rs.getString("Contraseña"),
                    rs.getString("Estado"),
                    rs.getString("Ultimo_acceso")
                ));
            }
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al obtener amigos: " + e.getMessage());
        }
        return friends;
    }
    
    /*
        Devuelve las solicitudes de amistad PENDIENTES recibidas por un usuario
        Solo las devuelve cuando el usuario es el desttinatario
        Devuelve objetos FriendRequest con el User remitente para que se muestre en la UI X persona te envio una solicitud de amistad
    */
    public List<FriendRequest> getPendingRequests(int idDestinatario) {
        List<FriendRequest> requests = new ArrayList<>();
 
        String sql =
            "SELECT a.ID_amistad, " +
            "       u.id_usuario, u.Nombre, u.Email, u.Contraseña, u.Estado, u.Ultimo_acceso " +
            "FROM amistades a " +
            "JOIN Usuarios u ON a.ID_remitente = u.id_usuario " +
            "WHERE a.ID_destinatario = ? AND a.estado = 'PENDING'";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idDestinatario);
            ResultSet rs = stmt.executeQuery();
 
            while (rs.next()) {
                User sender = new User(
                    rs.getInt("id_usuario"),
                    rs.getString("Nombre"),
                    rs.getString("Email"),
                    rs.getString("Contraseña"),
                    rs.getString("Estado"),
                    rs.getString("Ultimo_acceso")
                );
                FriendRequest fr = new FriendRequest(
                    rs.getInt("ID_amistad"),
                    sender,
                    null,           // targetUser no es necesario mostrarlo en la UI
                    RequestStatus.PENDING
                );
                requests.add(fr);
            }
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al obtener solicitudes pendientes: " + e.getMessage());
        }
        return requests;
    }
 
    /*
         Verifica si dos usuarios son amigos APPROVED
         Bidireccional, comprueba ambas direcciones del par
         Sirve criticamente para que la use ConversationDAO y decida si crear una conversacion TEMP o FRIEND al iniciar el chat
    */
    public boolean areFriends(int idUser1, int idUser2) {
        String sql =
            "SELECT COUNT(*) FROM amistades " +
            "WHERE ((ID_remitente = ? AND ID_destinatario = ?) " +
            "    OR (ID_remitente = ? AND ID_destinatario = ?)) " +
            "  AND estado = 'APPROVED'";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idUser1);
            stmt.setInt(2, idUser2);
            stmt.setInt(3, idUser2);
            stmt.setInt(4, idUser1);
            ResultSet rs = stmt.executeQuery();
 
            if (rs.next()) return rs.getInt(1) > 0;
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al verificar amistad: " + e.getMessage());
        }
        return false;
    }
 
    /*
        Verifica si ya existe una solicitud entre dos usuarios en cualquier estado, 
        Combrueba ambas direcciones para evitar que un usuario A le envie una solicitud a usuario B cuando B ya le envio una a usuario A
        Este es el metodo auxiliar que usa sendFriendRequest()
    */
    public boolean requestAlreadyExists(int idRemitente, int idDestinatario) {
        String sql =
            "SELECT COUNT(*) FROM amistades " +
            "WHERE (ID_remitente = ? AND ID_destinatario = ?) " +
            "   OR (ID_remitente = ? AND ID_destinatario = ?)";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idRemitente);
            stmt.setInt(2, idDestinatario);
            stmt.setInt(3, idDestinatario);
            stmt.setInt(4, idRemitente);
            ResultSet rs = stmt.executeQuery();
 
            if (rs.next()) return rs.getInt(1) > 0;
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al verificar solicitud existente: " + e.getMessage());
        }
        return false;
    }
 
    // Actualizar
    /*
        Cambia el estado de una solicitud a APPROVED
        Despues de que se llama este metodo, el servidor llama ConversationDAO.promoteToFriend si habia un chat TEMP abierto 
        Se notifica a ambos cliente del nuevo estado via JSON
    */
    
    public void acceptRequest(int idAmistad) {
        updateStatus(idAmistad, "APPROVED");
        System.out.println("[FriendshipDAO] Solicitud " + idAmistad + " aceptada.");
    }
 
    /*
        Cambia el estado de una solicitud a DENIED
        Se conserva el estado en la BD para evitar que se envie otra solicitud inmediatamente despues de ser denegada
    */
    public void denyRequest(int idAmistad) {
        updateStatus(idAmistad, "DENIED");
        System.out.println("[FriendshipDAO] Solicitud " + idAmistad + " denegada.");
    }
 
    /*
        Este metodo es privado para ser reutilizable al cambiar el estado de una amistad
        Solo centraliza el UPDATE para que acceptRequest y denyRequest no reptan codigo
    */
    private void updateStatus(int idAmistad, String nuevoEstado) {
        String sql = "UPDATE amistades SET estado = ? WHERE ID_amistad = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idAmistad);
            stmt.executeUpdate();
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al actualizar estado: " + e.getMessage());
        }
    }
 
    // Eliminar
    /*
        Elimina una amistad de la tabla
        Sirve para cuando un usuario decide eliminar un amigo
        Se conserva como referencia historica y solo deja de mostrarla en el panel de amigos
    */
    public void deleteFriendship(int idAmistad) {
        String sql = "DELETE FROM amistades WHERE ID_amistad = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idAmistad);
            int rows = stmt.executeUpdate();
            System.out.println("[FriendshipDAO] Amistad " + idAmistad
                    + " eliminada (" + rows + " fila/s).");
 
        } catch (SQLException e) {
            System.out.println("[FriendshipDAO] Error al eliminar amistad: " + e.getMessage());
        }
    }
}