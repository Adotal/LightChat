package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.UserDAO;
import java.io.IOException;
import java.net.Socket;
import model.User;

/**
 *
 * @author adotal
 */
public class ClientThread implements Runnable {

    private final int clientId;
    private final Socket client;
    private final JavaServer server;

    public ClientThread(Socket client, int clientId, JavaServer server) {
        this.client = client;
        this.clientId = clientId;
        this.server = server;

    }

    public int getId() {
        return clientId;
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

                            userDAO.insertUser(newUser);

//                            if SUCCESS
                            server.writeConsole("[Cliente #" + clientId + "] SIGNUP SUCCESS: For " + emailReq);
                            // Respuesta al cliente
                            out.println("{\"type\": \"SIGNUP_SUCCESS\"}");

                            // Validar y responder al cliente
//                            if (retrievedUser != null && retrievedUser.getPassword().equals(passReq)) {
//                                server.writeConsole("[Cliente #" + clientId + "] LOGIN SUCCESS: For " + emailReq);
//                                // Respuesta al cliente (el println agrega automáticamente el \n)
//                                out.println("{\"type\": \"LOGIN_SUCCESS\", \"message\": \"Bienvenido\"}");
//                            } else {
//                                server.writeConsole("[Cliente #" + clientId + "] LOGIN FAILURE: For " + emailReq);
//                                out.println("{\"type\": \"LOGIN_ERROR\", \"message\": \"Credenciales incorrectas\"}");
//                            }
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
            server.removeClient(clientId);
        }
    }

}
