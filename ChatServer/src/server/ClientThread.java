package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.ConversationDAO;
import dao.ConversationMemberDAO;
import dao.FriendshipDAO;
import dao.GroupDAO;
import dao.GroupInvitationDAO;
import dao.MessageDAO;
import dao.UserDAO;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.FriendRequest;
import model.Group;
import model.GroupInvitation;
import model.Message;
import model.TodosMessage;
import model.User;

/**
 *
 * @author adotal
 */
public class ClientThread implements Runnable {

    private final int clientId;
    private final Socket client;
    private final JavaServer server;

// Class-level properties to allow external threads to message this client
    private java.io.PrintWriter out;
    private String userEmail = null;

    public ClientThread(Socket client, int clientId, JavaServer server) {
        this.client = client;
        this.clientId = clientId;
        this.server = server;

    }

    public int getId() {
        return clientId;
    }
    
    public String getEmail(){
        return this.userEmail;
    }

    // Public helper to securely push raw text down this specific socket
    public synchronized void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    // Self-contained method to fetch and send the updated list to this client
    public void sendUserListUpdate() {

        if (this.userEmail == null) {
            return; // Do not send updates to clients still on the login screen
        }
        try {

            System.out.println("[SENT:] " + this.userEmail);

            ObjectMapper mapper = new ObjectMapper();
            UserDAO userDAO = new UserDAO();
            ArrayList<User> activeUsers = userDAO.getAllUsersNotEmail(this.userEmail);

            for (User u : activeUsers) {
                u.setPassword(null); // Security erasure
            }

            ObjectNode response = mapper.createObjectNode();
            response.put("type", "UPDATE_USERS_LIST");
            response.set("users", mapper.valueToTree(activeUsers));

            sendMessage(mapper.writeValueAsString(response));
        } catch (Exception e) {
            server.writeConsole("[Cliente #" + clientId + "] Error processing broadcast update: " + e.getMessage());
        }
    }

