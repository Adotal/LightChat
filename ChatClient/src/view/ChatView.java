package view;

import controller.ChatController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Message;
import model.SessionManager;
import model.User;
import socket.ClientSocket;

/**
 * @author alond
 */
public class ChatView extends JFrame implements ChatController.View {

    private JPanel panelHeader;
    private JPanel panelHistorialMensajes;
    private JScrollPane scrollChat;
    private JPanel panelInput;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnVolver;
    private JLabel lblStatusCircle;
    private JLabel lblUserName;

    private ChatController controller;

    public ChatView() {
        // Distinct mock data to easily differentiate profiles during preview mode
        this(
                new User(2, "Anna Clara", "annabanana@email.com", true));
    }

    public ChatView(User receiverUser) {
        super();
        controller = new ChatController(this, receiverUser);
        initComponents();
        configurarEstilos();
        actualizarEstadoUsuario();
        controller.connect();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat");
        setSize(500, 600);
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Header configuration
        panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));

        btnVolver = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawLine(16, 6, 8, 12);
                g2.drawLine(8, 12, 16, 18);
                g2.dispose();
            }
        };
        btnVolver.setPreferredSize(new Dimension(24, 24));
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnVolver.addActionListener(e -> {
            try {
                new UsersListView().setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "UsersListView no encontrada.");
            }
        });

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

        lblUserName = new JLabel("UserName");
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblUserName.setForeground(Color.WHITE);

        panelHeader.add(btnVolver);
        panelHeader.add(lblStatusCircle);
        panelHeader.add(lblUserName);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(panelHeader, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 30));
        headerContainer.add(separator, BorderLayout.SOUTH);

        getContentPane().add(headerContainer, BorderLayout.NORTH);

        // Switched layout to GridBagLayout for strict control over row compression
        panelHistorialMensajes = new JPanel(new GridBagLayout());
        panelHistorialMensajes.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollChat = new JScrollPane(panelHistorialMensajes);
        scrollChat.setBorder(null);
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);

        getContentPane().add(scrollChat, BorderLayout.CENTER);

        JPanel panelSouthContainer = new JPanel(new BorderLayout());
        panelSouthContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        panelInput = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(29, 43, 84));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
                g2.dispose();
            }
        };
        panelInput.setLayout(new BorderLayout());
        panelInput.setBorder(new EmptyBorder(5, 20, 5, 20));

        txtMensaje = new JTextField("Ingresa un mensaje");
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMensaje.setForeground(new Color(200, 200, 200));
        txtMensaje.setBorder(null);
        txtMensaje.setOpaque(false);

        UIDefaults sinFondo = new UIDefaults();
        sinFondo.put("TextField[Enabled].backgroundPainter", (javax.swing.Painter<JComponent>) (g, c, w, h) -> {
        });
        sinFondo.put("TextField[Focused].backgroundPainter", (javax.swing.Painter<JComponent>) (g, c, w, h) -> {
        });
        txtMensaje.putClientProperty("Nimbus.Overrides", sinFondo);
        txtMensaje.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

        txtMensaje.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtMensaje.getText().equals("Ingresa un mensaje")) {
                    txtMensaje.setText("");
                    txtMensaje.setForeground(Color.WHITE);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtMensaje.getText().isEmpty()) {
                    txtMensaje.setText("Ingresa un mensaje");
                    txtMensaje.setForeground(new Color(200, 200, 200));
                    txtMensaje.setBorder(null);
                    txtMensaje.setOpaque(false);
                    txtMensaje.setBackground(new Color(0, 0, 0, 0));
                }
            }
        });

        btnEnviar = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));

                int[] xPoints = {4, 20, 4, 8};
                int[] yPoints = {4, 12, 20, 12};
                g2.drawPolygon(xPoints, yPoints, 4);
                g2.dispose();
            }
        };
        btnEnviar.setPreferredSize(new Dimension(24, 24));
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelInput.add(txtMensaje, BorderLayout.CENTER);
        panelInput.add(btnEnviar, BorderLayout.EAST);
        panelSouthContainer.add(panelInput, BorderLayout.CENTER);

        getContentPane().add(panelSouthContainer, BorderLayout.SOUTH);

        ActionListener enviarAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarMensajeEnviado();
            }
        };
        btnEnviar.addActionListener(enviarAction);
        txtMensaje.addActionListener(enviarAction);
    }

    private void configurarEstilos() {
        Color fondoOscuroPrincipal = new Color(7, 16, 51);
        panelHeader.setBackground(fondoOscuroPrincipal);
        panelHistorialMensajes.setBackground(fondoOscuroPrincipal);
        scrollChat.getViewport().setBackground(fondoOscuroPrincipal);
        panelInput.getParent().setBackground(fondoOscuroPrincipal);
    }

    public void actualizarEstadoUsuario() {
        User receiverUser = controller.getReceiverUser();
        if (receiverUser != null) {
            lblUserName.setText(receiverUser.getName());
            setTitle("LightChat - " + receiverUser.getName()); // Dynamically updates frame title
            lblStatusCircle.repaint();
        }
    }

    private void procesarMensajeEnviado() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty() || texto.equals("Ingresa un mensaje")) {
            return;
        }

        Message mensajeMio = controller.sendMessage(texto);
        agregarBurbujaMensaje(mensajeMio, true);

        txtMensaje.setText("");
        txtMensaje.requestFocus();

