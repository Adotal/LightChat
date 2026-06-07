package dao;

/**
 *
 * @author GLENNCAMILOGOITIASAN
 */
 
import database.DatabaseConnection;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Miembros_Conversacion la tabla pivote entre usuarios y conversaciones 
public class ConversationMemberDAO {
 
    // Crear
    /* Añade un usuario como miembro de una conversacion y se llama al crear un chat directo TEMP o FRIEND y agrega los dos usuarios,
    y al unirse a un grupo y agrega solo ese usuario al grupo, antes de registrar un usuario verifica con isMember para no hacer un duplicado*/
    public void addMember(int idUsuario, int idConversacion) {
        // Verificar primero que no sea miembro ya
        if (isMember(idUsuario, idConversacion)) {
            System.out.println("[ConversationMemberDAO] Usuario " + idUsuario
                    + " ya es miembro de conversación " + idConversacion + ". Operación ignorada.");
            return;
        }
 
        String sql = "INSERT INTO Miembros_Conversacion (id_usuario, id_conversacion) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idConversacion);
            stmt.executeUpdate();
            System.out.println("[ConversationMemberDAO] Usuario " + idUsuario
                    + " agregado a conversación " + idConversacion);
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al agregar miembro: " + e.getMessage());
        }
    }
 
    // Lectura
 
    /* Devuelve la lista de usuarios que pertenecen a una conversacion, sirve para cargar los participantes de un chat 
        Para que se muestre en UI y para saber a quien reenviar los mensajes en el server
    */
    public List<User> getMembersByConversation(int idConversacion) {
        List<User> members = new ArrayList<>();
        String sql =
            "SELECT u.id_usuario, u.Nombre, u.Email, u.Contraseña, u.Estado, u.Ultimo_acceso " +
            "FROM Miembros_Conversacion mc " +
            "JOIN Usuarios u ON mc.id_usuario = u.id_usuario " +
            "WHERE mc.id_conversacion = ?";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idConversacion);
            ResultSet rs = stmt.executeQuery();
 
            while (rs.next()) {
                User u = new User(
                    rs.getInt("id_usuario"),
                    rs.getString("Nombre"),
                    rs.getString("Email"),
                    rs.getString("Contraseña"),
                    rs.getString("Estado"),
                    rs.getString("Ultimo_acceso")
                );
                members.add(u);
            }
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al obtener miembros: " + e.getMessage());
        }
        return members;
    }
 
    /*
        Devuelve los ids de todas las conversaciones en las que participa un usuario en especifico, 
    se usa al iniciar sesion para mostrar las conversaciones activas en la lista de chats directos y grupos
    */
    public List<Integer> getConversationsByUser(int idUsuario) {
        List<Integer> conversationIds = new ArrayList<>();
        String sql = "SELECT id_conversacion FROM Miembros_Conversacion WHERE id_usuario = ?";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
 
            while (rs.next()) {
                conversationIds.add(rs.getInt("id_conversacion"));
            }
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al obtener conversaciones del usuario: " + e.getMessage());
        }
        return conversationIds;
    }
    
    /*
        Verifica si un usuario ya es miembro de una conversacion, 
    este metodo es auxiliar del addMember()  solo para evitar miembros duplicados.
    */
    public boolean isMember(int idUsuario, int idConversacion) {
        String sql = "SELECT COUNT(*) FROM Miembros_Conversacion " +
                     "WHERE id_usuario = ? AND id_conversacion = ?";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idConversacion);
            ResultSet rs = stmt.executeQuery();
 
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al verificar membresía: " + e.getMessage());
        }
        return false;
    }
 
    /**
     * Cuenta cuántos miembros tiene una conversación.
     *
     * Útil para grupos: antes de borrar un grupo se puede verificar aquí
     * cuántos participantes activos quedan en su conversación.
     *
     * @param idConversacion id de la conversación
     * @return número de miembros
     */
    /*
        Cuenta la cantidad de miembros de una conversacion para verificar antes de borrar un grupo cuantos participantes activos quedan en la conversacion
    */
    public int getMemberCount(int idConversacion) {
        String sql = "SELECT COUNT(*) FROM Miembros_Conversacion WHERE id_conversacion = ?";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idConversacion);
            ResultSet rs = stmt.executeQuery();
 
            if (rs.next()) {
                return rs.getInt(1);
            }
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al contar miembros: " + e.getMessage());
        }
        return 0;
    }
 
    // Eliminar
 
    /* Elimina un miembro de una conversacion en especifico
    Esta funciona para si un usuario se sale de un grupo no para una directa porque ahi se borra toda la conversacion 
    */
    public void removeMember(int idUsuario, int idConversacion) {
        String sql = "DELETE FROM Miembros_Conversacion WHERE id_usuario = ? AND id_conversacion = ?";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idConversacion);
            int rows = stmt.executeUpdate();
            System.out.println("[ConversationMemberDAO] Usuario " + idUsuario
                    + " eliminado de conversación " + idConversacion + " (" + rows + " fila/s).");
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al eliminar miembro: " + e.getMessage());
        }
    }

    /* Elimina todos los miembros de una conversacion previo a borrar la conversacion temporal TEMP, 
    este metodo es suponiendo que no tengamos ON DELETE CASCADE en la bd que es lo mas probable, no he checado*/
    public void removeAllMembers(int idConversacion) {
        String sql = "DELETE FROM Miembros_Conversacion WHERE id_conversacion = ?";
 
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
 
            stmt.setInt(1, idConversacion);
            int rows = stmt.executeUpdate();
            System.out.println("[ConversationMemberDAO] " + rows
                    + " miembro(s) eliminados de conversación " + idConversacion);
 
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al eliminar todos los miembros: " + e.getMessage());
        }
    }
}