package controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import model.Group;
import model.SessionManager;
import model.User;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica de la ventana de lista (pestañas Todos, Amigos, Grupos): solicita
 * usuarios, amigos y grupos; envía solicitudes de amistad; gestiona el cierre
 * de sesión.
 */
public class UsersListController {

    public interface View {
        void onUsersLoaded(ArrayList<User> users);
        void onFriendsLoaded(ArrayList<User> friends);
        void onGroupsLoaded(ArrayList<Group> groups);
        void onLogoutSuccess();
        void onFriendRequestResult(boolean ok, String message);
        /** Llega una nueva solicitud de amistad entrante (para avisar al usuario). */
        void onNewFriendRequest();
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
                    root.get("users"), new TypeReference<ArrayList<User>>() {});
            view.onUsersLoaded(users);
        });

        dispatcher.register("FRIENDS_LIST", root -> {
            if (!root.has("friends")) {
                return;
            }
            ArrayList<User> friends = Json.mapper().convertValue(
                    root.get("friends"), new TypeReference<ArrayList<User>>() {});
            view.onFriendsLoaded(friends);
        });

        dispatcher.register("GROUPS_LIST", root -> {
            ArrayList<Group> groups = new ArrayList<>();
            if (root.has("groups")) {
                for (com.fasterxml.jackson.databind.JsonNode g : root.get("groups")) {
                    groups.add(new Group(g.get("id").asInt(), g.get("title").asText()));
                }
            }
            view.onGroupsLoaded(groups);
        });

        // El servidor avisa que la amistad cambió: refrescar amigos y usuarios.
        dispatcher.register("FRIENDS_LIST_UPDATED", root -> {
            fetchFriends();
            fetchAllUsers();
        });

        // Cambios de grupos: refrescar lista de grupos.
        dispatcher.register("GROUPS_LIST_UPDATED", root -> fetchGroups());
        dispatcher.register("GROUP_DELETED", root -> fetchGroups());

        dispatcher.register("FRIEND_REQUEST_SENT", root ->
                view.onFriendRequestResult(true,
                        root.has("message") ? root.get("message").asText() : "Solicitud enviada."));

        dispatcher.register("FRIEND_REQUEST_ERROR", root ->
                view.onFriendRequestResult(false,
                        root.has("message") ? root.get("message").asText() : "No se pudo enviar la solicitud."));

        dispatcher.register("NEW_FRIEND_REQUEST", root -> view.onNewFriendRequest());

        dispatcher.register("LOGOUT_SUCCESS", root -> {
            SessionManager.getInstance().logout();
            view.onLogoutSuccess();
        });
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    private int currentUserId() {
        return SessionManager.getInstance().getCurrentUser().getIdUser();
    }

    public void fetchAllUsers() {
        send("FETCH_ALL_USERS", req ->
                req.put("email", SessionManager.getInstance().getCurrentUser().getEmail()));
    }

    public void fetchFriends() {
        send("FETCH_FRIENDS", req -> req.put("userId", currentUserId()));
    }

    public void fetchGroups() {
        send("FETCH_GROUPS", req -> req.put("userId", currentUserId()));
    }

    public void sendFriendRequest(String receiverEmail) {
        send("SEND_FRIEND_REQUEST", req -> {
            req.put("senderId", currentUserId());
            req.put("receiverEmail", receiverEmail);
        });
    }

    public void logout() {
        send("LOGOUT_REQUEST", req ->
                req.put("email", SessionManager.getInstance().getCurrentUser().getEmail()));
    }

    private void send(String type, java.util.function.Consumer<ObjectNode> filler) {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", type);
            filler.accept(request);
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[UsersListController] Error al enviar " + type + ": " + ex.getMessage());
        }
    }
}
