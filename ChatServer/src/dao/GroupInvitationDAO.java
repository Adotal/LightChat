package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Group;
import model.GroupInvitation;
import model.Request.RequestStatus;
import model.User;

/**
 * DAO para la tabla group_invitations.
 *
 * Estructura (ver schema.sql): id_invitation (PK), id_group (FK chat_groups),
 * id_invited (FK users), status ENUM(PENDING, APPROVED, DENIED).
 *
 * @author adotal
 */
public class GroupInvitationDAO extends DatabaseConnection {

    public GroupInvitationDAO() {
        super();
    }

    /** Crea una invitación PENDING (RQNF56). Evita duplicados. Devuelve su id o -1. */
    public int invite(int idGroup, int idInvited) {
        if (invitationExists(idGroup, idInvited)) {
            System.out.println("[GroupInvitationDAO] Ya existe invitación de grupo "
                    + idGroup + " para usuario " + idInvited);
            return -1;
        }
        String sql = "INSERT INTO group_invitations (id_group, id_invited, status) VALUES (?, ?, 'PENDING')";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idGroup);
            ps.setInt(2, idInvited);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al invitar: " + e.getMessage());
        }
        return -1;
    }

    public boolean invitationExists(int idGroup, int idInvited) {
        String sql = "SELECT COUNT(*) FROM group_invitations WHERE id_group = ? AND id_invited = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ps.setInt(2, idInvited);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al verificar invitación: " + e.getMessage());
        }
        return false;
    }

    /**
     * Invitaciones PENDIENTES recibidas por un usuario, con datos del grupo y
     * del creador para mostrar "X te invitó a Y" (RQF30, RQNF58/60).
     */
    public List<GroupInvitation> getPendingInvitationsByUser(int idInvited) {
        List<GroupInvitation> invitations = new ArrayList<>();
        String sql
                = "SELECT gi.id_invitation, gi.status, "
                + "       g.id_group, g.title, "
                + "       o.id_user AS owner_id, o.name AS owner_name, o.email AS owner_email "
                + "FROM group_invitations gi "
                + "JOIN chat_groups g ON gi.id_group = g.id_group "
                + "JOIN users o ON g.id_owner = o.id_user "
                + "WHERE gi.id_invited = ? AND gi.status = 'PENDING'";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idInvited);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Group g = new Group(rs.getInt("id_group"), rs.getString("title"));
                User owner = new User(rs.getInt("owner_id"), rs.getString("owner_name"),
                        rs.getString("owner_email"), false, null);
                invitations.add(new GroupInvitation(
                        rs.getInt("id_invitation"), g, owner, null,
                        RequestStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al obtener invitaciones del usuario: " + e.getMessage());
        }
        return invitations;
    }

    /**
     * Todas las invitaciones de un grupo con el usuario invitado y su estado,
     * para que el grupo muestre a quién se invitó y en qué estado (RQF31/RQNF63).
     */
    public List<GroupInvitation> getInvitationsByGroup(int idGroup) {
        List<GroupInvitation> invitations = new ArrayList<>();
        String sql
                = "SELECT gi.id_invitation, gi.status, "
                + "       u.id_user, u.name, u.email, u.is_connected, u.last_access "
                + "FROM group_invitations gi "
                + "JOIN users u ON gi.id_invited = u.id_user "
                + "WHERE gi.id_group = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User invited = new User(rs.getInt("id_user"), rs.getString("name"),
                        rs.getString("email"), rs.getBoolean("is_connected"), rs.getString("last_access"));
                invitations.add(new GroupInvitation(
                        rs.getInt("id_invitation"), null, null, invited,
                        RequestStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al obtener invitaciones del grupo: " + e.getMessage());
        }
        return invitations;
    }

    /** Devuelve {idGroup, idInvited} de una invitación, o null. */
    public int[] getInvitationTarget(int idInvitation) {
        String sql = "SELECT id_group, id_invited FROM group_invitations WHERE id_invitation = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idInvitation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new int[]{rs.getInt("id_group"), rs.getInt("id_invited")};
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al obtener objetivo de invitación: " + e.getMessage());
        }
        return null;
    }

    public void accept(int idInvitation) {
        updateStatus(idInvitation, "APPROVED");
    }

    public void deny(int idInvitation) {
        updateStatus(idInvitation, "DENIED");
    }

    private void updateStatus(int idInvitation, String status) {
        String sql = "UPDATE group_invitations SET status = ? WHERE id_invitation = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, idInvitation);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al actualizar estado: " + e.getMessage());
        }
    }

    /** Cuenta invitaciones PENDING del grupo (para la regla de permanencia). */
    public int countPending(int idGroup) {
        String sql = "SELECT COUNT(*) FROM group_invitations WHERE id_group = ? AND status = 'PENDING'";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al contar pendientes: " + e.getMessage());
        }
        return 0;
    }

    /** Ids de usuarios con invitación (cualquier estado) a un grupo: para notificar eliminación. */
    public List<Integer> getInvitedUserIds(int idGroup) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_invited FROM group_invitations WHERE id_group = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id_invited"));
            }
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al obtener invitados: " + e.getMessage());
        }
        return ids;
    }

    /** Elimina la invitación de un usuario a un grupo (al abandonar). */
    public void removeInvitation(int idGroup, int idInvited) {
        String sql = "DELETE FROM group_invitations WHERE id_group = ? AND id_invited = ?";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idGroup);
            ps.setInt(2, idInvited);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[GroupInvitationDAO] Error al eliminar invitación: " + e.getMessage());
        }
    }
}
