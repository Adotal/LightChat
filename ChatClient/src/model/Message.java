package model;

import java.time.LocalDateTime;

/**
 *
 * @author adotal
 */
public class Message {

    private User userSender;
    private User userReceiver;
    private String text;
    private LocalDateTime sendedAt;

    public Message() {

    }

    public Message(User userSender, User userReceiver, String text, LocalDateTime sendedAt) {
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

    public LocalDateTime getSendedAt() {
        return sendedAt;
    }

    public void setSendedAt(LocalDateTime sendedAt) {
        this.sendedAt = sendedAt;
    }

}
