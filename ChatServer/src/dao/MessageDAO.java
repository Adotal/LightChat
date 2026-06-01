/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import database.DatabaseConnection;
import model.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kosey
 */
public class MessageDAO {
    
    // Insert message
    public void insertMessage(Message message) {
        String sql = "INSERT INTO messages (id_conversation, id_sender, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, message.getIdConversation());
            stmt.setInt(2, message.getIdSender());
            stmt.setString(3, message.getContent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check messages in a conversation
    public List<Message> getMessagesByConversation(int idConversation) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE id_conversation = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConversation);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message m = new Message(
                    rs.getInt("id_message"),
                    rs.getInt("id_conversation"),
                    rs.getInt("id_sender"),
                    rs.getString("content"),
                    rs.getString("sent_date")
                );
                messages.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