    @Override
    public void run() {

        String ip = client.getRemoteSocketAddress().toString();
        server.writeConsole("[Cliente #" + clientId + "] CONNECTED from " + ip);

        ObjectMapper mapper = new ObjectMapper();

        // BufferedReader y PrintWriter para manejar líneas completas de texto
        try (
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(client.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
                ); java.io.PrintWriter out = new java.io.PrintWriter(
                        new java.io.OutputStreamWriter(client.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true
                )) {
            // Initialize class field writer
            this.out = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(client.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true
            );
            String receivedData;

            // readLine() se pausa automáticamente hasta recibir un '\n'. 
            // Garantiza que 'receivedData' tiene el JSON entero.
            while ((receivedData = reader.readLine()) != null) {

                server.writeConsole("[Cliente #" + clientId + "] JSON Crudo: " + receivedData);

                // ---------------------JACKSON MAPPING--------------------
                try {
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(receivedData);

                    if (rootNode.has("type")) {
                        String tipo = rootNode.get("type").asText();

                        if (tipo.equals("LOGIN")) {
                            String emailReq = rootNode.get("email").asText();
                            String passReq = rootNode.get("password").asText();

                            server.writeConsole("[Cliente #" + clientId + "] LOGIN REQUEST: Email: " + emailReq + " Password: " + passReq);

                            // Retrieve and verify from DB
                            UserDAO userDAO = new UserDAO();
                            User retrievedUser = userDAO.getUserByEmail(emailReq);

                            // Validate password and send client response
                            if (retrievedUser != null && retrievedUser.getPassword().equals(passReq)) {

                                server.writeConsole("[Cliente #" + clientId + "] LOGIN SUCCESS: For " + emailReq);

                                // Change con status to true
                                userDAO.changeIsConnected(emailReq, true);
//                                retrievedUser.setIsConnected(true);
                                // Save current email status & tell server to update everyone
                                this.userEmail = emailReq;

                                // Clear the password for security before sending it over the network
                                retrievedUser.setPassword(null);

                                // Create a dynamic Jackson JSON object
                                ObjectNode responseNode = mapper.createObjectNode();
                                responseNode.put("type", "LOGIN_SUCCESS");
                                responseNode.put("message", "Bienvenido");
                                // Convert User object into a JsonNode and nest it
                                responseNode.set("user", mapper.valueToTree(retrievedUser));

                                // Convert the entire object node to a string and send it
                                String jsonResponse = mapper.writeValueAsString(responseNode);
                                out.println(jsonResponse);

                                // Notify all clients of the new online state
                                server.broadcastUserStatus();

                            } else {
                                // Registro de intento de autenticación fallido (RQF11/RQNF15)
                                server.logEvent("AUTH_FAILURE", "Intento de inicio de sesión fallido para " + emailReq);
                                out.println("{\"type\": \"LOGIN_ERROR\", \"message\": \"Credenciales incorrectas\"}");
                            }
                        } else if (tipo.equals("SIGNUP")) {

                            String name = rootNode.get("name").asText();
                            String emailReq = rootNode.get("email").asText();
                            String passReq = rootNode.get("password").asText();
                            // isConnected is false by default

                            server.writeConsole("[Cliente #" + clientId + "] SIGNUP REQUEST: Name" + name + "Email: " + emailReq + " Password: " + passReq);

                            // Retrieve and verify from DB
                            UserDAO userDAO = new UserDAO();
                            User newUser = new User(name, emailReq, passReq, false);

                            if (userDAO.insertUser(newUser)) {

                                // If SUCCESS
                                server.writeConsole("[Cliente #" + clientId + "] SIGNUP_SUCCESS: For " + emailReq);
                                // Respuesta al cliente
                                out.println("{\"type\": \"SIGNUP_SUCCESS\"}");

                            } else {

                                // If SUCCESS
                                server.writeConsole("[Cliente #" + clientId + "] SIGNUP_ERROR: For " + emailReq);
                                // Respuesta al cliente
                                out.println("{\"type\": \"SIGNUP_ERROR\"}");

                            }

                        } else if (tipo.equals("RECOVER_PASSWORD")) {

                            String emailReq = rootNode.get("email").asText();
                            String passReq = rootNode.get("password").asText();

                            server.writeConsole("[Cliente #" + clientId + "] RECOVER_PASSWORD_REQUEST: Email: " + emailReq + " Password: " + passReq);

                            // Chreate DAO and attempt to change password
                            UserDAO userDAO = new UserDAO();

                            if (userDAO.changePassword(emailReq, passReq)) {

                                server.writeConsole("[Cliente #" + clientId + "] RECOVER_PASSWORD_SUCCESS: For " + emailReq);
                                // Respuesta al cliente (el println agrega automáticamente el \n)
                                out.println("{\"type\": \"RECOVER_PASSWORD_SUCCESS\"}");

                            } else {

                                server.writeConsole("[Cliente #" + clientId + "] RECOVER_PASSWROD_ERROR: For " + emailReq);
                                out.println("{\"type\": \"RECOVER_PASSWROD_ERROR\", \"message\": \"El usuario no existe\"}");

                            }

                        } else if (tipo.equals("LOGOUT_REQUEST")) {

                            // Get email to logout
                            String emailReq = rootNode.get("email").asText();
                            server.writeConsole("[Cliente #" + clientId + "] LOGOUT_REQUEST: Email: " + emailReq);

                            UserDAO userDAO = new UserDAO();
                            // Make is_connected false
                            userDAO.changeIsConnected(emailReq, false);

                            // Chats con no-amigos son efímeros: borrar conversaciones TEMP del
                            // usuario al cerrar sesión y avisar al otro miembro (RQNF26/27).
                            User loggingOut = userDAO.getUserByEmail(emailReq);
                            if (loggingOut != null) {
                                cleanupTempConversations(loggingOut.getIdUser(), mapper);
                            }

                            // Return success
                            out.println("{\"type\": \"LOGOUT_SUCCESS\"}");

                            // Clear our email reference and alert everyone they went offline
                            this.userEmail = null;
                            server.broadcastUserStatus();

                        } else if (tipo.equals("FETCH_ALL_USERS")) {

                            // Get email not to load
                            String emailReq = rootNode.get("email").asText();

                            UserDAO userDAO = new UserDAO();
                            ArrayList<User> activeUsers = userDAO.getAllUsersNotEmail(emailReq);

                            // Create the wrapper envelope JSON object 
                            ObjectNode response = mapper.createObjectNode();
                            response.put("type", "UPDATE_USERS_LIST");

                            // Convert the ArrayList directly into a JSON Array Node
                            response.set("users", mapper.valueToTree(activeUsers));

                            // Send it to socket
                            String rawJsonToSend = mapper.writeValueAsString(response);
                            out.println(rawJsonToSend);
                        } else if (tipo.equals("SEND_MESSAGE")) {

                            String chatType = rootNode.get("chat_type").asText();

                            if (chatType.equals("TODOS")) {

                                // Mensaje directo entre dos usuarios.
                                TodosMessage message = mapper.treeToValue(rootNode.get("message"), TodosMessage.class);
                                String rawJsonPayload = rootNode.toString();

                                int idReceiver = message.getUserReceiver().getIdUser();
                                int idSender = message.getUserSender().getIdUser();
                                String emailReceiver = message.getUserReceiver().getEmail();
                                String text = message.getText();

                                // Validar mensaje no vacío (RQNF22/44)
                                if (text == null || text.trim().isEmpty()) {
                                    server.writeConsole("[SERVER] Mensaje vacío ignorado de User#" + idSender);
                                } else {
                                    ConversationDAO convDAO = new ConversationDAO();
                                    ConversationMemberDAO memberDAO = new ConversationMemberDAO();
                                    boolean friends = new FriendshipDAO().areFriends(idSender, idReceiver);

                                    // La pestaña de origen decide el comportamiento: desde "Todos"
                                    // el chat es siempre efímero (TEMP), aun entre amigos; desde
                                    // "Amigos" es persistente (FRIEND). Pueden coexistir ambas.
                                    String accessMode = rootNode.has("access_mode")
                                            ? rootNode.get("access_mode").asText() : "TODOS";
                                    boolean persistent = friends && "AMIGOS".equals(accessMode);
                                    String desiredType = persistent ? "FRIEND" : "TEMP";

                                    // Resolver/crear conversación del tipo correspondiente
                                    int idConv = convDAO.getConversationIdByUsersAndType(idSender, idReceiver, desiredType);
                                    if (idConv == -1) {
                                        idConv = convDAO.createDirectConversation(desiredType);
                                        memberDAO.addMember(idSender, idConv);
                                        memberDAO.addMember(idReceiver, idConv);
                                    }

                                    // Persistir el mensaje en su conversación (FRIEND persiste de forma
                                    // permanente; TEMP persiste solo mientras ambos siguen en línea: se
                                    // borra al primer logout/desconexión vía cleanupTempConversations).
                                    new MessageDAO().insertMessage(new Message(0, idConv, idSender, text, null));
                                    convDAO.updateLastSeen(idConv);

                                    // Reenviar al receptor conectado (RQNF25/50)
                                    ClientThread targetClient = server.findClientByUserEmail(emailReceiver);
                                    if (targetClient != null) {
                                        targetClient.sendMessage(rawJsonPayload);
                                        server.writeConsole("[SERVER] Mensaje directo de User#" + idSender + " a User#" + idReceiver);
                                    } else {
                                        server.writeConsole("[SERVER] User#" + idReceiver + " OFFLINE; mensaje no entregado en vivo.");
                                    }
                                }

                            } else if (chatType.equals("GROUP")) {
                                handleGroupMessage(rootNode, mapper);
                            }

                        } else if (tipo.equals("SEND_FRIEND_REQUEST")) {
                            handleSendFriendRequest(rootNode, mapper);
                        } else if (tipo.equals("FETCH_FRIEND_REQUESTS")) {
                            handleFetchFriendRequests(rootNode, mapper);
                        } else if (tipo.equals("RESPOND_FRIEND_REQUEST")) {
                            handleRespondFriendRequest(rootNode, mapper);
                        } else if (tipo.equals("FETCH_FRIENDS")) {
                            handleFetchFriends(rootNode, mapper);
                        } else if (tipo.equals("FETCH_CONVERSATION_HISTORY")) {
                            handleFetchConversationHistory(rootNode, mapper);
                        } else if (tipo.equals("CREATE_GROUP")) {
                            handleCreateGroup(rootNode, mapper);
                        } else if (tipo.equals("INVITE_TO_GROUP")) {
                            handleInviteToGroup(rootNode, mapper);
                        } else if (tipo.equals("FETCH_GROUP_INVITATIONS")) {
                            handleFetchGroupInvitations(rootNode, mapper);
                        } else if (tipo.equals("RESPOND_GROUP_INVITATION")) {
                            handleRespondGroupInvitation(rootNode, mapper);
                        } else if (tipo.equals("FETCH_GROUPS")) {
                            handleFetchGroups(rootNode, mapper);
                        } else if (tipo.equals("FETCH_GROUP_MEMBERS")) {
                            handleFetchGroupMembers(rootNode, mapper);
                        } else if (tipo.equals("LEAVE_GROUP")) {
                            handleLeaveGroup(rootNode, mapper);
                        } else if (tipo.equals("DELETE_GROUP")) {
                            handleDeleteGroup(rootNode, mapper);
                        } else if (tipo.equals("FETCH_GROUP_HISTORY")) {
                            handleFetchGroupHistory(rootNode, mapper);
                        }
                    }
                } catch (Exception jsonEx) {
                    server.writeConsole("Advertencia: El mensaje recibido no es un JSON válido. " + jsonEx.getMessage());
                }
                // --------------------------------------------------------
            }

        } catch (IOException e) {
            System.getLogger(ClientThread.class.getName()).log(
                    System.Logger.Level.ERROR, "Conexión interrumpida o cliente desconectado", e);
        } finally {
            // Clean up safety layer. If window closed abruptly, force DB offline status
            if (this.userEmail != null) {
                try {
                    UserDAO userDAO = new UserDAO();
                    userDAO.changeIsConnected(this.userEmail, false);
                    // Borrar conversaciones TEMP efímeras también en desconexión abrupta
                    // y avisar al otro miembro para que limpie su vista (RQNF26/27).
                    User droppedUser = userDAO.getUserByEmail(this.userEmail);
                    if (droppedUser != null) {
                        cleanupTempConversations(droppedUser.getIdUser(), new ObjectMapper());
                    }
                } catch (Exception ex) {
                    server.writeConsole("Error setting database offline state on unexpected drop.");
                }
            }
            server.removeClient(clientId);

            // NEW: Alert remaining users that this client left
            server.broadcastUserStatus();

        }
    }

    // =====================================================================
    //  Utilidades comunes
    // =====================================================================

    /** Serializa un User a JSON sin exponer la contraseña. */
    private ObjectNode userToNode(ObjectMapper mapper, User u) {
        u.setPassword(null);
        return (ObjectNode) mapper.valueToTree(u);
    }

    /** Empuja un JSON crudo a un usuario por id, si está conectado. */
    private void pushToUser(int idUser, String json) {
        User u = new UserDAO().getUserById(idUser);
        if (u != null) {
            ClientThread t = server.findClientByUserEmail(u.getEmail());
            if (t != null) {
                t.sendMessage(json);
            }
        }
    }

    /**
     * Borra las conversaciones TEMP (efímeras, entre no-amigos) del usuario y
     * notifica a los demás miembros conectados para que limpien su vista del
     * chat (RQNF26/27). Se invoca tanto en logout limpio como en desconexión
     * abrupta.
     */
    private void cleanupTempConversations(int userId, ObjectMapper mapper) {
        try {
            ConversationDAO convDAO = new ConversationDAO();
            ConversationMemberDAO memberDAO = new ConversationMemberDAO();
            for (int idConv : convDAO.getDirectConversationsByUserAndType(userId, "TEMP")) {
                // Avisar al otro miembro conectado antes de borrar la conversación.
                for (User m : memberDAO.getMembersByConversation(idConv)) {
                    if (m.getIdUser() == userId) {
                        continue;
                    }
                    ClientThread t = server.findClientByUserEmail(m.getEmail());
                    if (t != null) {
                        ObjectNode n = mapper.createObjectNode();
                        n.put("type", "DELETE_CHAT");
                        n.put("otherUserId", userId);
                        // Solo afecta a la ventana de "Todos" (chat efímero).
                        n.put("access_mode", "TODOS");
                        t.sendMessage(mapper.writeValueAsString(n));
                    }
                }
                convDAO.deleteConversation(idConv);
            }
        } catch (Exception e) {
            server.writeConsole("[SERVER] cleanupTempConversations: " + e.getMessage());
        }
    }

    private void sendError(ObjectMapper mapper, String type, String message) {
        try {
            ObjectNode n = mapper.createObjectNode();
            n.put("type", type);
            n.put("message", message);
            sendMessage(mapper.writeValueAsString(n));
        } catch (Exception e) {
            server.writeConsole("[SERVER] Error enviando error al cliente: " + e.getMessage());
        }
    }

    // =====================================================================
    //  Bloque A — Solicitudes de amistad
    // =====================================================================

    private void handleSendFriendRequest(JsonNode root, ObjectMapper mapper) {
        try {
            int senderId = root.get("senderId").asInt();
            String receiverEmail = root.get("receiverEmail").asText();

            UserDAO userDAO = new UserDAO();
            User receiver = userDAO.getUserByEmail(receiverEmail);
            if (receiver == null) {
                sendError(mapper, "FRIEND_REQUEST_ERROR", "El usuario no existe.");
                return;
            }
            int receiverId = receiver.getIdUser();

            FriendshipDAO friendDAO = new FriendshipDAO();
            if (friendDAO.areFriends(senderId, receiverId)) {
                sendError(mapper, "FRIEND_REQUEST_ERROR", "Este usuario ya es tu amigo.");
                return;
            }
            if (friendDAO.requestAlreadyExists(senderId, receiverId)) {
                sendError(mapper, "FRIEND_REQUEST_ERROR", "Ya existe una solicitud con este usuario.");
                return;
            }

            int idFriendship = friendDAO.sendFriendRequest(senderId, receiverId);
            if (idFriendship == -1) {
                sendError(mapper, "FRIEND_REQUEST_ERROR", "No se pudo enviar la solicitud.");
                return;
            }

            ObjectNode ok = mapper.createObjectNode();
            ok.put("type", "FRIEND_REQUEST_SENT");
            ok.put("message", "Solicitud enviada.");
            sendMessage(mapper.writeValueAsString(ok));

            // Notificar al receptor conectado (RQNF30)
            ObjectNode notify = mapper.createObjectNode();
            notify.put("type", "NEW_FRIEND_REQUEST");
            pushToUser(receiverId, mapper.writeValueAsString(notify));

            server.logEvent("FRIEND_REQUEST", "User#" + senderId + " envió solicitud a User#" + receiverId);
        } catch (Exception e) {
            sendError(mapper, "FRIEND_REQUEST_ERROR", "Error al procesar la solicitud.");
            server.writeConsole("[SERVER] handleSendFriendRequest: " + e.getMessage());
        }
    }

    private void handleFetchFriendRequests(JsonNode root, ObjectMapper mapper) {
        try {
            int userId = root.get("userId").asInt();
            FriendshipDAO friendDAO = new FriendshipDAO();

            ArrayNode received = mapper.createArrayNode();
            for (FriendRequest fr : friendDAO.getPendingRequests(userId)) {
                ObjectNode node = mapper.createObjectNode();
                node.put("id", fr.getId());
                node.set("sender", userToNode(mapper, fr.getSenderUser()));
                received.add(node);
            }

            ArrayNode sent = mapper.createArrayNode();
            for (FriendRequest fr : friendDAO.getSentRequests(userId)) {
                ObjectNode node = mapper.createObjectNode();
                node.put("id", fr.getId());
                node.put("status", fr.getStatus().name());
                node.set("target", userToNode(mapper, fr.getTargetUser()));
                sent.add(node);
            }

            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "FRIEND_REQUESTS_LIST");
            resp.set("received", received);
            resp.set("sent", sent);
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchFriendRequests: " + e.getMessage());
        }
    }

