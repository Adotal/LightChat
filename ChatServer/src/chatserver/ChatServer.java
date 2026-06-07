package chatserver;

import dao.MessageDAO;
import dao.UserDAO;
import java.util.ArrayList;
import model.Message;
import model.User;
import server.JavaServer;

/**
 * @author adotal
 *
 */
public class ChatServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Start server
        JavaServer server;
        server = new JavaServer();
        server.beginServer();

        UserDAO userDAO = new UserDAO();

        /*// Insertar usuario
        User newUser = new User(0, "Adrián", "adrian@example.com", "1234", "connected", null);
        userDAO.insertUser(newUser);*/
        // Consultar usuarios
        
        ArrayList<User> list;
        list = userDAO.getAllUsers();
        for (User u : list) {
            System.out.println(u.getIdUser() + " - " + u.getName() + " - "
                    + u.getEmail() + " - " + u.getIsConnected()+ " - " + u.getLastAccess());
        }

        MessageDAO messageDAO = new MessageDAO();

        // Insertar un mensaje
        //Message newMessage = new Message(0, 1, 2, "soy Adrian", null);
        //messageDAO.insertMessage(newMessage);
        // Consultar mensajes de la conversación 1
        // Conseguir datos temp de un usuario
        for (Message m : messageDAO.getMessagesByConversation(1)) {
            User uTemp = userDAO.getUserById(m.getIdSender());
            System.out.println(uTemp.getName() + ": '" + m.getContent() + "' el " + m.getSentDate());
        }

    }

}
