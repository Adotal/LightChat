/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author adotal
 */
public class User {
    
    private String userName;
    private String email;
    private String status;    
    
    public User(){
        
    }

    public User(String userName, String email, String status) {
        this.userName = userName;
        this.email = email;
        this.status = status;
    }
    

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
    
}
