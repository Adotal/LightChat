package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Chat;
import model.Message;
import model.User;

/**
 * @author alond
 */
public class ChatView extends JFrame {

    private JPanel panelHeader;
    private JPanel panelHistorialMensajes;
    private JScrollPane scrollChat;
    private JPanel panelInput;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnVolver; // Nuevo botón de navegación
    private JLabel lblStatusCircle;
    private JLabel lblUserName;

    private User currentUser;       // remitente
    private User receiverUser;      // destinatario
    private Chat chatActual;

    public ChatView() {
        super();
        initComponents();
        initDataMock(); // ESO ES PARA LAS PRUEBAS 
        configurarEstilos();
        actualizarEstadoUsuario();
    }

    private void initComponents() {
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat"); //Nombre que Adro escogio
        setSize(500, 600);
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Header con FlowLayout alineado a la izquierda
        panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        
        // BOTÓN VOLVER: Dibujado mediante vectores de Graphics2D (<)
        btnVolver = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Dibuja la flecha de regreso estilizada <
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
        
        // NAVEGACIÓN: Regresar a UsersListView
        btnVolver.addActionListener(e -> {
            try {
                new UsersListView().setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "UsersListView no encontrada.");
            }
        });
        
        // ESTADO DEL USUARIO
        lblStatusCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Si está conectado: Azul #2544c4 si no: Gris azulado #8ba2b3
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

        // Agregamos primero el botón de volver en el extremo izquierdo
        panelHeader.add(btnVolver);
        panelHeader.add(lblStatusCircle);
        panelHeader.add(lblUserName);
        
        // línea que separa la info del user
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(panelHeader, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 30)); // Bajado un poco el alpha para que sea sutil
        headerContainer.add(separator, BorderLayout.SOUTH);
        
        getContentPane().add(headerContainer, BorderLayout.NORTH);

        // zona de los mensajes 
        panelHistorialMensajes = new JPanel();
        panelHistorialMensajes.setLayout(new BoxLayout(panelHistorialMensajes, BoxLayout.Y_AXIS));
        panelHistorialMensajes.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollChat = new JScrollPane(panelHistorialMensajes);
        scrollChat.setBorder(null);
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16); // Scroll 

        getContentPane().add(scrollChat, BorderLayout.CENTER);

        // Contenedor para ingresar el texto y el vector de enviar
        JPanel panelSouthContainer = new JPanel(new BorderLayout());
        panelSouthContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        panelInput = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(29, 43, 84)); // Color cápsula interna
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

        // Nimbus ignora setOpaque(false) en JTextField; se anulan sus painters de fondo
        UIDefaults sinFondo = new UIDefaults();
        sinFondo.put("TextField[Enabled].backgroundPainter", (javax.swing.Painter<JComponent>) (g, c, w, h) -> {});
        sinFondo.put("TextField[Focused].backgroundPainter", (javax.swing.Painter<JComponent>) (g, c, w, h) -> {});
        txtMensaje.putClientProperty("Nimbus.Overrides", sinFondo);
        txtMensaje.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

        // Evento focus placeholders
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

        // vector de enviar
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

        // Evento enviar !!!
        ActionListener enviarAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarMensajeEnviado();
            }
        };
        btnEnviar.addActionListener(enviarAction);
        txtMensaje.addActionListener(enviarAction); // Enviar al presionar enter
    }

    private void configurarEstilos() {
        Color fondoOscuroPrincipal = new Color(7, 16, 51);
        
        panelHeader.setBackground(fondoOscuroPrincipal);
        panelHistorialMensajes.setBackground(fondoOscuroPrincipal);
        scrollChat.getViewport().setBackground(fondoOscuroPrincipal);
        panelInput.getParent().setBackground(fondoOscuroPrincipal);
    }

    //Esto es para simular la respuesta y el estado del usuario
    private void initDataMock() {
        currentUser = new User(1, "Anna", "annabanana@email.com", true);
        receiverUser = new User(2, "Anna", "annabanana@email.com", true); // si lo pones en false, se cambia a inactivo 
        
        ArrayList<Message> listaMensajes = new ArrayList<>();
        chatActual = new Chat(101, receiverUser, listaMensajes);
    }

    //mandar al pojo de user
    public void actualizarEstadoUsuario() {
        if (receiverUser != null) {
            lblUserName.setText(receiverUser.getName());
            lblStatusCircle.repaint(); 
        }
    }

    //Simula respuesta 
    private void procesarMensajeEnviado() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty() || texto.equals("Ingresa un mensaje")) {
            return;
        }

        //pojo mensaje de remitente 
        Message mensajeMio = new Message(currentUser, receiverUser, texto, LocalDateTime.now());
        chatActual.getMessages().add(mensajeMio);

        agregarBurbujaMensaje(mensajeMio, true);
        
        txtMensaje.setText("");
        txtMensaje.requestFocus();

        // simulacion el otro usuario te regresa exactamente el mismo texto
        Timer timerEco = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message mensajeEco = new Message(receiverUser, currentUser, " " + mensajeMio.getText(), LocalDateTime.now());
                chatActual.getMessages().add(mensajeEco);
                
                agregarBurbujaMensaje(mensajeEco, false);
            }
        });
        timerEco.setRepeats(false);
        timerEco.start();
    }

    private void agregarBurbujaMensaje(Message msg, boolean esMio) {
        
        JPanel filaPanel = new JPanel(new GridBagLayout());
        filaPanel.setOpaque(false);
        filaPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

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
                    g2.setColor(new Color(57, 79, 133)); // Azul oscuro del emisor
                } else {
                    g2.setColor(new Color(112, 142, 255)); // Azul claro del receptor
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                g2.dispose();
            }
        };
        burbuja.setLayout(new BorderLayout());
        burbuja.setBorder(new EmptyBorder(12, 18, 12, 18));
        burbuja.setOpaque(false);

        // Texto del mensaje adentro de la burbuja
        JLabel lblTexto = new JLabel("<html><p style=\"width: 200px;\">" + msg.getText() + "</p></html>");
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTexto.setForeground(Color.WHITE);
        burbuja.add(lblTexto, BorderLayout.CENTER);

        filaPanel.add(burbuja, gbc);

        panelHistorialMensajes.add(filaPanel);
        panelHistorialMensajes.revalidate();
        panelHistorialMensajes.repaint();

        // Autoscroll
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
}