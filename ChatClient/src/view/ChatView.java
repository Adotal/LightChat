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

    // Componentes principales de la UI
    private JPanel panelHeader;
    private JPanel panelHistorialMensajes;
    private JScrollPane scrollChat;
    private JPanel panelInput;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JLabel lblStatusCircle;
    private JLabel lblUserName;

    // Modelos de datos para el hilo de conversación
    private User currentUser;       // Tú (Remitente)
    private User receiverUser;      // El usuario con el que chateas (Destinatario)
    private Chat chatActual;

    public ChatView() {
        initComponents();
        initDataMock(); // Inicializa datos de prueba locales
        configurarEstilos();
        actualizarEstadoUsuario();
    }

    /**
     * Construcción de la estructura responsiva mediante código.
     */
    private void initComponents() {
        // Configuración básica del JFrame
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat");
        setSize(500, 600);
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // --- 1. ENCABEZADO (ZONA NORTH) ---
        panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        
        // Componente personalizado para el círculo de estado del usuario
        lblStatusCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Si está conectado: Azul brillante (#2544c4), si no: Gris azulado (#8ba2b3)
                if (receiverUser != null && receiverUser.isIsConnected()) {
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

        panelHeader.add(lblStatusCircle);
        panelHeader.add(lblUserName);
        
        // Separador inferior del Header
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(panelHeader, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 80));
        headerContainer.add(separator, BorderLayout.SOUTH);
        
        getContentPane().add(headerContainer, BorderLayout.NORTH);

        // --- 2. HISTORIAL DE MENSAJES INFINITO (ZONA CENTER) ---
        panelHistorialMensajes = new JPanel();
        panelHistorialMensajes.setLayout(new BoxLayout(panelHistorialMensajes, BoxLayout.Y_AXIS));
        panelHistorialMensajes.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollChat = new JScrollPane(panelHistorialMensajes);
        scrollChat.setBorder(null);
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16); // Scroll suave

        getContentPane().add(scrollChat, BorderLayout.CENTER);

        // --- 3. BARRA DE ENTRADA OVALADA (ZONA SOUTH) ---
        // Contenedor general inferior
        JPanel panelSouthContainer = new JPanel(new BorderLayout());
        panelSouthContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel con forma de cápsula ovalada
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

        // Evento Placeholder básico
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
                    txtMensaje.setOpaque(false); // Le dice a Swing que no pinte su fondo por defecto
                    txtMensaje.setBackground(new Color(0, 0, 0, 0)); // Fuerza un fondo 100% transparente
                }
            }
        });

        // Botón Enviar (Flecha dibujada nativamente por vectores, lista para recibir PNG posterior)
        btnEnviar = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                // Dibuja un icono vectorial de flecha/avión de papel
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

        // --- LOGICA DE ACCION (EVENTO ENVIAR) ---
        ActionListener enviarAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarMensajeEnviado();
            }
        };
        btnEnviar.addActionListener(enviarAction);
        txtMensaje.addActionListener(enviarAction); // Enviar al presionar "Enter"
    }

    /**
     * Aplica la paleta de colores oscuros del diseño original de la imagen.
     */
    private void configurarEstilos() {
        Color fondoOscuroPrincipal = new Color(7, 16, 51);
        
        panelHeader.setBackground(fondoOscuroPrincipal);
        panelHistorialMensajes.setBackground(fondoOscuroPrincipal);
        scrollChat.getViewport().setBackground(fondoOscuroPrincipal);
        panelInput.getParent().setBackground(fondoOscuroPrincipal);
    }

    /**
     * Instancia objetos mock (falsos) locales para la simulación del entorno.
     */
    private void initDataMock() {
        currentUser = new User(1, "Anna", "annabanana@email.com", true);
        receiverUser = new User(2, "Anna", "annabanana@email.com", true); // Cambia a false para probar el círculo gris #8ba2b3
        
        ArrayList<Message> listaMensajes = new ArrayList<>();
        chatActual = new Chat(101, receiverUser, listaMensajes);
    }

    /**
     * Refresca los datos del Header con la información del POJO User.
     */
    public void actualizarEstadoUsuario() {
        if (receiverUser != null) {
            lblUserName.setText(receiverUser.getUserName());
            lblStatusCircle.repaint(); 
        }
    }

    /**
     * Controla el flujo de envío del POJO Message y simula la respuesta inmediata (Eco).
     */
    private void procesarMensajeEnviado() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty() || texto.equals("Ingresa un mensaje")) {
            return;
        }

        // 1. Crear el objeto POJO Message para el remitente (Tú)
        Message mensajeMio = new Message(currentUser, receiverUser, texto, LocalDateTime.now());
        chatActual.getMessages().add(mensajeMio);

        // 2. Pintarlo en la interfaz de forma responsiva (Alineado a la Derecha)
        agregarBurbujaMensaje(mensajeMio, true);
        
        // Limpiar caja de texto
        txtMensaje.setText("");
        txtMensaje.requestFocus();

        // 3. SIMULACIÓN DEL BACKEND (ECO): El otro usuario te regresa exactamente el mismo texto
        Timer timerEco = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Message mensajeEco = new Message(receiverUser, currentUser, " " + mensajeMio.getText(), LocalDateTime.now());
                chatActual.getMessages().add(mensajeEco);
                
                // Pintarlo en la interfaz (Alineado a la Izquierda)
                agregarBurbujaMensaje(mensajeEco, false);
            }
        });
        timerEco.setRepeats(false);
        timerEco.start();
    }

    /**
     * Genera dinámicamente las burbujas redondeadas y las empaqueta de forma responsiva.
     */
    private void agregarBurbujaMensaje(Message msg, boolean esMio) {
        // Contenedor de fila para forzar alineación lateral correcta al estirar la pantalla
        JPanel filaPanel = new JPanel(new GridBagLayout());
        filaPanel.setOpaque(false);
        filaPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; 
        // Si es mío, se recarga a la derecha (EAST). Si es del otro, a la izquierda (WEST).
        gbc.anchor = esMio ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        // Burbuja de texto con diseño redondeado exacto
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

        // Añadir al panel general de historial y refrescar interfaz
        panelHistorialMensajes.add(filaPanel);
        panelHistorialMensajes.revalidate();
        panelHistorialMensajes.repaint();

        // Autoscroll: Mueve la barra de desplazamiento hacia abajo de forma automática
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollChat.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Ejecutable principal del ChatView.
     */
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
