package view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.SessionManager;
import model.User;
import model.dbrequest.LoginRequest;
import socket.ClientSocket;

/**
 * @author alond
 */
public class UsersListView extends JFrame {

    private JLayeredPane layeredPane;
    private JPanel panelBaseContenido;
    private JPanel panelHeaderTabs;
    private JPanel panelListaContactos;
    private JScrollPane scrollContactos;
    private JPanel panelNavigationBottom;

    private JButton btnTabTodos;
    private JButton btnTabAmigos;
    private JButton btnTabGrupos;

    private JButton btnNotificaciones;
    private JButton btnCerrarSesion;

    private ArrayList<User> listaUsuariosMock;
    private ArrayList<Integer> listaIdAmigosMock;
    private String pestañaActiva = "TODOS";

    public UsersListView() {
        initComponents();
        initSocket();
        loadData();
        configurarEstilos();
        loadContentAccordingToTab();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat - Contactos");
        setSize(450, 650);
        setMinimumSize(new Dimension(380, 550));
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        getContentPane().add(layeredPane, BorderLayout.CENTER);

        panelBaseContenido = new JPanel(new BorderLayout());

        panelHeaderTabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnTabTodos = crearBotonPestaña("Todos", "TODOS");
        btnTabAmigos = crearBotonPestaña("Amigos", "AMIGOS");
        btnTabGrupos = crearBotonPestaña("Grupos", "GRUPOS");

        panelHeaderTabs.add(btnTabTodos);
        panelHeaderTabs.add(btnTabAmigos);
        panelHeaderTabs.add(btnTabGrupos);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(panelHeaderTabs, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 30));
        headerContainer.add(separator, BorderLayout.SOUTH);

        panelBaseContenido.add(headerContainer, BorderLayout.NORTH);

        panelListaContactos = new JPanel(new GridBagLayout());
        panelListaContactos.setBorder(new EmptyBorder(5, 0, 80, 0));

        scrollContactos = new JScrollPane(panelListaContactos);
        scrollContactos.setBorder(null);
        scrollContactos.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollContactos.getVerticalScrollBar().setUnitIncrement(16);

        panelBaseContenido.add(scrollContactos, BorderLayout.CENTER);
        layeredPane.add(panelBaseContenido, JLayeredPane.DEFAULT_LAYER);

