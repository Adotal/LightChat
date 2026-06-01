/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Kosey
 */
public class Message {
    private int idMessage;
    private int idConversation;
    private int idSender;
    private String content;
    private String sentDate;

    public Message() {}

    public Message(int idMessage, int idConversation, int idSender, String content, String sentDate) {
        this.idMessage = idMessage;
        this.idConversation = idConversation;
        this.idSender = idSender;
        this.content = content;
        this.sentDate = sentDate;
    }

    public int getIdMessage() { return idMessage; }
    public void setIdMessage(int idMessage) { this.idMessage = idMessage; }

    public int getIdConversation() { return idConversation; }
    public void setIdConversation(int idConversation) { this.idConversation = idConversation; }

    public int getIdSender() { return idSender; }
    public void setIdSender(int idSender) { this.idSender = idSender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSentDate() { return sentDate; }
    public void setSentDate(String sentDate) { this.sentDate = sentDate; }
}
