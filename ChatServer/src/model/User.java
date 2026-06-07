package model;

/**
 *
 * @author adotal 
 * @author Kosey
 */

public class User {
    private int idUser;
    private String name;
    private String email;
    private String password;
    private String state;
    private String lastAccess;

    public User() {}

    public User(int idUser, String name, String email, String password, String state, String lastAccess) {
        this.idUser = idUser;
        this.name = name;
        this.email = email;
        this.password = password;
        this.state = state;
        this.lastAccess = lastAccess;
    }
      public User(String name, String email, String password, String state) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.state = state;
    }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLastAccess() { return lastAccess; }
    public void setLastAccess(String lastAccess) { this.lastAccess = lastAccess; }
     
        @Override
    public String toString() {        
        return "Username: " + name + " Email: " + email + " satate: " + state;
    }
}
