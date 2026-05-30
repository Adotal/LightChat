package model;

import java.util.ArrayList;

/**
 *
 * @author adotal
 */
public class Group {

    private int id;
    private String title;
    private ArrayList<UserGroup> users;

    public Group() {
        title = "";
        users = null;
    }

    // For list of groups and invitations without loading all users list
    public Group(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Group(int id, String title, ArrayList<UserGroup> users) {
        this.id = id;
        this.title = title;
        this.users = users;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<UserGroup> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserGroup> users) {
        this.users = users;
    }

}