    private void handleRespondFriendRequest(JsonNode root, ObjectMapper mapper) {
        try {
            int idFriendship = root.get("idFriendship").asInt();
            boolean accept = root.get("accept").asBoolean();

            FriendshipDAO friendDAO = new FriendshipDAO();
            int[] users = friendDAO.getUsersOfRequest(idFriendship);

            if (accept) {
                friendDAO.acceptRequest(idFriendship);
                // No se promueve la conversación TEMP de "Todos": es efímera por diseño.
                // El chat de "Amigos" crea su propia conversación FRIEND al primer mensaje.
            } else {
                friendDAO.denyRequest(idFriendship);
            }

            ObjectNode ok = mapper.createObjectNode();
            ok.put("type", "FRIEND_REQUEST_RESPONDED");
            sendMessage(mapper.writeValueAsString(ok));

            // Avisar a ambos para que refresquen amigos y solicitudes (RQNF34/41)
            if (users != null) {
                ObjectNode upd = mapper.createObjectNode();
                upd.put("type", "FRIENDS_LIST_UPDATED");
                String s = mapper.writeValueAsString(upd);
                pushToUser(users[0], s);
                pushToUser(users[1], s);
            }
            server.logEvent("FRIEND_RESPONSE", "Solicitud " + idFriendship + (accept ? " aceptada" : " rechazada"));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleRespondFriendRequest: " + e.getMessage());
        }
    }

