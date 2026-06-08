package dao;

/**
 *
 * @author GLENNCAMILOGOITIASAN
 */
import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO extends DatabaseConnection {

    public ConversationDAO() {
        super();
    }

    // Creacion ----------------------------------------------------------
    // Crea conversación directa TEMP o FRIEND, devuelve el id generado.
    public int createDirectConversation(String type) {
        String sql = "INSERT INTO conversations (id_group, type, last_seen) VALUES (NULL, ?, NOW())";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, type);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al crear conversación directa: " + e.getMessage());
        }
        return -1;
    }

    // Crea conversación de grupo (tipo GROUP) ligada a un id_group.
    public int createGroupConversation(int idGroup) {
        String sql = "INSERT INTO conversations (id_group, type, last_seen) VALUES (?, 'GROUP', NOW())";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idGroup);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al crear conversación de grupo: " + e.getMessage());
        }
        return -1;
    }

    // Lectura -----------------------------------------------------------
    // Busca la conversación directa (no GROUP) entre dos usuarios; -1 si no existe.
    public int getConversationIdByUsers(int idUser1, int idUser2) {
        String sql
                = "SELECT c.id_conversation "
                + "FROM conversations c "
                + "JOIN conversation_members mc ON c.id_conversation = mc.id_conversation "
                + "WHERE mc.id_user IN (?, ?) "
                + "AND c.type != 'GROUP' "
                + "GROUP BY c.id_conversation "
                + "HAVING COUNT(DISTINCT mc.id_user) = 2";

        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser1);
            ps.setInt(2, idUser2);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_conversation");
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al buscar conversación por usuarios: " + e.getMessage());
        }
        return -1;
    }

    // Devuelve el tipo (TEMP, FRIEND, GROUP) de una conversación.
    public String getConversationType(int idConversation) {
        String sql = "SELECT type FROM conversations WHERE id_conversation = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al obtener tipo de conversación: " + e.getMessage());
        }
        return null;
    }

    // Devuelve el id de la conversación de un grupo; -1 si no existe.
    public int getConversationIdByGroup(int idGroup) {
        String sql = "SELECT id_conversation FROM conversations WHERE id_group = ? AND type = 'GROUP'";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_conversation");
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al buscar conversación de grupo: " + e.getMessage());
        }
        return -1;
    }

    /*
        Devuelve los ids de conversaciones directas (TEMP/FRIEND) de las que
        un usuario es miembro, filtradas por tipo. Útil para borrar las TEMP
        del usuario al cerrar sesión (chats efímeros con no-amigos).
     */
    public List<Integer> getDirectConversationsByUserAndType(int idUser, String type) {
        List<Integer> ids = new ArrayList<>();
        String sql
                = "SELECT c.id_conversation "
                + "FROM conversations c "
                + "JOIN conversation_members mc ON c.id_conversation = mc.id_conversation "
                + "WHERE mc.id_user = ? AND c.type = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id_conversation"));
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al obtener conversaciones por tipo: " + e.getMessage());
        }
        return ids;
    }

    // Actualiza last_seen al momento actual (ordena chats por recencia).
    public void updateLastSeen(int idConversation) {
        String sql = "UPDATE conversations SET last_seen = NOW() WHERE id_conversation = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al actualizar last_seen: " + e.getMessage());
        }
    }

    // Convierte una conversación TEMP en FRIEND (al aceptarse la amistad),
    // conservando el historial.
    public void promoteToFriend(int idConversation) {
        String sql = "UPDATE conversations SET type = 'FRIEND' WHERE id_conversation = ? AND type = 'TEMP'";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al promover conversación a FRIEND: " + e.getMessage());
        }
    }

    // Eliminar ----------------------------------------------------------
    // Elimina mensajes, miembros y la conversación de forma atómica.
    public void deleteConversation(int idConversation) {
        String deleteMsgs = "DELETE FROM messages WHERE id_conversation = ?";
        String deleteMembers = "DELETE FROM conversation_members WHERE id_conversation = ?";
        String deleteConv = "DELETE FROM conversations WHERE id_conversation = ?";

        try {
            getCon().setAutoCommit(false);
            try (
                    PreparedStatement s1 = getCon().prepareStatement(deleteMsgs);
                    PreparedStatement s2 = getCon().prepareStatement(deleteMembers);
                    PreparedStatement s3 = getCon().prepareStatement(deleteConv)) {
                s1.setInt(1, idConversation);
                s1.executeUpdate();
                s2.setInt(1, idConversation);
                s2.executeUpdate();
                s3.setInt(1, idConversation);
                s3.executeUpdate();
                getCon().commit();
                System.out.println("[ConversationDAO] Conversación " + idConversation + " eliminada completamente.");
            } catch (SQLException e) {
                getCon().rollback();
                System.out.println("[ConversationDAO] Error al eliminar conversación, rollback aplicado: " + e.getMessage());
            } finally {
                getCon().setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error de conexión: " + e.getMessage());
        }
    }
}
