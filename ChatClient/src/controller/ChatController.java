package controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.function.Consumer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import model.Chat;
import model.Message;
import model.SessionManager;
import model.TodosMessage;
import model.User;
import model.dbrequest.LoginRequest;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;
import static util.Json.mapper;

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

    public interface View {

        void onMessageReceived(String message);

        void onDeleteChat();

        /** Carga el historial persistido de la conversación, en orden cronológico. */
        void onHistoryLoaded(java.util.List<Message> messages);
    }

    private final View view;
    private final User currentUser;
    private final User receiverUser;
    private final Chat chat;

    // Referencias a los handlers registrados, para poder desregistrarlos al cerrar.
    private Consumer<JsonNode> sendMessageHandler;
    private Consumer<JsonNode> deleteChatHandler;
    private Consumer<JsonNode> historyHandler;

    public ChatController(View view, User receiverUser) {
        this.view = view;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.receiverUser = receiverUser;
        this.chat = new Chat(101, receiverUser, new ArrayList<>());
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();

        sendMessageHandler = rootNode -> {
            try {
                // Ignorar payloads de grupo: este handler solo procesa chat directo.
                if (rootNode.has("chat_type") && !"TODOS".equals(rootNode.get("chat_type").asText())) {
                    return;
                }
                //  Reutilizar el ObjectMapper para deserializar el nodo 'message'
                ObjectMapper mapper = new ObjectMapper();
                TodosMessage incomingMsg = mapper.treeToValue(rootNode.get("message"), TodosMessage.class);

                // Validar que el mensaje recibido sea para esta conversación activa
                // Evita que los mensajes de otros usuarios aparezcan en esta ventana de chat
                int currentOpenChatUserId = receiverUser.getIdUser();
                int senderId = incomingMsg.getUserSender().getIdUser();

                if (senderId == currentOpenChatUserId) {
                    // El mensaje proviene de la persona con la que estoy chateando actualmente

                    // Convertimos el TodosMessage a tu modelo visual 'Message' si es necesario
                    Message uiMessage = new Message(incomingMsg.getUserSender(), incomingMsg.getUserReceiver(), incomingMsg.getText(), incomingMsg.getSendedAt());
                    chat.getMessages().add(uiMessage);

                    //  Pasar el texto (u objeto) a la vista usando la interfaz
                    view.onMessageReceived(uiMessage.getText());
                } else {
                    System.out.println("Mensaje recibido de " + incomingMsg.getUserSender().getName() + " pero tienes abierto el chat de " + receiverUser.getName());
                    // Opcional: Aquí podrías disparar una notificación global de sistema
                }

            } catch (Exception e) {
                System.err.println("Error procesando mensaje P2P entrante: " + e.getMessage());
            }
        };
        dispatcher.register("SEND_MESSAGE", sendMessageHandler);

        deleteChatHandler = root -> {
            // Solo cerrar si el DELETE_CHAT corresponde a esta conversación.
            if (root.has("otherUserId")
                    && root.get("otherUserId").asInt() != receiverUser.getIdUser()) {
                return;
            }
            view.onDeleteChat();
        };
        dispatcher.register("DELETE_CHAT", deleteChatHandler);

        // Historial persistido de la conversación (RQF26/RQNF48/49)
        historyHandler = root -> {
            try {
                if (!root.has("otherUserId")
                        || root.get("otherUserId").asInt() != receiverUser.getIdUser()) {
                    return; // historial de otra conversación
                }
                java.util.List<Message> history = new ArrayList<>();
                if (root.has("messages")) {
                    for (com.fasterxml.jackson.databind.JsonNode mn : root.get("messages")) {
                        int idSender = mn.get("idSender").asInt();
                        String content = mn.get("content").asText();
                        String sentDate = mn.has("sentDate") ? mn.get("sentDate").asText() : "";
                        boolean mine = idSender == currentUser.getIdUser();
                        User sender = mine ? currentUser : receiverUser;
                        User receiver = mine ? receiverUser : currentUser;
                        Message m = new Message(sender, receiver, content, sentDate);
                        chat.getMessages().add(m);
                        history.add(m);
                    }
                }
                view.onHistoryLoaded(history);
            } catch (Exception e) {
                System.err.println("[ChatController] Error procesando historial: " + e.getMessage());
            }
        };
        dispatcher.register("CONVERSATION_HISTORY", historyHandler);
    }

    /** Desregistra los handlers de red; llamar al cerrar la vista. */
    public void dispose() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();
        if (sendMessageHandler != null) {
            dispatcher.unregister("SEND_MESSAGE", sendMessageHandler);
        }
        if (deleteChatHandler != null) {
            dispatcher.unregister("DELETE_CHAT", deleteChatHandler);
        }
        if (historyHandler != null) {
            dispatcher.unregister("CONVERSATION_HISTORY", historyHandler);
        }
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    /** Pide al servidor el historial persistido con este usuario (si son amigos). */
    public void requestHistory() {
        try {
            ObjectNode req = Json.mapper().createObjectNode();
            req.put("type", "FETCH_CONVERSATION_HISTORY");
            req.put("userId", currentUser.getIdUser());
            req.put("otherUserId", receiverUser.getIdUser());
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(req));
        } catch (Exception ex) {
            System.err.println("[ChatController] Error al pedir historial: " + ex.getMessage());
        }
    }

    /** True si el mensaje fue enviado por el usuario actual. */
    public boolean isMine(Message m) {
        return m.getUserSender() != null
                && m.getUserSender().getIdUser() == currentUser.getIdUser();
    }

    public User getReceiverUser() {
        return receiverUser;
    }

    public Chat getChat() {
        return chat;
    }

    /**
     * Crea el mensaje del usuario actual, lo añade al chat y lo devuelve.
     */
    public Message sendMessage(String text) {
        Message message = new Message(currentUser, receiverUser, text, LocalDateTime.now().toString());
        chat.getMessages().add(message);

        try {

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode responseNode = mapper.createObjectNode();
            responseNode.put("type", "SEND_MESSAGE");
            responseNode.put("chat_type", "TODOS");
            responseNode.set("message", mapper.valueToTree(message));
            // Convert the entire object node to a string and send it
            String json = mapper.writeValueAsString(responseNode);
            ClientSocket.getInstance().sendText(json);
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return message;
    }

    /**
     * Eco simulado: respuesta del receptor mientras no exista soporte real.
     */
    public Message buildEcho(Message original) {
        Message echo = new Message(receiverUser, currentUser, " " + original.getText(), LocalDateTime.now().toString());
        chat.getMessages().add(echo);
        return echo;
    }
}
