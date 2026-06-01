package database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import model.User;

/**
 *
 * @author adotal
 */
public class UserDAO extends DatabaseService {

    public UserDAO() {
        super();
    }

    public void add(User u) {

        try {

            PreparedStatement ps;

            // TDP: TODA VALIDACIÓN VA AQUÍ                        
            ps = getCon().prepareStatement("INSERT INTO user(username, email, password) values(?,?,?)");
            ps.setString(1, u.getUserName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

    public ArrayList<User> getAll() {
        try {

            // Contenedor de resultados
            ArrayList<User> users = new ArrayList<User>();

            PreparedStatement ps;
            ps = getCon().prepareStatement("SELECT id, username, email, is_connected FROM user");

            ResultSet rs;
            rs = ps.executeQuery();

            // Ejecutar rs.next() justo después de la consulta coloca el cursor en la primera fila
            while (rs.next()) {
                users.add(
                        new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getBoolean("is_connected")
                        ));
            }
            return users;
        } catch (SQLException ex) {
            System.getLogger(UserDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }

    }
}
