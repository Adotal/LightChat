package controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import model.SessionManager;
import model.User;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica de la lista de contactos: solicita la lista de usuarios y gestiona el
 * cierre de sesión. Procesa UPDATE_USERS_LIST y LOGOUT_SUCCESS.
 */
public class UsersListController {

    public interface View {
        void onUsersLoaded(ArrayList<User> users);
        void onLogoutSuccess();
    }

    private final View view;

    public UsersListController(View view) {
        this.view = view;
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();

        dispatcher.register("UPDATE_USERS_LIST", root -> {
            if (!root.has("users")) {
                return;
            }
            ArrayList<User> users = Json.mapper().convertValue(
                    root.get("users"),
                    new TypeReference<ArrayList<User>>() {
            });
            System.out.println("Successfully loaded " + users.size() + " users.");
            view.onUsersLoaded(users);
        });

        dispatcher.register("LOGOUT_SUCCESS", root -> {
            SessionManager.getInstance().logout();
            view.onLogoutSuccess();
        });
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    public void fetchAllUsers() {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", "FETCH_ALL_USERS");
            request.put("email", SessionManager.getInstance().getCurrentUser().getEmail());
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[UsersListController] Error al solicitar usuarios: " + ex.getMessage());
        }
    }

    public void logout() {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", "LOGOUT_REQUEST");
            request.put("email", SessionManager.getInstance().getCurrentUser().getEmail());
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[UsersListController] Error al cerrar sesión: " + ex.getMessage());
        }
    }
}
