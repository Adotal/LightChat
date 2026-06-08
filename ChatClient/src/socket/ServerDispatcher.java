package socket;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import util.Json;

/**
 * Enruta cada mensaje crudo del servidor al controller que registró ese
 * "type". Reemplaza el antiguo listener único de {@link ClientSocket}, que se
 * sobrescribía cada vez que una vista se registraba.
 *
 * Aquí vive el ÚNICO acoplamiento a Swing del lado de red: el marshaling al
 * Event Dispatch Thread se hace centralizadamente con
 * {@link SwingUtilities#invokeLater}, de modo que {@code ClientSocket} queda
 * libre de dependencias de UI.
 */
public class ServerDispatcher {

    private static ServerDispatcher instance;

    // type del mensaje -> handlers que lo procesan (en el EDT)
    private final Map<String, List<Consumer<JsonNode>>> handlers = new ConcurrentHashMap<>();

    // Listener de estado de conexión ("Conectando...", "Conectado", etc.)
    private volatile Consumer<String> statusListener;

    private ServerDispatcher() {
    }

    public static synchronized ServerDispatcher getInstance() {
        if (instance == null) {
            instance = new ServerDispatcher();
        }
        return instance;
    }

    /**
     * Registra un handler para un tipo de mensaje del servidor. Pueden coexistir
     * varios handlers para el mismo tipo (p. ej. varios chats abiertos); cada uno
     * debe filtrar internamente los mensajes que le competen.
     */
    public void register(String type, Consumer<JsonNode> handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /** Elimina un handler concreto previamente registrado para ese tipo. */
    public void unregister(String type, Consumer<JsonNode> handler) {
        List<Consumer<JsonNode>> list = handlers.get(type);
        if (list != null) {
            list.remove(handler);
        }
    }

    /** Elimina todos los handlers de un tipo. */
    public void unregister(String type) {
        handlers.remove(type);
    }

    public void setStatusListener(Consumer<String> listener) {
        this.statusListener = listener;
    }

    /**
     * Invocado desde el hilo de red de {@link ClientSocket}. Parsea el tipo y
     * despacha al handler correspondiente dentro del EDT.
     */
    public void dispatch(String rawJson) {
        try {
            JsonNode root = Json.mapper().readTree(rawJson);
            if (!root.has("type")) {
                return;
            }
            String type = root.get("type").asText();
            List<Consumer<JsonNode>> typeHandlers = handlers.get(type);
            if (typeHandlers == null || typeHandlers.isEmpty()) {
                System.out.println("[ServerDispatcher] Sin handler para type=" + type);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                for (Consumer<JsonNode> handler : typeHandlers) {
                    handler.accept(root);
                }
            });
        } catch (Exception e) {
            System.err.println("[ServerDispatcher] Error procesando mensaje: " + e.getMessage());
        }
    }

    /**
     * Invocado desde el hilo de red para reportar cambios de estado de
     * conexión, marshaleados al EDT.
     */
    public void dispatchStatus(String message) {
        Consumer<String> listener = statusListener;
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.accept(message));
        }
    }
}
