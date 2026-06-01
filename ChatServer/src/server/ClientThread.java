package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.UserDAO;
import java.io.IOException;
import java.net.Socket;

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
        server.writeConsole("[Cliente #" + clientId + "] conectado desde " + ip);

        // Escucha entradas y las imprime
        StringBuilder sb = new StringBuilder();
        // Jackson
        ObjectMapper mapper = new ObjectMapper();

        try {

            byte[] byteArray;
            byteArray = new byte[100];
            int bytesRead; // Guarda la cantidad real de bytes leídos

            // Guardamos el resultado del read en bytesRead
            while ((bytesRead = client.getInputStream().read(byteArray)) != -1) {

                // Corregido: Convertimos solo los bytes reales que llegaron (evita basura)
                sb.append(new String(byteArray, 0, bytesRead));
                String receivedData = sb.toString().trim(); // Obtenemos el texto y quitamos espacios/saltos extra

                server.writeConsole("[Cliente #" + clientId + "] JSON Crudo: " + receivedData);

                // ---------------------JACKSON MAPPING--------------------
                try {
                    // Leemos el String JSON y lo convertimos a un árbol de nodos
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(receivedData);

                    // Verificamos qué tipo de petición es
                    if (rootNode.has("type")) {
                        String tipo = rootNode.get("type").asText();

                        if (tipo.equals("LOGIN")) {
                            // Extraemos el email y el password
                            String emailReq = rootNode.get("email").asText();
                            String passReq = rootNode.get("password").asText();

                            server.writeConsole("[Cliente #" + clientId + "] LOGIN REQUEST: Email: " + emailReq + " Password: " + passReq.toString());
                            
                            UserDAO userDAO
                                    

                            // TODO: Aquí llamarías a tu Base de Datos para validar el emailReq y passReq
                            // Si es correcto, enviarías un JSON de vuelta al cliente aceptando el acceso.
                        }
                    }
                } catch (Exception jsonEx) {
                    server.writeConsole("Advertencia: El mensaje recibido no es un JSON válido o está incompleto.");
                }

                /// Limpia string builder
                sb.setLength(0);
            }

        } catch (IOException e) {
            System.getLogger(ClientThread.class.getName()).log(
                    System.Logger.Level.ERROR, "Connection error or client disconnected", e);

            server.removeClient(clientId);
        } finally {
            server.removeClient(clientId);
        }

    }

}
