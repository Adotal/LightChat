package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author adotal
 */
public class JavaServer {

    private static final int PORT = 1235;    

    // Para que hilos se guarden con identificador incremental
    private final AtomicInteger idCount = new AtomicInteger(0);

    // Mapa de clientes
    private final Map<Integer, ClientThread> activeClients
            = new ConcurrentHashMap<>();

    public JavaServer() {
    }

    public void beginServer() {

        new Thread(() -> {

            try {
                ServerSocket ss;
                ss = new ServerSocket(1235);
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
    }
    
    public void removeClient(int idClient){
        activeClients.remove(idClient);
        writeConsole("Cliente #" + idClient + " se ha desconectado");
                
    }

}
