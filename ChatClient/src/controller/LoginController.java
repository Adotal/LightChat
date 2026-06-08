package controller;

import model.SessionManager;
import model.User;
import model.dbrequest.LoginRequest;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica del inicio de sesión. Arma la solicitud, la envía por el socket y
 * procesa las respuestas del servidor (LOGIN_SUCCESS / LOGIN_ERROR),
 * notificando a la vista mediante la interfaz {@link View}.
 *
 * @author adotal
 */
public class LoginController {

    /**
     * Contrato que la vista implementa para recibir resultados. La vista solo
     * hace UI/navegación; nada de red ni de parsing JSON.
     */
    public interface View {
        void onStatus(String message);
        void onLoginSuccess();
        void onLoginError(String message);
        void onTooManyAttempts();
    }

    private final View view;
    private int errorCount = 0;

    public LoginController(View view) {
        this.view = view;
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();
        dispatcher.setStatusListener(view::onStatus);

        dispatcher.register("LOGIN_SUCCESS", root -> {
            if (root.has("user")) {
                try {
                    User user = Json.mapper().treeToValue(root.get("user"), User.class);
                    SessionManager.getInstance().setCurrentUser(user);
                } catch (Exception ex) {
                    System.err.println("[LoginController] Error al parsear user: " + ex.getMessage());
                }
            }
            errorCount = 0;
            view.onLoginSuccess();
        });

        dispatcher.register("LOGIN_ERROR", root -> {
            String msg = root.has("message") ? root.get("message").asText() : "Credenciales inválidas";
            view.onLoginError(msg);
            if (++errorCount == 3) {
                errorCount = 0;
                view.onTooManyAttempts();
            }
        });
    }

    /** Abre la conexión con el servidor (reintenta en segundo plano). */
    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    public void login(String email, String password) {
        try {
            LoginRequest request = new LoginRequest(email, password);
            String json = Json.mapper().writeValueAsString(request);
            ClientSocket.getInstance().sendText(json);
        } catch (Exception ex) {
            view.onLoginError("Error al enviar solicitud: " + ex.getMessage());
        }
    }
}
