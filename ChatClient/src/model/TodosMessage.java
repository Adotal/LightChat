package model;

import java.time.LocalDateTime;

/**
 *
 * @author adotal
 */
public class TodosMessage {

    private User userSender;
    private User userReceiver;
    private String text;
    private String sendedAt;

    public TodosMessage() {

    }

    public TodosMessage(User userSender, User userReceiver, String text, String sendedAt) {
        this.userSender = userSender;
        this.userReceiver = userReceiver;
        this.text = text;
        this.sendedAt = sendedAt;
    }

    public User getUserSender() {
        return userSender;
    }

    public void setUserSender(User userSender) {
        this.userSender = userSender;
    }

    public User getUserReceiver() {
        return userReceiver;
    }

    public void setUserReceiver(User userReceiver) {
        this.userReceiver = userReceiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSendedAt() {
        return sendedAt;
    }

    public void setSendedAt(String sendedAt) {
        this.sendedAt = sendedAt;
    }

}
