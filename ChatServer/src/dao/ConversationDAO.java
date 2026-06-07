package dao;

/**
 *
 * @author GLENNCAMILOGOITIASAN
 */
import database.DatabaseConnection;
import java.sql.*;

public class ConversationDAO extends DatabaseConnection {

    public ConversationDAO() {
        super();
    }

    // Creacion
    // Crear conversación directa TEMP temporal o FRIEND amigo e inserta el registro en la tabla conversaciones, y devuelve el isd que se genero de la conversacion
    public int createDirectConversation(String tipo) {
        // id_grupo se deja NULL porque no es conversación de grupo
        String sql = "INSERT INTO Conversaciones (id_grupo, tipo, ultima_vez) VALUES (NULL, ?, NOW())";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, tipo);
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

    // Crear conversacion de un grupo tipo GROUP y vincula el id del grupo para que la conversacion este ligada a ese grupo 
    public int createGroupConversation(int idGrupo) {
        String sql = "INSERT INTO Conversaciones (id_grupo, tipo, ultima_vez) VALUES (?, 'GROUP', NOW())";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, idGrupo);
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

    // Lectura
    // Obtiene la id de conversacion por los uasuarios, busca una conversacion directa entre dos usuarios, y hace un JOIN con Miembros_conversacion agrupando por id_conversacion
    // y filtra los que tienen solo esos participantes esto para saber si ya existe un chat entre usuario A y B  antes de hacer uno nuevo, si no hay regresa -1
    public int getConversationIdByUsers(int idUser1, int idUser2) {
        // Seleccionamos conversaciones donde ambos usuarios son miembros
        // y el tipo NO es GROUP (para no mezclar con grupos)
        String sql
                = "SELECT c.id_conversacion "
                + "FROM Conversaciones c "
                + "JOIN Miembros_Conversacion mc ON c.id_conversacion = mc.id_conversacion "
                + "WHERE mc.id_usuario IN (?, ?) "
                + "AND c.tipo != 'GROUP' "
                + "GROUP BY c.id_conversacion "
                + "HAVING COUNT(DISTINCT mc.id_usuario) = 2";

        try {

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setInt(1, idUser1);
            ps.setInt(2, idUser2);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_conversacion");
            }

        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al buscar conversación por usuarios: " + e.getMessage());
        }
        return -1;
    }

    // Obtiene el tipo de conversacion por id, retorna TEMP, FRIEND o GROUP
    public String getConversationType(int idConversacion) {
        String sql = "SELECT tipo FROM Conversaciones WHERE id_conversacion = ?";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setInt(1, idConversacion);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("tipo");
            }

        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al obtener tipo de conversación: " + e.getMessage());
        }
        return null;
    }

    // Obtiene el id de la conversación asociada a un grupo si la conversacioon es de tipo grupo
    public int getConversationIdByGroup(int idGrupo) {
        String sql = "SELECT id_conversacion FROM Conversaciones WHERE id_grupo = ? AND tipo = 'GROUP'";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setInt(1, idGrupo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_conversacion");
            }

        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al buscar conversación de grupo: " + e.getMessage());
        }
        return -1;
    }

    // Actualiza el momento de ultima vez al momento actual y se llama cada que se manda un nuevo mensaje en la concversacion y asi se ordenan los chats de recientes a antiguos
    public void updateLastSeen(int idConversacion) {
        String sql = "UPDATE Conversaciones SET ultima_vez = NOW() WHERE id_conversacion = ?";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setInt(1, idConversacion);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al actualizar ultima_vez: " + e.getMessage());
        }
    }

    // Convertir una conversacion temporal TEMP en conversacion de tipo amigos FRIEND cuando dos usuarios se hacen amigos, solo hace UPDATE para no perder el historial de la conversacion al aceptar una solicitud
    public void promoteToFriend(int idConversacion) {
        String sql = "UPDATE Conversaciones SET tipo = 'FRIEND' WHERE id_conversacion = ? AND tipo = 'TEMP'";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setInt(1, idConversacion);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error al promover conversación a FRIEND: " + e.getMessage());
        }
    }

    /* Eliminar
        Elimina toda la conversacion de forma ordenada
     */
    public void deleteConversation(int idConversacion) {
        // Paso 1: borrar todos los mensajes de esa conversación
        String deleteMsgs = "DELETE FROM Mensaje WHERE id_conversacion = ?";
        // Paso 2: borrar todos los miembros de esa conversación  
        String deleteMembers = "DELETE FROM Miembros_Conversacion WHERE id_conversacion = ?";
        // Paso 3: recién ahora borrar la conversación
        String deleteConv = "DELETE FROM Conversaciones WHERE id_conversacion = ?";

        try {
            getCon().setAutoCommit(false); // Todo o nada — si algo falla, se revierte todo

            try (
                    PreparedStatement s1 = getCon().prepareStatement(deleteMsgs); PreparedStatement s2 = getCon().prepareStatement(deleteMembers); PreparedStatement s3 = getCon().prepareStatement(deleteConv)) {
                s1.setInt(1, idConversacion);
                s1.executeUpdate();

                s2.setInt(1, idConversacion);
                s2.executeUpdate();

                s3.setInt(1, idConversacion);
                s3.executeUpdate();

                getCon().commit(); // Confirmar los tres pasos juntos
                System.out.println("[ConversationDAO] Conversación " + idConversacion + " eliminada completamente.");

            } catch (SQLException e) {
                getCon().rollback(); // Si algo falla, deshacer todo
                System.out.println("[ConversationDAO] Error al eliminar conversación, rollback aplicado: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("[ConversationDAO] Error de conexión: " + e.getMessage());
        }
    }
}