//        Timer timerEco = new Timer(800, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Message mensajeEco = controller.buildEcho(mensajeMio);
//                agregarBurbujaMensaje(mensajeEco, false);
//            }
//        });
//        timerEco.setRepeats(false);
//        timerEco.start();
    }

    private void agregarBurbujaMensaje(Message msg, boolean esMio) {
        // Individual message wrapper panel
        JPanel filaPanel = new JPanel(new GridBagLayout());
        filaPanel.setOpaque(false);
        filaPanel.setBorder(new EmptyBorder(3, 5, 3, 5)); // Tight padding on Y axis

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = esMio ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        JPanel burbuja = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (esMio) {
                    g2.setColor(new Color(57, 79, 133));
                } else {
                    g2.setColor(new Color(112, 142, 255));
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                g2.dispose();
            }
        };
        burbuja.setLayout(new BorderLayout());
        burbuja.setBorder(new EmptyBorder(10, 16, 10, 16));
        burbuja.setOpaque(false);

        JLabel lblTexto = new JLabel("<html><p style=\"width: 200px;\">" + msg.getText() + "</p></html>");
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTexto.setForeground(Color.WHITE);
        burbuja.add(lblTexto, BorderLayout.CENTER);

        filaPanel.add(burbuja, gbc);

        // Clears any previously added layout spacer panel to append the new row securely
        Component[] components = panelHistorialMensajes.getComponents();
        for (Component c : components) {
            if (c instanceof JPanel && "spacerY".equals(c.getName())) {
                panelHistorialMensajes.remove(c);
            }
        }

        // Constraints for adding rows inside the parent panelHistorialMensajes layout grid
        GridBagConstraints gbcFila = new GridBagConstraints();
        gbcFila.gridx = 0;
        gbcFila.gridy = panelHistorialMensajes.getComponentCount();
        gbcFila.weightx = 1.0;
        gbcFila.weighty = 0.0; // Rows take minimum height footprint
        gbcFila.fill = GridBagConstraints.HORIZONTAL;
        gbcFila.anchor = GridBagConstraints.NORTH;
        panelHistorialMensajes.add(filaPanel, gbcFila);

        // Appends a transparent filler row at the absolute bottom to push all rows together seamlessly
        GridBagConstraints gbcSpacer = new GridBagConstraints();
        gbcSpacer.gridx = 0;
        gbcSpacer.gridy = gbcFila.gridy + 1;
        gbcSpacer.weightx = 1.0;
        gbcSpacer.weighty = 1.0; // Absorbs remainder panel height
        gbcSpacer.fill = GridBagConstraints.BOTH;

        JPanel spacerY = new JPanel();
        spacerY.setName("spacerY");
        spacerY.setOpaque(false);
        panelHistorialMensajes.add(spacerY, gbcSpacer);

        panelHistorialMensajes.revalidate();
        panelHistorialMensajes.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollChat.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
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
        // Forzar la ejecución en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear una instancia local del mensaje para pintar en la interfaz
            // El emisor en este caso es el 'receiverUser' del controlador (la otra persona)
            User remoteUser = controller.getReceiverUser();
            User localUser = SessionManager.getInstance().getCurrentUser();

            Message receivedMessage = new Message(remoteUser, localUser, textContent, java.time.LocalDateTime.now().toString());

            // Agregar la burbuja al historial indicando que NO es un mensaje propio (esMio = false)
            agregarBurbujaMensaje(receivedMessage, false);
        });
    }

    @Override
    public void onDeleteChat() {

    }
}
