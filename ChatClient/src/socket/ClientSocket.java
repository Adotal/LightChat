package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
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

    // The active listener to process incoming JSON messages from the server
    private Consumer<String> onMessageReceived;

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

    // Allow the active JFrame to intercept JSON responses from the server
    public void setMessageListener(Consumer<String> listener) {
        this.onMessageReceived = listener;
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
                    // Se mantiene a la escucha
                    listen();
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

    /**
     * Se ejecuta indefinidamente en segundo plano en el hilo secundario. Lee
     * las respuestas del servidor que finalizan con un salto de línea (\n).
     */
    private void listen() {
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {

            String receivedLine;
            // Se bloquea de forma eficiente hasta que llega una línea completa
            while ((receivedLine = reader.readLine()) != null) {
                System.out.println("\n[Socket] Recibido desde Servidor: " + receivedLine);

                // Forward the raw JSON string straight to the active View 
                // using SwingUtilities to prevent multi-threading crashes.
                if (onMessageReceived != null) {
                    final String rawJson = receivedLine;
                    SwingUtilities.invokeLater(() -> onMessageReceived.accept(rawJson));
                }

            }
        } catch (IOException e) {
            System.err.println("\nConexión perdida con el servidor: " + e.getMessage());
        } finally {
            closeSocket();
            updateStatus("Desconectado");
        }
    }

    public void sendText(String s) {
        if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed()) {
            try {
                OutputStream out = clientSocket.getOutputStream();

                // "\n" para indicar el fin del JSON
                String mensajeConSalto = s + "\n";

                out.write(mensajeConSalto.getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.out.print("Enviado: " + s);
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

    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket de manera segura: " + e.getMessage());
        }
    }
}
