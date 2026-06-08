package controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import model.SessionManager;
import model.User;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica de la creación de grupos: carga los usuarios registrados a invitar y
 * envía la petición CREATE_GROUP.
 *
 * @author adotal
 */
public class NewGroupController {

    public interface View {
        void onUsersForGroup(ArrayList<User> users);
        void onGroupCreated();
        void onError(String message);
    }

    private final View view;

    public NewGroupController(View view) {
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
                    root.get("users"), new TypeReference<ArrayList<User>>() {});
            view.onUsersForGroup(users);
        });

        dispatcher.register("GROUP_CREATED", root -> view.onGroupCreated());
        dispatcher.register("GROUP_ERROR", root ->
                view.onError(root.has("message") ? root.get("message").asText() : "Error en la operación de grupo."));
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    public void fetchUsers() {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", "FETCH_ALL_USERS");
            request.put("email", SessionManager.getInstance().getCurrentUser().getEmail());
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[NewGroupController] Error al pedir usuarios: " + ex.getMessage());
        }
    }

    public void createGroup(String title, List<String> invitedEmails) {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", "CREATE_GROUP");
            request.put("ownerId", SessionManager.getInstance().getCurrentUser().getIdUser());
            request.put("title", title);
            ArrayNode arr = request.putArray("invitedEmails");
            for (String email : invitedEmails) {
                arr.add(email);
            }
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[NewGroupController] Error al crear grupo: " + ex.getMessage());
        }
    }
}
