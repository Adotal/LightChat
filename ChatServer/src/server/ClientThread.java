package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.UserDAO;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
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

    // Public helper to securely push raw text down this specific socket
    public synchronized void sendMessage(String msg) {
        System.out.println("ARRIVE OUT");
        if (out != null) {
            out.println(msg);
            System.out.println("ARRIVE SENT");
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
                                com.fasterxml.jackson.databind.node.ObjectNode responseNode = mapper.createObjectNode();
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
                                server.writeConsole("[Cliente #" + clientId + "] LOGIN FAILURE: For " + emailReq);
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

                            // HERE ALL TODOS MESSAGES WITH EMAIL emailReq SHOULD BE DELETED
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
                    new UserDAO().changeIsConnected(this.userEmail, false);
                } catch (Exception ex) {
                    server.writeConsole("Error setting database offline state on unexpected drop.");
                }
            }
            server.removeClient(clientId);

            // NEW: Alert remaining users that this client left
            server.broadcastUserStatus();

        }
    }

}
