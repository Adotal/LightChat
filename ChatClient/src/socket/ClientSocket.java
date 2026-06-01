package socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;


/**
 *
 * @author adotal
 */

/*
    This Singleton mantains the connection with Server through websocket
    And Safely update UI with a Consumer
*/
public class ClientSocket {

    //  The single, static instance of the class
    private static ClientSocket instance;

    private final String host = "127.0.0.1";
    private final int port = 1235;
    private Socket clientSocket;
    
    // The active listener for UI updates
    private Consumer<String> onStatusUpdate;

    // PRIVATE constructor prevents from using 'new ClientSocket()'
    private ClientSocket() {
    }

    // Synchronized method to get the single instance
    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    // Allow the active JFrame to set itself as the receiver of updates
    public void setStatusListener(Consumer<String> listener) {
        this.onStatusUpdate = listener;
    }

    public void tryConnect() {
        // Prevent starting a new connection thread if already connected
        if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed()) {
            updateStatus("Ya estás conectado.");
            return;
        }

        new Thread(() -> {
            int maxRetries = 10;
            int delayMillis = 2000;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    attempt++;
                    clientSocket = new Socket(host, port);
                    updateStatus("Conectado");
                    return; 
                } catch (IOException ex) {
                    updateStatus("Intento " + attempt + " fallido. Reintentando...");
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            updateStatus("Fallo de conexión tras " + maxRetries + " intentos.");
        }).start();
    }

    public void sendText(String s) {
        if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed()) {
            try {
                OutputStream out = clientSocket.getOutputStream();
                out.write(s.getBytes(StandardCharsets.UTF_8));
                out.flush(); 
                System.out.println("Enviado: " + s);
            } catch (IOException ex) {
                System.err.println("Error al enviar: " + ex.getMessage());
            }
        }
    }
    
    private void updateStatus(String message) {
        if (onStatusUpdate != null) {
            SwingUtilities.invokeLater(() -> onStatusUpdate.accept(message));
        }
    }
}