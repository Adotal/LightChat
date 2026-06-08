package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Group;

/**
 * DAO para la tabla chat_groups.
 *
 * Estructura (ver schema.sql): id_group (PK), title, id_owner (FK users),
 * created_at. La membresía "aceptada" se modela con group_invitations en
 * estado APPROVED; el creador (id_owner) siempre cuenta como miembro.
 *
 * @author adotal
 */
public class GroupDAO extends DatabaseConnection {

    public GroupDAO() {
        super();
    }

    /** Crea un grupo y devuelve su id único (RQNF52/54). El owner es admin (RQNF53). */
    public int createGroup(String title, int idOwner) {
        String sql = "INSERT INTO chat_groups (title, id_owner) VALUES (?, ?)";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setInt(2, idOwner);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[GroupDAO] Error al crear grupo: " + e.getMessage());
        }
        return -1;
    }

    /** Devuelve un grupo (id, title) o null. */
    public Group getGroup(int idGroup) {
        String sql = "SELECT id_group, title FROM chat_groups WHERE id_group = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Group(rs.getInt("id_group"), rs.getString("title"));
            }
        } catch (SQLException e) {
            System.out.println("[GroupDAO] Error al obtener grupo: " + e.getMessage());
        }
        return null;
    }

    /** Devuelve el id del creador/admin del grupo, o -1. */
    public int getOwnerId(int idGroup) {
        String sql = "SELECT id_owner FROM chat_groups WHERE id_group = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_owner");
            }
        } catch (SQLException e) {
            System.out.println("[GroupDAO] Error al obtener owner: " + e.getMessage());
        }
        return -1;
    }

    /** Verifica si un usuario es el creador del grupo (RQNF73). */
    public boolean isOwner(int idGroup, int idUser) {
        return getOwnerId(idGroup) == idUser;
    }

    /**
     * Grupos de los que un usuario forma parte: los que creó o donde tiene una
     * invitación APPROVED (RQF33).
     */
    public List<Group> getGroupsByUser(int idUser) {
        List<Group> groups = new ArrayList<>();
        String sql
                = "SELECT id_group, title FROM chat_groups WHERE id_owner = ? "
                + "UNION "
                + "SELECT g.id_group, g.title FROM chat_groups g "
                + "JOIN group_invitations gi ON g.id_group = gi.id_group "
                + "WHERE gi.id_invited = ? AND gi.status = 'APPROVED'";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ps.setInt(2, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                groups.add(new Group(rs.getInt("id_group"), rs.getString("title")));
            }
        } catch (SQLException e) {
            System.out.println("[GroupDAO] Error al obtener grupos del usuario: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Cuenta los miembros aceptados del grupo: invitaciones APPROVED + el
     * creador. Base de la regla de permanencia (≥3, RQNF71).
     */
    public int countApprovedMembers(int idGroup) {
        String sql = "SELECT COUNT(*) FROM group_invitations WHERE id_group = ? AND status = 'APPROVED'";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1; // +1 por el creador
            }
        } catch (SQLException e) {
            System.out.println("[GroupDAO] Error al contar miembros: " + e.getMessage());
        }
        return 0;
    }

    /** Devuelve true si el grupo existe. */
    public boolean exists(int idGroup) {
        return getOwnerId(idGroup) != -1;
    }

    /**
     * Elimina el grupo. Gracias a ON DELETE CASCADE del esquema, esto borra en
     * cascada sus invitaciones, su conversación, miembros y mensajes
     * (RQNF74/75/78).
     */
    public void deleteGroup(int idGroup) {
        String sql = "DELETE FROM chat_groups WHERE id_group = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            int rows = ps.executeUpdate();
            System.out.println("[GroupDAO] Grupo " + idGroup + " eliminado (" + rows + " fila/s).");
        } catch (SQLException e) {
            System.out.println("[GroupDAO] Error al eliminar grupo: " + e.getMessage());
        }
    }
}
