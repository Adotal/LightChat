package view;

import controller.ChatController;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import model.Message;
import model.User;

/**
 * @author alond
 */
public class ChatView extends BaseChatView implements ChatController.View {

    private JLabel lblStatusCircle;
    private final ChatController controller;
    String chatType;

    public ChatView() {
        // Distinct mock data to easily differentiate profiles during preview mode
        this(new User(2, "Anna Clara", "annabanana@email.com", true), "TODOS");
    }

    public ChatView(User receiverUser, String chatType) {
        super();
        this.chatType = chatType;
        controller = new ChatController(this, receiverUser, chatType);
        initComponents();
        actualizarEstadoUsuario();
        controller.connect();
        controller.requestHistory();
    }

    @Override
    public void dispose() {
        controller.dispose();
        super.dispose();
    }

    @Override
    protected JComponent getHeaderLeadingIndicator() {
        lblStatusCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                User receiverUser = controller.getReceiverUser();
                if (receiverUser != null && receiverUser.getIsConnected()) {
                    g2.setColor(new Color(37, 68, 196));
                } else {
                    g2.setColor(new Color(139, 162, 179));
                }
                g2.fill(new Ellipse2D.Double(0, 0, 24, 24));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        return lblStatusCircle;
    }

    public void actualizarEstadoUsuario() {
        User receiverUser = controller.getReceiverUser();
        if (receiverUser != null) {
            setHeaderTitle(receiverUser.getName());
            setTitle("LightChat - " + receiverUser.getName()); // Dynamically updates frame title
            if (lblStatusCircle != null) {
                lblStatusCircle.repaint();
            }
        }
    }

    @Override
    protected void onEnviar(String texto) {
        Message mensajeMio = controller.sendMessage(texto);
        agregarBurbujaMensaje(mensajeMio.getText(), true, null, null);
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ChatView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(() -> {
            new ChatView().setVisible(true);
        });
    }

    @Override
    public void onMessageReceived(String textContent) {
        SwingUtilities.invokeLater(() -> agregarBurbujaMensaje(textContent, false, null, null));
    }

    @Override
    public void onDeleteChat() {
        // El otro usuario cerró sesión/se desconectó: el chat efímero (Todos)
        // desaparece. Avisar y cerrar la ventana.
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "La conversación finalizó porque el otro usuario se desconectó.");
            dispose();
        });
    }

    @Override
    public void onHistoryLoaded(java.util.List<Message> messages) {
        SwingUtilities.invokeLater(() -> {
            for (Message m : messages) {
                agregarBurbujaMensaje(m.getText(), controller.isMine(m), null, null);
            }
        });
    }
}
