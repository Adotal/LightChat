package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;

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

    public UsersListView() {
        initComponents();
        initDataMock(); 
        configurarEstilos();
        cargarContactosEnInterfaz();
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
      
        panelHeaderTabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 8));
        
        btnTabTodos = crearBotonTab("Todos", true);
        btnTabAmigos = crearBotonTab("Amigos", false);
        btnTabGrupos = crearBotonTab("Grupos", false);

        panelHeaderTabs.add(btnTabTodos);
        panelHeaderTabs.add(btnTabAmigos);
        panelHeaderTabs.add(btnTabGrupos);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(panelHeaderTabs, BorderLayout.CENTER);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 30));
        headerContainer.add(separator, BorderLayout.SOUTH);
        
        panelBaseContenido.add(headerContainer, BorderLayout.NORTH);

        // Lista de contactos 
        panelListaContactos = new JPanel(new GridBagLayout());
        panelListaContactos.setBorder(new EmptyBorder(5, 0, 80, 0)); 

        scrollContactos = new JScrollPane(panelListaContactos);
        scrollContactos.setBorder(null);
        scrollContactos.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollContactos.getVerticalScrollBar().setUnitIncrement(16);

        panelBaseContenido.add(scrollContactos, BorderLayout.CENTER);
        layeredPane.add(panelBaseContenido, JLayeredPane.DEFAULT_LAYER);

        // Barra de navegacion (notificaciones y cerrar sesion)
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
// icono campana
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

     //icono cerrar sesion
        btnCerrarSesion = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.6f));
                
                //contenedor
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

        btnCerrarSesion.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(
                    UsersListView.this, 
                    "¿Seguro de cerrar sesión?", 
                    "Cerrar Sesión", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE
            );
            if (respuesta == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // Responsividad
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int width = layeredPane.getWidth();
                int height = layeredPane.getHeight();
                
                panelBaseContenido.setBounds(0, 0, width, height);
                
                int barWidth = 210;
                int barHeight = 48;
                int barX = (width - barWidth) / 2;
                int barY = height - barHeight - 20; 
                
                panelNavigationBottom.setBounds(barX, barY, barWidth, barHeight);
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

    private void initDataMock() {
        listaUsuariosMock = new ArrayList<>();
        listaIdAmigosMock = new ArrayList<>();

        listaUsuariosMock.add(new User(1, "Anna", "anna@email.com", true));
        listaUsuariosMock.add(new User(2, "Fernando", "fernando@email.com", false));
        listaUsuariosMock.add(new User(3, "Damaris", "damaris@email.com", true));
        listaUsuariosMock.add(new User(4, "Adro", "adro@email.com", false));

        listaIdAmigosMock.add(1);
        listaIdAmigosMock.add(3);
    }

    private void cargarContactosEnInterfaz() {
        panelListaContactos.removeAll();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH; 

        for (User user : listaUsuariosMock) {
            panelListaContactos.add(crearFilaContacto(user, listaIdAmigosMock.contains(user.getId())), gbc);
            gbc.gridy++;
        }
        
        gbc.weighty = 1.0;
        JPanel panelEmpujador = new JPanel();
        panelEmpujador.setOpaque(false);
        panelListaContactos.add(panelEmpujador, gbc);
        
        panelListaContactos.revalidate();
        panelListaContactos.repaint();
    }

    private JPanel crearFilaContacto(User usuario, boolean esAmigo) {
        // Estados de contacto 
        final String[] estadoContacto = { esAmigo ? "AMIGO" : "NINGUNO" };
        
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        
        fila.setPreferredSize(new Dimension(200, 54));
        fila.setMinimumSize(new Dimension(200, 54));
        fila.setMaximumSize(new Dimension(3000, 54));
        fila.setBorder(new EmptyBorder(0, 20, 0, 20)); 

        JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 11)); 
        panelIzquierdo.setOpaque(false);

        JLabel lblAvatarCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(usuario.isIsConnected() ? new Color(37, 68, 196) : new Color(139, 162, 179));
                g2.fill(new Ellipse2D.Double(0, 0, 32, 32)); 
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };

        JPanel panelTextos = new JPanel(new GridLayout(2, 1, 0, -2)); 
        panelTextos.setOpaque(false);

        JLabel lblNombre = new JLabel(usuario.getUserName());
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setForeground(Color.WHITE);

        JLabel lblSubtexto = new JLabel("Ultimo mensaje");
        lblSubtexto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtexto.setForeground(new Color(140, 150, 180));

        panelTextos.add(lblNombre);
        panelTextos.add(lblSubtexto);

        panelIzquierdo.add(lblAvatarCircle);
        panelIzquierdo.add(panelTextos);

        //icono de estado 
        JButton btnAccionAmigo = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.5f));

                // Silueta de usuario 
                g2.setColor(estadoContacto[0].equals("AMIGO") ? new Color(112, 142, 255) : Color.WHITE);
                g2.drawOval(4, 3, 8, 8);
                g2.drawArc(1, 13, 14, 8, 0, 180);

                if (estadoContacto[0].equals("AMIGO")) {
                    //amigos
                    g2.setColor(new Color(112, 142, 255));
                    g2.drawLine(16, 12, 18, 15);
                    g2.drawLine(18, 15, 23, 9);
                } else if (estadoContacto[0].equals("ESPERANDO")) {
                    // pendiente
                    g2.setColor(new Color(112, 142, 255)); 
                    g2.fillOval(16, 11, 2, 2);
                    g2.fillOval(19, 11, 2, 2);
                    g2.fillOval(22, 11, 2, 2);
                } else {
                    //general
                    g2.setColor(Color.WHITE);
                    g2.drawLine(19, 8, 19, 14);
                    g2.drawLine(16, 11, 22, 11);
                }
                g2.dispose();
            }
        };
        configurarBotonIcono(btnAccionAmigo);
        btnAccionAmigo.setVisible(false); 

        // Mensaje emergente sombre estado de contacto 
        btnAccionAmigo.addActionListener(e -> {
            if (estadoContacto[0].equals("AMIGO")) {
                JOptionPane.showMessageDialog(
                        UsersListView.this, 
                        "Este contacto ya es tu amigo", 
                        "Información", 
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if (estadoContacto[0].equals("ESPERANDO")) {
                JOptionPane.showMessageDialog(
                        UsersListView.this, 
                        "Esperando respuesta a la solicitud", 
                        "Solicitud Pendiente", 
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        UsersListView.this, 
                        "Solicitud de amistad enviada", 
                        "Estado de Solicitud", 
                        JOptionPane.INFORMATION_MESSAGE
                );
                // esperando
                estadoContacto[0] = "ESPERANDO"; 
                btnAccionAmigo.repaint();
            }
        });

        fila.add(panelIzquierdo, BorderLayout.WEST);
        fila.add(btnAccionAmigo, BorderLayout.EAST);

        fila.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnAccionAmigo.setVisible(true);
                fila.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!fila.getBounds().contains(e.getPoint())) {
                    btnAccionAmigo.setVisible(false);
                    fila.repaint();
                }
            }
        });

        JSeparator div = new JSeparator();
        div.setForeground(new Color(255, 255, 255, 12));
        
        JPanel contenedorFilaCompleta = new JPanel(new BorderLayout());
        contenedorFilaCompleta.setOpaque(false);
        contenedorFilaCompleta.add(fila, BorderLayout.CENTER);
        contenedorFilaCompleta.add(div, BorderLayout.SOUTH);

        return contenedorFilaCompleta;
    }

    private void ajustarEstilosBotonesTab(JButton btn, boolean sel) {
        btn.setFont(new Font("Segoe UI", sel ? Font.BOLD : Font.PLAIN, 14));
        btn.setForeground(sel ? Color.WHITE : new Color(120, 130, 160));
    }

    private void configurarBotonIcono(JButton btn) {
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JButton crearBotonTab(String texto, boolean seleccionado) {
        JButton btn = new JButton(texto);
        ajustarEstilosBotonesTab(btn, seleccionado);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (seleccionado) {
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(57, 79, 133)),
                    BorderFactory.createEmptyBorder(0, 3, 3, 3)
            ));
        }
        return btn;
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
            java.util.logging.Logger.getLogger(UsersListView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(() -> {
            new UsersListView().setVisible(true);
        });
    }
}