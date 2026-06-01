package model;

import java.util.ArrayList;

/**
 *
 * @author adotal
 */
public class Chat {

    private int id;
    private User receiverUser;
    private ArrayList<Message> messages;

    public Chat() {

    }

    public Chat(int id, User receiverUser, ArrayList<Message> messages) {
        this.id = id;
        this.receiverUser = receiverUser;
        this.messages = messages;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

}
