package chatclient;

import view.LoginView;

/**
 * @author adotal
 * 
 */
public class ChatClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        LoginView loginView = new LoginView();
        loginView.setVisible(true);
        
        System.out.println("Hello World");
    }
    
}
