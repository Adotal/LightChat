package chatserver;

import dao.MessageDAO;
import dao.UserDAO;
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
        UserDAO userDAO = new UserDAO();

        /*// Insertar usuario
        User newUser = new User(0, "Adrián", "adrian@example.com", "1234", "connected", null);
        userDAO.insertUser(newUser);*/
        
        // Consultar usuarios
        for (User u : userDAO.getAllUsers()) {
            System.out.println(u.getIdUser() + " - " + u.getName() + " - " + u.getEmail());
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
        
        JavaServer server;
        server = new JavaServer();
        server.beginServer();
    }

}
