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

//    @Override
//    public void run() {
//
//        String ip = client.getRemoteSocketAddress().toString();
//        server.writeConsole("[Cliente #" + clientId + "] conectado desde " + ip);
//
//        // Escucha entradas y las imprime
//        StringBuilder sb = new StringBuilder();
//        // Jackson
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//
//            byte[] byteArray;
//            byteArray = new byte[100];
//            int bytesRead; // Guarda la cantidad real de bytes leídos
//
//            // Guardamos el resultado del read en bytesRead
//            while ((bytesRead = client.getInputStream().read(byteArray)) != -1) {
//
//                // Corregido: Convertimos solo los bytes reales que llegaron (evita basura)
//                sb.append(new String(byteArray, 0, bytesRead));
//                String receivedData = sb.toString().trim(); // Obtenemos el texto y quitamos espacios/saltos extra
//
//                server.writeConsole("[Cliente #" + clientId + "] JSON Crudo: " + receivedData);
//
//                // ---------------------JACKSON MAPPING--------------------
//                try {
//                    // Leemos el String JSON y lo convertimos a un árbol de nodos
//                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(receivedData);
//
//                    // Verificamos qué tipo de petición es
//                    if (rootNode.has("type")) {
//                        String tipo = rootNode.get("type").asText();
//
//                        if (tipo.equals("LOGIN")) {
//                            // Extraemos el email y el password
//                            String emailReq = rootNode.get("email").asText();
//                            String passReq = rootNode.get("password").asText();
//
//                            server.writeConsole("[Cliente #" + clientId + "] LOGIN REQUEST: Email: " + emailReq + " Password: " + passReq.toString());
//                            
//                            // Retrieve and verify from DB
//                            UserDAO userDAO = new UserDAO();
//                            User retrievedUser = userDAO.getUserByEmail(emailReq);
//                            
//                            // If right, send JSON of login successful
//                            if(retrievedUser.getPassword().equals(passReq)){
//                                
//                            } else  {
//                                
//                            }
//                                   
//                        }
//                    }
//                } catch (Exception jsonEx) {
//                    server.writeConsole("Advertencia: El mensaje recibido no es un JSON válido o está incompleto.");
//                }
//
//                /// Limpia string builder
//                sb.setLength(0);
//            }
//
//        } catch (IOException e) {
//            System.getLogger(ClientThread.class.getName()).log(
//                    System.Logger.Level.ERROR, "Connection error or client disconnected", e);
//
//            server.removeClient(clientId);
//        } finally {
//            server.removeClient(clientId);
//        }
//
//    }
//    
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

                            // Validar y responder al cliente
                            if (retrievedUser != null && retrievedUser.getPassword().equals(passReq)) {
                                server.writeConsole("[Cliente #" + clientId + "] LOGIN SUCCESS: For " + emailReq);
                                // Respuesta al cliente (el println agrega automáticamente el \n)
                                out.println("{\"type\": \"LOGIN_SUCCESS\", \"message\": \"Bienvenido\"}");
                            } else {
                                server.writeConsole("[Cliente #" + clientId + "] LOGIN FAILURE: For " + emailReq);
                                out.println("{\"type\": \"LOGIN_ERROR\", \"message\": \"Credenciales incorrectas\"}");
                            }
                        } else if (tipo.equals("SIGNUP")) {

                            String name = rootNode.get("name").asText();
                            String emailReq = rootNode.get("email").asText();
                            String passReq = rootNode.get("password").asText();
                            String state = rootNode.get("state").asText();

                            server.writeConsole("[Cliente #" + clientId + "] SIGNUP REQUEST: Name" + name + "Email: " + emailReq + " Password: " + passReq);

                            // Retrieve and verify from DB
                            UserDAO userDAO = new UserDAO();
                            User newUser = new User(name, emailReq, passReq, state);

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