    private void handleFetchFriends(JsonNode root, ObjectMapper mapper) {
        try {
            int userId = root.get("userId").asInt();
            List<User> friends = new FriendshipDAO().getFriendsByUser(userId);
            for (User u : friends) {
                u.setPassword(null);
            }
            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "FRIENDS_LIST");
            resp.set("friends", mapper.valueToTree(friends));
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchFriends: " + e.getMessage());
        }
    }

    // =====================================================================
    //  Bloque B — Historial de conversación directa
    // =====================================================================

    private void handleFetchConversationHistory(JsonNode root, ObjectMapper mapper) {
        try {
            int userId = root.get("userId").asInt();
            int otherUserId = root.get("otherUserId").asInt();

            // El historial mostrado coincide con la pestaña: Amigos→FRIEND (persistido);
            // Todos→TEMP (efímero, sin historial persistido).
            String accessMode = root.has("access_mode") ? root.get("access_mode").asText() : "TODOS";
            boolean persistent = "AMIGOS".equals(accessMode)
                    && new FriendshipDAO().areFriends(userId, otherUserId);
            String desiredType = persistent ? "FRIEND" : "TEMP";

            ArrayNode msgs = mapper.createArrayNode();
            ConversationDAO convDAO = new ConversationDAO();
            int idConv = convDAO.getConversationIdByUsersAndType(userId, otherUserId, desiredType);
            if (idConv != -1) {
                UserDAO userDAO = new UserDAO();
                User self = userDAO.getUserById(userId);
                User other = userDAO.getUserById(otherUserId);
                for (Message m : new MessageDAO().getMessagesByConversation(idConv)) {
                    ObjectNode mn = mapper.createObjectNode();
                    mn.put("idSender", m.getIdSender());
                    String name = (self != null && m.getIdSender() == userId) ? self.getName()
                            : (other != null ? other.getName() : "");
                    mn.put("senderName", name);
                    mn.put("content", m.getContent());
                    mn.put("sentDate", m.getSentDate());
                    msgs.add(mn);
                }
            }

            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "CONVERSATION_HISTORY");
            resp.put("otherUserId", otherUserId);
            resp.set("messages", msgs);
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchConversationHistory: " + e.getMessage());
        }
    }

    // =====================================================================
    //  Bloque C — Grupos
    // =====================================================================

    /** Notifica a todos los miembros actuales de la conversación de un grupo. */
    private void notifyGroupConversation(int idGroup, ObjectMapper mapper, String type) {
        try {
            int idConv = new ConversationDAO().getConversationIdByGroup(idGroup);
            if (idConv == -1) {
                return;
            }
            ObjectNode n = mapper.createObjectNode();
            n.put("type", type);
            n.put("group_id", idGroup);
            String s = mapper.writeValueAsString(n);
            for (User m : new ConversationMemberDAO().getMembersByConversation(idConv)) {
                ClientThread t = server.findClientByUserEmail(m.getEmail());
                if (t != null) {
                    t.sendMessage(s);
                }
            }
        } catch (Exception e) {
            server.writeConsole("[SERVER] notifyGroupConversation: " + e.getMessage());
        }
    }

    /**
     * Elimina un grupo por completo y notifica a todos los implicados.
     *
     * No depende de ON DELETE CASCADE (poco confiable ante drift de esquema):
     * borra explícitamente la conversación (mensajes + miembros + conversación),
     * las invitaciones y finalmente el grupo. Reúne a los destinatarios ANTES de
     * borrar y les envía GROUP_DELETED para que no quede rastro en la UI.
     */
    private void deleteGroupAndNotify(int idGroup, ObjectMapper mapper) {
        try {
            GroupDAO groupDAO = new GroupDAO();
            GroupInvitationDAO invDAO = new GroupInvitationDAO();
            List<Integer> toNotify = invDAO.getInvitedUserIds(idGroup);
            int ownerId = groupDAO.getOwnerId(idGroup);

            int idConv = new ConversationDAO().getConversationIdByGroup(idGroup);
            if (idConv != -1) {
                new ConversationDAO().deleteConversation(idConv);
            }
            invDAO.deleteByGroup(idGroup);
            groupDAO.deleteGroup(idGroup);

            ObjectNode n = mapper.createObjectNode();
            n.put("type", "GROUP_DELETED");
            n.put("group_id", idGroup);
            String s = mapper.writeValueAsString(n);
            for (int uid : toNotify) {
                pushToUser(uid, s);
            }
            if (ownerId != -1) {
                pushToUser(ownerId, s);
            }
        } catch (Exception e) {
            server.writeConsole("[SERVER] deleteGroupAndNotify: " + e.getMessage());
        }
    }

    /**
     * Regla de permanencia (RQNF71/72/79): un grupo solo sobrevive si puede
     * alcanzar ≥3 miembros (aceptados incluido el creador + invitaciones aún
     * pendientes). Si no, se elimina y se notifica a todos los implicados.
     * Devuelve true si el grupo fue eliminado.
     */
    private boolean evaluateGroupPermanence(int idGroup, ObjectMapper mapper) {
        try {
            GroupDAO groupDAO = new GroupDAO();
            if (!groupDAO.exists(idGroup)) {
                return true;
            }
            GroupInvitationDAO invDAO = new GroupInvitationDAO();
            int approved = groupDAO.countApprovedMembers(idGroup); // incluye al creador
            int pending = invDAO.countPending(idGroup);
            if (approved + pending < 3) {
                deleteGroupAndNotify(idGroup, mapper);
                server.logEvent("GROUP_DELETED", "Grupo " + idGroup + " eliminado por regla de permanencia (<3 miembros).");
                return true;
            }
        } catch (Exception e) {
            server.writeConsole("[SERVER] evaluateGroupPermanence: " + e.getMessage());
        }
        return false;
    }

    private void handleCreateGroup(JsonNode root, ObjectMapper mapper) {
        try {
            int ownerId = root.get("ownerId").asInt();
            String title = root.get("title").asText();
            if (title == null || title.trim().isEmpty()) {
                sendError(mapper, "GROUP_ERROR", "El nombre del grupo no puede estar vacío.");
                return;
            }

            GroupDAO groupDAO = new GroupDAO();
            int idGroup = groupDAO.createGroup(title, ownerId);
            if (idGroup == -1) {
                sendError(mapper, "GROUP_ERROR", "No se pudo crear el grupo.");
                return;
            }
            ConversationDAO convDAO = new ConversationDAO();
            int idConv = convDAO.createGroupConversation(idGroup);
            new ConversationMemberDAO().addMember(ownerId, idConv);

            // Invitaciones iniciales (RQNF55: solo usuarios registrados)
            UserDAO userDAO = new UserDAO();
            GroupInvitationDAO invDAO = new GroupInvitationDAO();
            if (root.has("invitedEmails") && root.get("invitedEmails").isArray()) {
                for (JsonNode emailNode : root.get("invitedEmails")) {
                    User invited = userDAO.getUserByEmail(emailNode.asText());
                    if (invited != null) {
                        invDAO.invite(idGroup, invited.getIdUser());
                        ObjectNode notify = mapper.createObjectNode();
                        notify.put("type", "GROUP_INVITATION_RECEIVED");
                        pushToUser(invited.getIdUser(), mapper.writeValueAsString(notify));
                    }
                }
            }

            ObjectNode ok = mapper.createObjectNode();
            ok.put("type", "GROUP_CREATED");
            ok.put("group_id", idGroup);
            ok.put("title", title);
            sendMessage(mapper.writeValueAsString(ok));
            server.logEvent("GROUP_CREATED", "User#" + ownerId + " creó grupo " + idGroup + " (" + title + ")");
        } catch (Exception e) {
            sendError(mapper, "GROUP_ERROR", "Error al crear el grupo.");
            server.writeConsole("[SERVER] handleCreateGroup: " + e.getMessage());
        }
    }

    private void handleInviteToGroup(JsonNode root, ObjectMapper mapper) {
        try {
            int idGroup = root.get("groupId").asInt();
            String invitedEmail = root.get("invitedEmail").asText();

            User invited = new UserDAO().getUserByEmail(invitedEmail);
            if (invited == null) {
                sendError(mapper, "GROUP_ERROR", "El usuario a invitar no existe.");
                return;
            }
            int idInv = new GroupInvitationDAO().invite(idGroup, invited.getIdUser());
            if (idInv == -1) {
                sendError(mapper, "GROUP_ERROR", "El usuario ya fue invitado.");
                return;
            }

            ObjectNode notify = mapper.createObjectNode();
            notify.put("type", "GROUP_INVITATION_RECEIVED");
            pushToUser(invited.getIdUser(), mapper.writeValueAsString(notify));

            ObjectNode ok = mapper.createObjectNode();
            ok.put("type", "GROUP_INVITE_SENT");
            sendMessage(mapper.writeValueAsString(ok));
            server.logEvent("GROUP_INVITE", "Invitación a grupo " + idGroup + " para " + invitedEmail);
        } catch (Exception e) {
            sendError(mapper, "GROUP_ERROR", "Error al invitar al grupo.");
            server.writeConsole("[SERVER] handleInviteToGroup: " + e.getMessage());
        }
    }

    private void handleFetchGroupInvitations(JsonNode root, ObjectMapper mapper) {
        try {
            int userId = root.get("userId").asInt();
            ArrayNode arr = mapper.createArrayNode();
            for (GroupInvitation gi : new GroupInvitationDAO().getPendingInvitationsByUser(userId)) {
                ObjectNode node = mapper.createObjectNode();
                node.put("id", gi.getId());
                ObjectNode g = mapper.createObjectNode();
                g.put("id", gi.getGroup().getId());
                g.put("title", gi.getGroup().getTitle());
                node.set("group", g);
                node.set("owner", userToNode(mapper, gi.getGroupOwnerUser()));
                arr.add(node);
            }
            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "GROUP_INVITATIONS_LIST");
            resp.set("invitations", arr);
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchGroupInvitations: " + e.getMessage());
        }
    }

    private void handleRespondGroupInvitation(JsonNode root, ObjectMapper mapper) {
        try {
            int idInvitation = root.get("idInvitation").asInt();
            boolean accept = root.get("accept").asBoolean();

            GroupInvitationDAO invDAO = new GroupInvitationDAO();
            int[] target = invDAO.getInvitationTarget(idInvitation); // {idGroup, idInvited}
            if (target == null) {
                sendError(mapper, "GROUP_ERROR", "La invitación no existe.");
                return;
            }
            int idGroup = target[0];
            int idInvited = target[1];

            if (accept) {
                invDAO.accept(idInvitation);
                int idConv = ensureGroupConversation(idGroup);
                if (idConv != -1) {
                    new ConversationMemberDAO().addMember(idInvited, idConv);
                }
            } else {
                invDAO.deny(idInvitation);
            }

            ObjectNode ok = mapper.createObjectNode();
            ok.put("type", "GROUP_INVITATION_RESPONDED");
            sendMessage(mapper.writeValueAsString(ok));

            // Evaluar permanencia; si no se elimina, refrescar a los miembros
            boolean deleted = evaluateGroupPermanence(idGroup, mapper);
            if (!deleted) {
                notifyGroupConversation(idGroup, mapper, "GROUPS_LIST_UPDATED");
                notifyGroupConversation(idGroup, mapper, "GROUP_MEMBERS_UPDATED");
            }
            server.logEvent("GROUP_INV_RESPONSE", "Invitación " + idInvitation + (accept ? " aceptada" : " rechazada"));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleRespondGroupInvitation: " + e.getMessage());
        }
    }

    private void handleFetchGroups(JsonNode root, ObjectMapper mapper) {
        try {
            int userId = root.get("userId").asInt();
            ArrayNode arr = mapper.createArrayNode();
            for (Group g : new GroupDAO().getGroupsByUser(userId)) {
                ObjectNode node = mapper.createObjectNode();
                node.put("id", g.getId());
                node.put("title", g.getTitle());
                arr.add(node);
            }
            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "GROUPS_LIST");
            resp.set("groups", arr);
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchGroups: " + e.getMessage());
        }
    }

    private void handleFetchGroupMembers(JsonNode root, ObjectMapper mapper) {
        try {
            int idGroup = root.get("groupId").asInt();
            GroupDAO groupDAO = new GroupDAO();
            ArrayNode arr = mapper.createArrayNode();
            for (GroupInvitation gi : new GroupInvitationDAO().getInvitationsByGroup(idGroup)) {
                ObjectNode node = mapper.createObjectNode();
                node.put("status", gi.getStatus().name());
                node.set("invited", userToNode(mapper, gi.getInvitedUser()));
                arr.add(node);
            }
            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "GROUP_MEMBERS");
            resp.put("group_id", idGroup);
            resp.put("owner_id", groupDAO.getOwnerId(idGroup));
            resp.set("invitations", arr);
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchGroupMembers: " + e.getMessage());
        }
    }

    private void handleLeaveGroup(JsonNode root, ObjectMapper mapper) {
        try {
            int idGroup = root.get("groupId").asInt();
            int userId = root.get("userId").asInt();

            GroupDAO groupDAO = new GroupDAO();
            if (!groupDAO.exists(idGroup)) {
                return;
            }

            if (groupDAO.isOwner(idGroup, userId)) {
                // El creador abandona => se elimina el grupo (RQNF77)
                deleteGroupAndNotify(idGroup, mapper);
                server.logEvent("GROUP_LEFT", "Creador User#" + userId + " abandonó y eliminó grupo " + idGroup);
                return;
            }

            // Miembro normal abandona
            int idConv = new ConversationDAO().getConversationIdByGroup(idGroup);
            if (idConv != -1) {
                new ConversationMemberDAO().removeMember(userId, idConv);
            }
            new GroupInvitationDAO().removeInvitation(idGroup, userId);

            ObjectNode ok = mapper.createObjectNode();
            ok.put("type", "GROUP_LEFT_OK");
            ok.put("group_id", idGroup);
            sendMessage(mapper.writeValueAsString(ok));

            boolean deleted = evaluateGroupPermanence(idGroup, mapper);
            if (!deleted) {
                notifyGroupConversation(idGroup, mapper, "GROUP_MEMBERS_UPDATED");
            }
            server.logEvent("GROUP_LEFT", "User#" + userId + " abandonó grupo " + idGroup);
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleLeaveGroup: " + e.getMessage());
        }
    }

    private void handleDeleteGroup(JsonNode root, ObjectMapper mapper) {
        try {
            int idGroup = root.get("groupId").asInt();
            int userId = root.get("userId").asInt();

            GroupDAO groupDAO = new GroupDAO();
            if (!groupDAO.isOwner(idGroup, userId)) {
                sendError(mapper, "GROUP_ERROR", "Solo el creador puede eliminar el grupo.");
                return;
            }
            deleteGroupAndNotify(idGroup, mapper);
            server.logEvent("GROUP_DELETED", "Creador User#" + userId + " eliminó grupo " + idGroup);
        } catch (Exception e) {
            sendError(mapper, "GROUP_ERROR", "Error al eliminar el grupo.");
            server.writeConsole("[SERVER] handleDeleteGroup: " + e.getMessage());
        }
    }

    private void handleFetchGroupHistory(JsonNode root, ObjectMapper mapper) {
        try {
            int idGroup = root.get("groupId").asInt();
            ArrayNode msgs = mapper.createArrayNode();
            int idConv = new ConversationDAO().getConversationIdByGroup(idGroup);
            if (idConv != -1) {
                ConversationMemberDAO memberDAO = new ConversationMemberDAO();
                java.util.Map<Integer, String> names = new java.util.HashMap<>();
                for (User u : memberDAO.getMembersByConversation(idConv)) {
                    names.put(u.getIdUser(), u.getName());
                }
                for (Message m : new MessageDAO().getMessagesByConversation(idConv)) {
                    ObjectNode mn = mapper.createObjectNode();
                    mn.put("idSender", m.getIdSender());
                    String name = names.get(m.getIdSender());
                    if (name == null) {
                        User s = new UserDAO().getUserById(m.getIdSender());
                        name = (s != null) ? s.getName() : "";
                    }
                    mn.put("senderName", name);
                    mn.put("content", m.getContent());
                    mn.put("sentDate", m.getSentDate());
                    msgs.add(mn);
                }
            }
            ObjectNode resp = mapper.createObjectNode();
            resp.put("type", "GROUP_HISTORY");
            resp.put("group_id", idGroup);
            resp.set("messages", msgs);
            sendMessage(mapper.writeValueAsString(resp));
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleFetchGroupHistory: " + e.getMessage());
        }
    }

    /**
     * Garantiza que exista la conversación GROUP del grupo y que sus miembros
     * (owner + invitados APPROVED) estén registrados en conversation_members.
     * Auto-repara grupos creados antes de corregir el esquema, cuya conversación
     * nunca llegó a crearse. Idempotente: addMember ignora duplicados.
     *
     * @return el id de la conversación de grupo, o -1 si no se pudo crear.
     */
    private int ensureGroupConversation(int idGroup) {
        ConversationDAO convDAO = new ConversationDAO();
        int idConv = convDAO.getConversationIdByGroup(idGroup);
        if (idConv == -1) {
            idConv = convDAO.createGroupConversation(idGroup);
            if (idConv == -1) {
                return -1;
            }
        }
        ConversationMemberDAO memberDAO = new ConversationMemberDAO();
        int ownerId = new GroupDAO().getOwnerId(idGroup);
        if (ownerId != -1) {
            memberDAO.addMember(ownerId, idConv);
        }
        for (GroupInvitation gi : new GroupInvitationDAO().getInvitationsByGroup(idGroup)) {
            if (gi.getStatus() == model.Request.RequestStatus.APPROVED && gi.getInvitedUser() != null) {
                memberDAO.addMember(gi.getInvitedUser().getIdUser(), idConv);
            }
        }
        return idConv;
    }

    private void handleGroupMessage(JsonNode root, ObjectMapper mapper) {
        try {
            int idGroup = root.get("group_id").asInt();
            JsonNode senderNode = root.get("sender");
            int idSender = senderNode.get("idUser").asInt();
            String text = root.get("text").asText();

            if (text == null || text.trim().isEmpty()) {
                return; // validar no vacío (RQNF83)
            }

            ConversationDAO convDAO = new ConversationDAO();
            int idConv = ensureGroupConversation(idGroup);
            if (idConv == -1) {
                return;
            }

            // Persistir (RQNF85/89) y actualizar recencia
            new MessageDAO().insertMessage(new Message(0, idConv, idSender, text, null));
            convDAO.updateLastSeen(idConv);

            // Distribuir a los miembros activos excepto el remitente (RQNF84/88)
            String raw = root.toString();
            for (User m : new ConversationMemberDAO().getMembersByConversation(idConv)) {
                if (m.getIdUser() == idSender) {
                    continue;
                }
                ClientThread t = server.findClientByUserEmail(m.getEmail());
                if (t != null) {
                    t.sendMessage(raw);
                }
            }
        } catch (Exception e) {
            server.writeConsole("[SERVER] handleGroupMessage: " + e.getMessage());
        }
    }

}
