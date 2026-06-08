package controller;

import model.dbrequest.RecoverPasswordRequest;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica de recuperación de contraseña. Envía RECOVER_PASSWORD y procesa las
 * respuestas del servidor.
 *
 * Nota: el servidor emite el tipo de error con el typo "RECOVER_PASSWROD_ERROR";
 * se respeta tal cual para mantener compatibilidad con el protocolo actual.
 *
 * @author adotal
 */
public class RecoverPasswordController {

    public interface View {
        void onRecoverSuccess();
        void onRecoverError(String message);
    }

    private final View view;
    private final String email;

    public RecoverPasswordController(View view, String email) {
        this.view = view;
        this.email = email;
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();
        dispatcher.register("RECOVER_PASSWORD_SUCCESS", root -> view.onRecoverSuccess());
        dispatcher.register("RECOVER_PASSWROD_ERROR", root -> {
            String msg = root.has("message") ? root.get("message").asText() : "Error desconocido";
            view.onRecoverError(msg);
        });
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    public void recover(String password) {
        try {
            RecoverPasswordRequest request = new RecoverPasswordRequest(email, password);
            String json = Json.mapper().writeValueAsString(request);
            ClientSocket.getInstance().sendText(json);
        } catch (Exception ex) {
            view.onRecoverError("Error al enviar solicitud: " + ex.getMessage());
        }
    }
}
