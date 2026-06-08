package controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import model.Chat;
import model.Message;
import model.User;

/**
 * Lógica de un chat directo: posee el modelo {@link Chat} y construye los
 * objetos {@link Message}. Mantiene fuera de la vista la creación de modelos.
 *
 * El servidor aún no soporta el envío de mensajes de chat, por lo que se
 * conserva el "eco" simulado: {@link #buildEcho(Message)} fabrica la respuesta
 * del receptor. La temporización (Swing Timer) queda en la vista, que es una
 * preocupación de UI.
 *
 * @author adotal
 */
public class ChatController {

    private final User currentUser;
    private final User receiverUser;
    private final Chat chat;

    public ChatController(User currentUser, User receiverUser) {
        this.currentUser = currentUser;
        this.receiverUser = receiverUser;
        this.chat = new Chat(101, receiverUser, new ArrayList<>());
    }

    public User getReceiverUser() {
        return receiverUser;
    }

    public Chat getChat() {
        return chat;
    }

    /** Crea el mensaje del usuario actual, lo añade al chat y lo devuelve. */
    public Message sendMessage(String text) {
        Message message = new Message(currentUser, receiverUser, text, LocalDateTime.now());
        chat.getMessages().add(message);
        return message;
    }

    /** Eco simulado: respuesta del receptor mientras no exista soporte real. */
    public Message buildEcho(Message original) {
        Message echo = new Message(receiverUser, currentUser, " " + original.getText(), LocalDateTime.now());
        chat.getMessages().add(echo);
        return echo;
    }
}