        // Barra de navegación (notificaciones y cerrar sesión)
        panelNavigationBottom = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 31, 66));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.dispose();
            }
        };
        panelNavigationBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 35, 8));
        panelNavigationBottom.setOpaque(false);

        // Campana de notificaciones
        btnNotificaciones = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawArc(7, 4, 10, 12, 0, 180);
                g2.drawLine(7, 10, 5, 15);
                g2.drawLine(17, 10, 19, 15);
                g2.drawLine(5, 15, 19, 15);
                g2.drawArc(10, 15, 4, 3, 180, 180);
                g2.setColor(new Color(112, 142, 255));
                g2.fill(new Ellipse2D.Double(15, 3, 5, 5));
                g2.dispose();
            }
        };
        configurarBotonIcono(btnNotificaciones);

        // NAVEGACIÓN
        btnNotificaciones.addActionListener(e -> {
            try {
                new FriendsRequestView().setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "FriendsRequestView no encontrada.");
            }
        });

        // Cerrar Sesión 
        btnCerrarSesion = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.6f));

                g2.drawRoundRect(4, 4, 11, 16, 5, 5);

                g2.setColor(new Color(20, 31, 66));
                g2.fillRect(14, 8, 3, 8);

                g2.setColor(Color.WHITE);
                g2.drawLine(9, 12, 20, 12);
                g2.drawLine(16, 8, 20, 12);
                g2.drawLine(16, 16, 20, 12);
                g2.dispose();
            }
        };
        configurarBotonIcono(btnCerrarSesion);

        panelNavigationBottom.add(btnNotificaciones);
        panelNavigationBottom.add(btnCerrarSesion);
        layeredPane.add(panelNavigationBottom, JLayeredPane.PALETTE_LAYER);

        // NAVEGACIÓN: Al aceptar cerrar sesión
        btnCerrarSesion.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro de cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (respuesta == JOptionPane.YES_OPTION) {
                try {

                    // Build JSON using Jackson
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode request = mapper.createObjectNode();
                    request.put("type", "LOGOUT_REQUEST");
                    request.put("email", SessionManager.getInstance().getCurrentUser().getEmail());
                    String json = mapper.writeValueAsString(request);

                    // Enviar el JSON al servidor
                    ClientSocket.getInstance().sendText(json);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "LoginView no encontrada.");
                }
            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int width = layeredPane.getWidth();
                int height = layeredPane.getHeight();
                panelBaseContenido.setBounds(0, 0, width, height);
                panelNavigationBottom.setBounds((width - 210) / 2, height - 68, 210, 48);
            }
        });
    }

    private void configurarEstilos() {
        Color fondoOscuroPrincipal = new Color(7, 16, 51);
        panelHeaderTabs.setBackground(fondoOscuroPrincipal);
        panelListaContactos.setBackground(fondoOscuroPrincipal);
        scrollContactos.getViewport().setBackground(fondoOscuroPrincipal);
        panelBaseContenido.setBackground(fondoOscuroPrincipal);
    }

    private void loadData() {
        // Initialize arrays
        listaUsuariosMock = new ArrayList<>();
        listaIdAmigosMock = new ArrayList<>();

        // Send fetch all users request
        try {

            // Build JSON using Jackson
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode request = mapper.createObjectNode();
            request.put("type", "FETCH_ALL_USERS");
            request.put("email", SessionManager.getInstance().getCurrentUser().getEmail());

            String json = mapper.writeValueAsString(request);
            // Enviar el JSON al servidor
            ClientSocket.getInstance().sendText(json);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al enviar solicitud: " + ex.getMessage());
            ex.printStackTrace();
        }

        // Fetch ALL Users
//        listaUsuariosMock.add(new User(1, "Anna", "anna@email.com", false));
//        listaUsuariosMock.add(new User(2, "Fernando", "fernando@email.com", false));
//        listaUsuariosMock.add(new User(3, "Damaris", "damaris@email.com", true));
//        listaUsuariosMock.add(new User(4, "Adro", "adro@email.com", false));
//        listaIdAmigosMock.add(1);
//        listaIdAmigosMock.add(3);
    }

    private void loadContentAccordingToTab() {
        panelListaContactos.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        if (pestañaActiva.equals("GRUPOS")) {
            panelListaContactos.add(crearFilaGrupo("Nombre del grupo 1"), gbc);
            gbc.gridy++;
            panelListaContactos.add(crearFilaGrupo("Nombre del grupo 2"), gbc);
            gbc.gridy++;
        } else {
            for (User user : listaUsuariosMock) {
                boolean esAmigo = listaIdAmigosMock.contains(user.getIdUser());
                if (pestañaActiva.equals("AMIGOS") && !esAmigo) {
                    continue;
                }
                panelListaContactos.add(crearFilaContacto(user, esAmigo), gbc);
                gbc.gridy++;
            }
        }

        gbc.weighty = 1.0;
        JPanel empujador = new JPanel();
        empujador.setOpaque(false);
        panelListaContactos.add(empujador, gbc);
        panelListaContactos.revalidate();
        panelListaContactos.repaint();
    }

    private JPanel crearFilaContacto(User usuario, boolean esAmigoInitial) {
        final String[] estado = {esAmigoInitial ? "AMIGO" : "NINGUNO"};
        JPanel fila = crearContenedorFila();

        JLabel lblAvatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(usuario.getIsConnected() ? new Color(37, 68, 196) : new Color(139, 162, 179));
                g2.fill(new Ellipse2D.Double(0, 0, 32, 32));
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };

        JButton btnAccion = crearBotonAccionContacto(estado);
        JPanel textos = crearPanelTextos(usuario.getName(), "Ultimo mensaje");

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 11));
        content.setOpaque(false);
        content.add(lblAvatar);
        content.add(textos);

        fila.add(content, BorderLayout.WEST);
        fila.add(btnAccion, BorderLayout.EAST);

        configurarEventosHover(fila, btnAccion);

        // NAVEGACIÓN: Al hacer clic en cualquier parte de la fila del usuario, abre el ChatView
        fila.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new ChatView().setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UsersListView.this, "ChatView no encontrada.");
                }
            }
        });

        return armarFilaCompleta(fila);
    }

    private JPanel crearFilaGrupo(String nombre) {
        JPanel fila = crearContenedorFila();

        JLabel lblIcono = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(112, 142, 255));
                g2.setStroke(new BasicStroke(1.3f));

                g2.drawOval(0, 0, 31, 31);

                g2.drawOval(11, 6, 8, 8);
                g2.drawArc(6, 15, 18, 11, 0, 180);

                g2.drawOval(5, 10, 6, 6);
                g2.drawArc(1, 17, 13, 9, 0, 180);

                g2.drawOval(19, 10, 6, 6);
                g2.drawArc(16, 17, 13, 9, 0, 180);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };

        JPanel textos = crearPanelTextos(nombre, "Ultimo mensaje");
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 11));
        content.setOpaque(false);
        content.add(lblIcono);
        content.add(textos);

        fila.add(content, BorderLayout.WEST);

        // NAVEGACIÓN: Al hacer clic en la fila del grupo, también abre el ChatView CAMBIAR A CHAT DE GRUPO
        fila.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new ChatView().setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UsersListView.this, "ChatView no encontrada.");
                }
            }
        });

        return armarFilaCompleta(fila);
    }

    private JButton crearBotonPestaña(String texto, String id) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (pestañaActiva.equals(id)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(23, 35, 74));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(90, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            pestañaActiva = id;
            panelHeaderTabs.repaint();
            loadContentAccordingToTab();
        });
        return btn;
    }

    private JPanel crearContenedorFila() {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setPreferredSize(new Dimension(200, 54));
        fila.setMaximumSize(new Dimension(3000, 54));
        fila.setBorder(new EmptyBorder(0, 20, 0, 20));
        fila.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano al pasar por encima del contacto
        return fila;
    }

    private JPanel crearPanelTextos(String titulo, String sub) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, -2));
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(new Color(140, 150, 180));
        p.add(t);
        p.add(s);
        return p;
    }

    private JButton crearBotonAccionContacto(String[] estado) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(estado[0].equals("AMIGO") ? new Color(112, 142, 255) : Color.WHITE);
                g2.drawOval(4, 3, 8, 8);
                g2.drawArc(1, 13, 14, 8, 0, 180);
                if (estado[0].equals("AMIGO")) {
                    g2.drawLine(16, 12, 18, 15);
                    g2.drawLine(18, 15, 23, 9);
                } else if (estado[0].equals("ESPERANDO")) {
                    g2.setColor(new Color(112, 142, 255));
                    g2.fillOval(16, 11, 2, 2);
                    g2.fillOval(19, 11, 2, 2);
                    g2.fillOval(22, 11, 2, 2);
                } else {
                    g2.drawLine(19, 8, 19, 14);
                    g2.drawLine(16, 11, 22, 11);
                }
                g2.dispose();
            }
        };
        configurarBotonIcono(btn);
        btn.setVisible(false);
        btn.addActionListener(e -> {
            if (estado[0].equals("AMIGO")) {
                JOptionPane.showMessageDialog(this, "Este contacto ya es tu amigo");
            } else if (estado[0].equals("ESPERANDO")) {
                JOptionPane.showMessageDialog(this, "Esperando respuesta a la solicitud", "Pendiente", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Solicitud de amistad enviada");
                estado[0] = "ESPERANDO";
                btn.repaint();
            }
        });
        return btn;
    }

    private void configurarEventosHover(JPanel fila, JButton btn) {
        fila.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setVisible(true);
                fila.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!fila.getBounds().contains(e.getPoint())) {
                    btn.setVisible(false);
                    fila.repaint();
                }
            }
        });
    }

    private JPanel armarFilaCompleta(JPanel fila) {
        JSeparator div = new JSeparator();
        div.setForeground(new Color(255, 255, 255, 12));
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(fila, BorderLayout.CENTER);
        contenedor.add(div, BorderLayout.SOUTH);
        return contenedor;
    }

    private void configurarBotonIcono(JButton btn) {
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        }
        EventQueue.invokeLater(() -> new UsersListView().setVisible(true));
    }

    private void initSocket() {
        // Get the global (singleton) instance
        ClientSocket client = ClientSocket.getInstance();

        // Tell the client to send updates to this frame's label
        //client.setStatusListener(mensaje -> lblConStatus.setText(mensaje));
        // Server responess Mapping
        client.setMessageListener(rawJson -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(rawJson);

                if (rootNode.has("type")) {
                    String tipo = rootNode.get("type").asText();

                    if (tipo.equals("UPDATE_CON_STATUS")) {
                        // Update con status

                    } else if (tipo.equals("LOGOUT_SUCCESS")) {
                        // Clear session
                        SessionManager.getInstance().logout();
                        // Return login
                        new LoginView().setVisible(true);
                        dispose();
                    } else if (tipo.equals("UPDATE_USERS_LIST")) {

                        if (rootNode.has("users")) {

                            //  Use TypeReference to preserve the target collection types
                            ArrayList<User> downloadedUsersList = mapper.convertValue(
                                    rootNode.get("users"),
                                    new TypeReference<ArrayList<User>>() {
                            }
                            );

                            // ArrayList<User> is full
                            System.out.println("Successfully loaded " + downloadedUsersList.size() + " users.");
                            SwingUtilities.invokeLater(() -> {
                                listaUsuariosMock = downloadedUsersList;
                                loadContentAccordingToTab();
                            });
                        }

                    }
                }
            } catch (Exception ex) {
                System.err.println("Error procesando respuesta del servidor: " + ex.getMessage());
            }
        });

        // Connect
        client.tryConnect();
    }

}
