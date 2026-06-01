package model;

/**
 *
 * @author adotal
 */
public class User {

    private int id;
    private String userName;
    private String email;
    private boolean isConnected;
    private String password;

    public User() {
        id = 0;
        userName = "";
        email = "";
        isConnected = false;

    }

    public User(int id, String userName, String email, boolean isConnected) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.isConnected = isConnected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {        
        return "Username: " + userName + " Email: " + email + " IsConnected: " + isConnected;
    }
}
