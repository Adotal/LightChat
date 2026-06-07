package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 *
 * @author Kosey
 */
public class UserDAO extends DatabaseConnection {

    public UserDAO() {
        super();
    }

    // Insert users
    public void insertUser(User user) {

        String sql = "INSERT INTO users (name, email, password, is_connected) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps;
            ps = getCon().prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setBoolean(4, user.getIsConnected());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

    // Get all users
    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        // Fetch all but password
        String sql = "SELECT id_user, name, email, is_connected, last_access FROM users";

        try {

            PreparedStatement ps = getCon().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                users.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                ));

            }
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }
        return users;
    }
    
       // Get all users except one with given email
    public ArrayList<User> getAllUsersNotEmail(String email) {
        ArrayList<User> users = new ArrayList<>();
        // Fetch all but password
        String sql = "SELECT id_user, name, email, is_connected, last_access FROM users WHERE email <> ?";

        try {

            PreparedStatement ps = getCon().prepareStatement(sql);            
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                users.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                ));

            }
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }
        return users;
    }

    // Get user by ID
    public User getUserById(int idUser) {
        String sql = "SELECT * FROM users WHERE id_user = ?";
        User user = null;
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;
        try {

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("is_connected"),
                        rs.getString("last_access")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    
    // Will return false if fails or user doesnt exists
    public boolean changePassword(String email, String newPassword) {

        // Check if user exists
        if (getUserByEmail(email) == null) {
            return false;
        }

        // Attempt to change password
        String sql = "UPDATE users SET password = ? WHERE email = ?";

        try {
            PreparedStatement ps;
            ps = getCon().prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return false;
        }

        return true;
    }
    
      // Will return false if fails or user doesnt exists
    public boolean changeIsConnected(String email, boolean newStatus) {

        // Attempt to change password
        String sql = "UPDATE users SET is_connected = ? WHERE email = ?";

        try {
            PreparedStatement ps;
            ps = getCon().prepareStatement(sql);
            ps.setBoolean(1, newStatus);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return false;
        }

        return true;
    }
}
