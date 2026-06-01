/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Kosey
 */
public class DatabaseConnection {

    public static Connection connect() {

        Properties props = new Properties();

        // Native loader for the local .env file
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("[INFO] No local .env file found. Falling back to system environment.");
        }

        // Check local file first, fall back to system environment variables if empty
        String url = props.getProperty("DB_URL", System.getenv("DB_URL"));
        String user = props.getProperty("DB_USER", System.getenv("DB_USER"));
        String password = props.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));

        if (url == null) {
            System.err.println("[ERROR] DB settings missing! Please copy .env.example to .env and fill it out.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("[ERROR] Connection failed: " + e.getMessage());
            return null;
        }
    }
}
