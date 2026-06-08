package controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import model.SessionManager;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica de la vista de notificaciones: solicitudes de amistad (recibidas y
 * enviadas) e invitaciones de grupo recibidas. Permite aceptar/rechazar.
 *
 * @author adotal
 */
public class FriendsRequestController {

    /** Item simple de solicitud de amistad para la vista. */
    public static class FriendReqItem {
        public final int id;
        public final String name;
        public final String email;
        public final String status; // PENDING/APPROVED/DENIED (solo en enviadas)

        public FriendReqItem(int id, String name, String email, String status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.status = status;
        }
    }

    /** Item simple de invitación de grupo para la vista. */
    public static class GroupInvItem {
        public final int id;
        public final String groupTitle;
        public final String ownerName;

        public GroupInvItem(int id, String groupTitle, String ownerName) {
            this.id = id;
            this.groupTitle = groupTitle;
            this.ownerName = ownerName;
        }
    }

    public interface View {
        void onFriendRequests(List<FriendReqItem> received, List<FriendReqItem> sent);
        void onGroupInvitations(List<GroupInvItem> invitations);
    }

    private final View view;

    public FriendsRequestController(View view) {
        this.view = view;
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();

        dispatcher.register("FRIEND_REQUESTS_LIST", root -> {
            List<FriendReqItem> received = new ArrayList<>();
            if (root.has("received")) {
                for (JsonNode n : root.get("received")) {
                    JsonNode s = n.get("sender");
                    received.add(new FriendReqItem(n.get("id").asInt(),
                            text(s, "name"), text(s, "email"), "PENDING"));
                }
            }
            List<FriendReqItem> sent = new ArrayList<>();
            if (root.has("sent")) {
                for (JsonNode n : root.get("sent")) {
                    JsonNode t = n.get("target");
                    sent.add(new FriendReqItem(n.get("id").asInt(),
                            text(t, "name"), text(t, "email"),
                            n.has("status") ? n.get("status").asText() : "PENDING"));
                }
            }
            view.onFriendRequests(received, sent);
        });

        dispatcher.register("GROUP_INVITATIONS_LIST", root -> {
            List<GroupInvItem> invs = new ArrayList<>();
            if (root.has("invitations")) {
                for (JsonNode n : root.get("invitations")) {
                    String title = n.has("group") ? text(n.get("group"), "title") : "";
                    String owner = n.has("owner") ? text(n.get("owner"), "name") : "";
                    invs.add(new GroupInvItem(n.get("id").asInt(), title, owner));
                }
            }
            view.onGroupInvitations(invs);
        });

        // Tras responder, refrescar ambas listas.
        dispatcher.register("FRIEND_REQUEST_RESPONDED", root -> fetchAll());
        dispatcher.register("GROUP_INVITATION_RESPONDED", root -> fetchAll());
    }

    private String text(JsonNode node, String field) {
        return (node != null && node.has(field) && !node.get(field).isNull())
                ? node.get(field).asText() : "";
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    private int currentUserId() {
        return SessionManager.getInstance().getCurrentUser().getIdUser();
    }

    public void fetchAll() {
        send("FETCH_FRIEND_REQUESTS", req -> req.put("userId", currentUserId()));
        send("FETCH_GROUP_INVITATIONS", req -> req.put("userId", currentUserId()));
    }

    public void respondFriend(int idFriendship, boolean accept) {
        send("RESPOND_FRIEND_REQUEST", req -> {
            req.put("idFriendship", idFriendship);
            req.put("accept", accept);
        });
    }

    public void respondGroup(int idInvitation, boolean accept) {
        send("RESPOND_GROUP_INVITATION", req -> {
            req.put("idInvitation", idInvitation);
            req.put("accept", accept);
        });
    }

    private void send(String type, java.util.function.Consumer<ObjectNode> filler) {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", type);
            filler.accept(request);
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[FriendsRequestController] Error al enviar " + type + ": " + ex.getMessage());
        }
    }
}
