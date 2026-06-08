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

// conversation_members: tabla pivote entre usuarios y conversaciones.
public class ConversationMemberDAO extends DatabaseConnection {

    public ConversationMemberDAO() {
        super();
    }

    // Crear -------------------------------------------------------------
    // Añade un usuario a una conversación (evita duplicados con isMember).
    public void addMember(int idUser, int idConversation) {
        if (isMember(idUser, idConversation)) {
            System.out.println("[ConversationMemberDAO] Usuario " + idUser
                    + " ya es miembro de conversación " + idConversation + ". Operación ignorada.");
            return;
        }

        String sql = "INSERT INTO conversation_members (id_user, id_conversation) VALUES (?, ?)";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.setInt(2, idConversation);
            ps.executeUpdate();
            System.out.println("[ConversationMemberDAO] Usuario " + idUser
                    + " agregado a conversación " + idConversation);
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al agregar miembro: " + e.getMessage());
        }
    }

    // Lectura -----------------------------------------------------------
    // Devuelve los usuarios miembros de una conversación.
    public List<User> getMembersByConversation(int idConversation) {
        List<User> members = new ArrayList<>();
        String sql
                = "SELECT u.id_user, u.name, u.email, u.is_connected, u.last_access "
                + "FROM conversation_members mc "
                + "JOIN users u ON mc.id_user = u.id_user "
                + "WHERE mc.id_conversation = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                members.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                ));
            }
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al obtener miembros: " + e.getMessage());
        }
        return members;
    }

    // Devuelve los ids de conversaciones en las que participa un usuario.
    public List<Integer> getConversationsByUser(int idUser) {
        List<Integer> conversationIds = new ArrayList<>();
        String sql = "SELECT id_conversation FROM conversation_members WHERE id_user = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversationIds.add(rs.getInt("id_conversation"));
            }
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al obtener conversaciones del usuario: " + e.getMessage());
        }
        return conversationIds;
    }

    // Verifica si un usuario ya es miembro de una conversación.
    public boolean isMember(int idUser, int idConversation) {
        String sql = "SELECT COUNT(*) FROM conversation_members "
                + "WHERE id_user = ? AND id_conversation = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.setInt(2, idConversation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al verificar membresía: " + e.getMessage());
        }
        return false;
    }

    // Cuenta los miembros de una conversación.
    public int getMemberCount(int idConversation) {
        String sql = "SELECT COUNT(*) FROM conversation_members WHERE id_conversation = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al contar miembros: " + e.getMessage());
        }
        return 0;
    }

    // Eliminar ----------------------------------------------------------
    // Elimina un miembro de una conversación (p. ej. al abandonar un grupo).
    public void removeMember(int idUser, int idConversation) {
        String sql = "DELETE FROM conversation_members WHERE id_user = ? AND id_conversation = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.setInt(2, idConversation);
            int rows = ps.executeUpdate();
            System.out.println("[ConversationMemberDAO] Usuario " + idUser
                    + " eliminado de conversación " + idConversation + " (" + rows + " fila/s).");
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al eliminar miembro: " + e.getMessage());
        }
    }

    // Elimina todos los miembros de una conversación.
    public void removeAllMembers(int idConversation) {
        String sql = "DELETE FROM conversation_members WHERE id_conversation = ?";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            int rows = ps.executeUpdate();
            System.out.println("[ConversationMemberDAO] " + rows
                    + " miembro(s) eliminados de conversación " + idConversation);
        } catch (SQLException e) {
            System.out.println("[ConversationMemberDAO] Error al eliminar todos los miembros: " + e.getMessage());
        }
    }
}
