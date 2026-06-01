/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Kosey
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://lightchat-mysql-database-lightchat.d.aivencloud.com:13866/defaultdb";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_0YPH70tRkp43yTsPfEX";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("[ERROR] Connection failed: " + e.getMessage());
            return null;
        }
    }
}
