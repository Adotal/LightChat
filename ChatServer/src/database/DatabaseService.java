package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author adotal
 */

// Aunque debía ser el nombre de la BD, se eligió DatabaseService por representación
public class DatabaseService {

    private Connection con;

    public DatabaseService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/light_chat", "root", "12345");

        } catch (ClassNotFoundException ex) {
            System.getLogger(DatabaseService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (SQLException ex) {
            System.getLogger(DatabaseService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public Connection getCon() {
        return con;
    }
}
