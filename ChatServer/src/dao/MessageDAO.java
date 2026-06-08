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
public class MessageDAO extends DatabaseConnection {

    public MessageDAO() {
        super();
    }

    // Insert message
    public void insertMessage(Message message) {
        String sql = "INSERT INTO messages (id_conversation, id_sender, content) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, message.getIdConversation());
            ps.setInt(2, message.getIdSender());
            ps.setString(3, message.getContent());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check messages in a conversation
    public List<Message> getMessagesByConversation(int idConversation) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE id_conversation = ? ORDER BY sent_date ASC, id_message ASC";
        try {

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, idConversation);
            ResultSet rs = ps.executeQuery();
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
