package server;

import dao.ServerEventDAO;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 * @author adotal
 */
public class JavaServer {

    private static final int PORT = 1235;

    private static final DateTimeFormatter TS_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Para que hilos se guarden con identificador incremental
    private final AtomicInteger idCount = new AtomicInteger(0);

    // Mapa de clientes
    private final Map<Integer, ClientThread> activeClients
            = new ConcurrentHashMap<>();

    // Listeners de eventos (p. ej. la vista de administrador). Thread-safe.
    private final List<Consumer<String>> eventListeners = new CopyOnWriteArrayList<>();

    public JavaServer() {
    }

    /**
     * Registra un listener (vista admin) que recibe cada línea de evento
     * formateada y legible en cuanto ocurre (RQNF91/93).
     */
    public void addEventListener(Consumer<String> listener) {
        eventListeners.add(listener);
    }

    /**
     * Registra un evento generado por usuarios o por el servidor (RQNF90).
     * Notifica a los listeners de inmediato (≤1s, RQNF15/93) con marca
     * temporal legible (RQNF94) y persiste en BD de forma asíncrona para no
     * bloquear el hilo del cliente.
     */
    public void logEvent(String type, String description) {
        String timestamp = LocalDateTime.now().format(TS_FORMAT);
        String line = "[" + timestamp + "] " + type + " - " + description;

        // Consola
        System.out.println(line);

        // Notificación inmediata a la vista admin
        for (Consumer<String> listener : eventListeners) {
            try {
                listener.accept(line);
            } catch (Exception ex) {
                System.out.println("[SERVER] Error notificando listener de evento: " + ex.getMessage());
            }
        }

        // Persistencia asíncrona
        new Thread(() -> {
            try {
                new ServerEventDAO().insertEvent(type, description);
            } catch (Exception ex) {
                System.out.println("[SERVER] Error persistiendo evento: " + ex.getMessage());
            }
        }).start();
    }

    public void beginServer() {

        new Thread(() -> {

            try (ServerSocket ss = new ServerSocket(PORT)) {
                writeConsole("Servidor iniciado en puerto " + PORT);

                while (!ss.isClosed()) {

                    // Se mantiene aquí hasta recibir cliente
                    Socket clientSocket = ss.accept();

                    // Nuevo id incrmental cliente 
                    int newId = idCount.incrementAndGet();

                    // Nuevo hilo de cliente
                    ClientThread newClient = new ClientThread(clientSocket, newId, this);

                    // Registra nuevo cliente
                    activeClients.put(newId, newClient);

                    // Inicia hilo
                    Thread newThread = new Thread(newClient);
                    newThread.start();

                }
            } catch (Exception ex) {

                writeConsole("Error en servidor: " + ex.getMessage());

                System.getLogger(ServerSocket.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        }).start();

    }

    public synchronized void writeConsole(String s) {
        System.out.println(s);
        // Reflejar también en la vista de administrador (sin persistir).
        for (Consumer<String> listener : eventListeners) {
            try {
                listener.accept(s);
            } catch (Exception ex) {
                System.out.println("[SERVER] Error notificando listener de consola: " + ex.getMessage());
            }
        }
    }

    public void removeClient(int idClient) {
        activeClients.remove(idClient);
        writeConsole("[Cliente #" + idClient + "] DISCONNECTED");

    }

    // Central broadcaster method
    public void broadcastUserStatus() {
        writeConsole("[SERVER] Broadcasting real-time status updates to all active sessions...");
        for (ClientThread client : activeClients.values()) {
            // Triggers each thread to evaluate its user custom array list and execute .sendMessage()
            client.sendUserListUpdate();
        }
    }
    
    public ClientThread findClientByUserEmail(String email) {
        for (ClientThread client : activeClients.values()) {
            // Asumiendo que guardas el objeto 'User' logueado dentro de cada ClientThread
            if (client.getEmail()!= null && client.getEmail().equals(email)) {
                return client;
            }
        }
        return null; // El usuario está offline
    }

}
