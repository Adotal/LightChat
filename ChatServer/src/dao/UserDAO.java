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

        String sql = "INSERT INTO users (name, email, password, state) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps;
            ps = getCon().prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getState());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

    // Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try {

            PreparedStatement ps = getCon().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                users.add(new User(
                        rs.getInt("id_user"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("state"),
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
                        rs.getString("state"),
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
                        rs.getString("state"),
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
}
