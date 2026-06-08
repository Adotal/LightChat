package view;

import controller.GroupsController;
import model.Group;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Vista del chat de grupo. Reutiliza la UI base compartida con el chat directo
 * ({@link BaseChatView}), adaptándola para mostrar el nombre del autor y un
 * color por participante, además del menú de opciones del grupo.
 */
public class GroupChatView extends BaseChatView implements GroupsController.View {

    private final Group grupoActual;
    private final GroupsController controller;

    // Paleta para distinguir visualmente a los usuarios (color por autor).
    private final Color[] PALETA_COLORES = {
        new Color(82, 113, 255),  // AZUL ELÁSTICO
        new Color(141, 75, 255),  // MORADO VIBRANTE
        new Color(0, 185, 206)    // CIAN OSCURO
    };

    public GroupChatView(Group grupo) {
        super();
        this.grupoActual = grupo;
        controller = new GroupsController(this, grupoActual);

        initComponents();
        setHeaderTitle(grupoActual.getTitle());
        setTitle("Chat - " + grupoActual.getTitle());

        controller.connect();
        controller.requestHistory();
    }

    @Override
    public void dispose() {
        controller.dispose();
        super.dispose();
    }

    @Override
    protected JComponent getHeaderTrailing() {
        JButton btnOpciones = new JButton("•••");
        btnOpciones.setBackground(FONDO_OSCURO_PRINCIPAL);
        btnOpciones.setForeground(Color.WHITE);
        btnOpciones.setFocusPainted(false);
        btnOpciones.setContentAreaFilled(false);
        btnOpciones.setBorder(null);
        btnOpciones.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        btnOpciones.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOpciones.addActionListener(e -> mostrarMenuOpciones(btnOpciones));
        return btnOpciones;
    }

    @Override
    protected void onEnviar(String texto) {
        controller.sendGroupMessage(texto);
        agregarBurbujaMensaje(texto, true, null, PALETA_COLORES[0]);
    }

    private Color colorPara(String autor) {
        return PALETA_COLORES[Math.abs(autor.hashCode()) % PALETA_COLORES.length];
    }

    // ===== Callbacks de GroupsController.View =====

    @Override
    public void onGroupMessage(GroupsController.GMsg msg) {
        SwingUtilities.invokeLater(() -> agregarBurbujaMensaje(
                msg.text, msg.mine, msg.mine ? null : msg.senderName,
                msg.mine ? PALETA_COLORES[0] : colorPara(msg.senderName)));
    }

    @Override
    public void onGroupHistory(List<GroupsController.GMsg> messages) {
        SwingUtilities.invokeLater(() -> {
            for (GroupsController.GMsg m : messages) {
                agregarBurbujaMensaje(m.text, m.mine, m.mine ? null : m.senderName,
                        m.mine ? PALETA_COLORES[0] : colorPara(m.senderName));
            }
        });
    }

    @Override
    public void onGroupDeleted() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "El grupo ha sido eliminado.");
            volverALista();
        });
    }

    @Override
    public void onGroupLeft() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Has abandonado el grupo.");
            volverALista();
        });
    }

    @Override
    public void onGroupMembers(List<GroupsController.GMember> members, boolean isOwner) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("Integrantes de " + grupoActual.getTitle() + ":\n\n");
            sb.append("• Creador (tú)").append(isOwner ? "\n" : "");
            for (GroupsController.GMember m : members) {
                sb.append("• ").append(m.name).append(" - ").append(traducirEstado(m.status)).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Integrantes", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private String traducirEstado(String status) {
        switch (status) {
            case "APPROVED": return "Aceptado";
            case "DENIED":   return "Rechazado";
            default:          return "Pendiente";
        }
    }

    /** Menú de opciones del grupo: ver integrantes, abandonar o eliminar. */
    private void mostrarMenuOpciones(Component invoker) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem verIntegrantes = new JMenuItem("Ver integrantes");
        verIntegrantes.addActionListener(e -> controller.fetchMembers());
        menu.add(verIntegrantes);

        JMenuItem abandonar = new JMenuItem("Abandonar grupo");
        abandonar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Abandonar este grupo?",
                    "Abandonar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                controller.leaveGroup();
            }
        });
        menu.add(abandonar);

        JMenuItem eliminar = new JMenuItem("Eliminar grupo (creador)");
        eliminar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Eliminar el grupo? Esta acción es permanente.",
                    "Eliminar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                controller.deleteGroup();
            }
        });
        menu.add(eliminar);

        menu.show(invoker, 0, invoker.getHeight());
    }
}
