package controller;

import model.dbrequest.SignUpRequest;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica del registro de usuario. Envía la solicitud SIGNUP y procesa las
 * respuestas del servidor (SIGNUP_SUCCESS / SIGNUP_ERROR).
 *
 * @author adotal
 */
public class SignUpController {

    public interface View {
        void onSignUpSuccess();
        void onSignUpError(String message);
    }

    private final View view;

    public SignUpController(View view) {
        this.view = view;
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();
        dispatcher.register("SIGNUP_SUCCESS", root -> view.onSignUpSuccess());
        dispatcher.register("SIGNUP_ERROR", root -> {
            String msg = root.has("message") ? root.get("message").asText() : "Correo ya utilizado";
            view.onSignUpError(msg);
        });
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    public void signUp(String name, String email, String password) {
        try {
            SignUpRequest request = new SignUpRequest(name, email, password);
            String json = Json.mapper().writeValueAsString(request);
            ClientSocket.getInstance().sendText(json);
        } catch (Exception ex) {
            view.onSignUpError("Error al enviar solicitud: " + ex.getMessage());
        }
    }
}
