package controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import model.Group;
import model.SessionManager;
import model.User;
import socket.ClientSocket;
import socket.ServerDispatcher;
import util.Json;

/**
 * Lógica del chat de grupo: envía y recibe mensajes de grupo, carga el
 * historial y reacciona a la eliminación del grupo.
 *
 * @author adotal
 */
public class GroupsController {

    /** Mensaje de grupo simple para la vista. */
    public static class GMsg {
        public final String senderName;
        public final String text;
        public final boolean mine;

        public GMsg(String senderName, String text, boolean mine) {
            this.senderName = senderName;
            this.text = text;
            this.mine = mine;
        }
    }

    /** Integrante de grupo (invitado) con su estado, para la vista de detalle. */
    public static class GMember {
        public final String name;
        public final String status; // PENDING/APPROVED/DENIED

        public GMember(String name, String status) {
            this.name = name;
            this.status = status;
        }
    }

    public interface View {
        void onGroupMessage(GMsg msg);
        void onGroupHistory(List<GMsg> messages);
        void onGroupDeleted();
        void onGroupMembers(List<GMember> members, boolean isOwner);
        void onGroupLeft();
    }

    private final View view;
    private final Group group;
    private final User currentUser;

    public GroupsController(View view, Group group) {
        this.view = view;
        this.group = group;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        registerHandlers();
    }

    private void registerHandlers() {
        ServerDispatcher dispatcher = ServerDispatcher.getInstance();

        dispatcher.register("SEND_MESSAGE", root -> {
            if (!root.has("chat_type") || !"GROUP".equals(root.get("chat_type").asText())) {
                return;
            }
            if (!root.has("group_id") || root.get("group_id").asInt() != group.getId()) {
                return;
            }
            JsonNode sender = root.get("sender");
            int idSender = sender.get("idUser").asInt();
            String name = sender.has("name") ? sender.get("name").asText() : "";
            String text = root.get("text").asText();
            view.onGroupMessage(new GMsg(name, text, idSender == currentUser.getIdUser()));
        });

        dispatcher.register("GROUP_HISTORY", root -> {
            if (!root.has("group_id") || root.get("group_id").asInt() != group.getId()) {
                return;
            }
            List<GMsg> history = new ArrayList<>();
            if (root.has("messages")) {
                for (JsonNode mn : root.get("messages")) {
                    int idSender = mn.get("idSender").asInt();
                    String name = mn.has("senderName") ? mn.get("senderName").asText() : "";
                    String content = mn.get("content").asText();
                    history.add(new GMsg(name, content, idSender == currentUser.getIdUser()));
                }
            }
            view.onGroupHistory(history);
        });

        dispatcher.register("GROUP_DELETED", root -> {
            if (root.has("group_id") && root.get("group_id").asInt() == group.getId()) {
                view.onGroupDeleted();
            }
        });

        dispatcher.register("GROUP_MEMBERS", root -> {
            if (!root.has("group_id") || root.get("group_id").asInt() != group.getId()) {
                return;
            }
            List<GMember> members = new ArrayList<>();
            if (root.has("invitations")) {
                for (JsonNode n : root.get("invitations")) {
                    String name = n.has("invited") && n.get("invited").has("name")
                            ? n.get("invited").get("name").asText() : "";
                    String status = n.has("status") ? n.get("status").asText() : "PENDING";
                    members.add(new GMember(name, status));
                }
            }
            boolean isOwner = root.has("owner_id")
                    && root.get("owner_id").asInt() == currentUser.getIdUser();
            view.onGroupMembers(members, isOwner);
        });

        dispatcher.register("GROUP_LEFT_OK", root -> {
            if (root.has("group_id") && root.get("group_id").asInt() == group.getId()) {
                view.onGroupLeft();
            }
        });
    }

    public void fetchMembers() {
        send("FETCH_GROUP_MEMBERS", req -> req.put("groupId", group.getId()));
    }

    public void leaveGroup() {
        send("LEAVE_GROUP", req -> {
            req.put("groupId", group.getId());
            req.put("userId", currentUser.getIdUser());
        });
    }

    public void deleteGroup() {
        send("DELETE_GROUP", req -> {
            req.put("groupId", group.getId());
            req.put("userId", currentUser.getIdUser());
        });
    }

    public void connect() {
        ClientSocket.getInstance().tryConnect();
    }

    public void requestHistory() {
        send("FETCH_GROUP_HISTORY", req -> req.put("groupId", group.getId()));
    }

    public void sendGroupMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        try {
            ObjectNode req = Json.mapper().createObjectNode();
            req.put("type", "SEND_MESSAGE");
            req.put("chat_type", "GROUP");
            req.put("group_id", group.getId());
            ObjectNode sender = Json.mapper().createObjectNode();
            sender.put("idUser", currentUser.getIdUser());
            sender.put("name", currentUser.getName());
            sender.put("email", currentUser.getEmail());
            req.set("sender", sender);
            req.put("text", text);
            req.put("sendedAt", java.time.LocalDateTime.now().toString());
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(req));
        } catch (Exception ex) {
            System.err.println("[GroupsController] Error al enviar mensaje de grupo: " + ex.getMessage());
        }
    }

    private void send(String type, java.util.function.Consumer<ObjectNode> filler) {
        try {
            ObjectNode request = Json.mapper().createObjectNode();
            request.put("type", type);
            filler.accept(request);
            ClientSocket.getInstance().sendText(Json.mapper().writeValueAsString(request));
        } catch (Exception ex) {
            System.err.println("[GroupsController] Error al enviar " + type + ": " + ex.getMessage());
        }
    }
}
