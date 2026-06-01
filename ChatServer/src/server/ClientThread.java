package server;

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
        server.writeConsole("Cliente #" + clientId + " conectado desde " + ip);

        // Escucha entradas y las imprime
        StringBuilder sb = new StringBuilder();

        try {

            byte[] byteArray;
            byteArray = new byte[100];
            int bytesRead; // Guarda la cantidad real de bytes leídos

            // Guardamos el resultado del read en bytesRead
            while ((bytesRead = client.getInputStream().read(byteArray)) != -1) {

                // Corregido: Convertimos solo los bytes reales que llegaron (evita basura)
                sb.append(new String(byteArray, 0, bytesRead));

                server.writeConsole("[Cliente #" + clientId + "]: " + sb.toString());

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
