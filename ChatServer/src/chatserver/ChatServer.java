package chatserver;

import database.UserDAO;
import java.util.ArrayList;
import model.User;

/**
 * @author adotal
 *
 */
public class ChatServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("List of registered users:");
        UserDAO userDAO = new UserDAO();

        ArrayList<User> users = new ArrayList<User>();
        users = userDAO.getAll();

        if (users.isEmpty()) {
            System.out.println("No hay datos para mostrar");
        } else {
            for (User user : users) {
                System.out.println(
                        user.toString());
            }
        }
    }

}
