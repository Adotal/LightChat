/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
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
}
