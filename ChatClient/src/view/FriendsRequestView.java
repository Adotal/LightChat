package view;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author alond
 */
public class FriendsRequestView extends JFrame {

    private JPanel panelPrincipal;
    private JScrollPane scrollPrincipal;
    private JPanel containerInvitaciones;
    private JButton btnVolver; // Nuevo botón de regreso

    public FriendsRequestView() {
        initComponents();
        configurarEstilos();
        cargarDatosPrueba();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("LightChat - Notificaciones");
        setSize(430, 680);
        setMinimumSize(new Dimension(380, 580));
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel(new BorderLayout());
        
       
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelHeader.setOpaque(false);
        panelHeader.setBorder(new EmptyBorder(25, 20, 10, 20));
        
        // BOTÓN VOLVER
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
        
        // Acción de navegación para retornar a la lista principal
        btnVolver.addActionListener(e -> {
            try {
                new UsersListView().setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "UsersListView no encontrada.");
            }
        });
        
        JLabel lblIconoCampana = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                
                g2.drawArc(6, 3, 12, 13, 0, 180); 
                g2.drawLine(6, 10, 3, 16); 
                g2.drawLine(18, 10, 21, 16); 
                g2.drawLine(3, 16, 21, 16); 
                g2.drawArc(10, 16, 4, 3, 180, 180); 
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(24, 24); }
        };

        JSeparator sepHeader = new JSeparator(JSeparator.HORIZONTAL);
        sepHeader.setForeground(new Color(255, 255, 255, 50));

        
        JPanel panelLineContainer = new JPanel(new GridBagLayout());
        panelLineContainer.setOpaque(false);
        
        panelLineContainer.setPreferredSize(new Dimension(270, 24)); 
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.fill = GridBagConstraints.HORIZONTAL;
        gbcL.weightx = 1.0;
        panelLineContainer.add(sepHeader, gbcL);

        
        panelHeader.add(btnVolver);
        panelHeader.add(lblIconoCampana);
        panelHeader.add(panelLineContainer);

        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        // Scroll
        containerInvitaciones = new JPanel();
        containerInvitaciones.setLayout(new BoxLayout(containerInvitaciones, BoxLayout.Y_AXIS));
        containerInvitaciones.setOpaque(false);
        containerInvitaciones.setBorder(new EmptyBorder(10, 25, 20, 25));

        scrollPrincipal = new JScrollPane(containerInvitaciones);
       
        scrollPrincipal.setBorder(BorderFactory.createEmptyBorder());
        scrollPrincipal.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPrincipal.setOpaque(false);
        scrollPrincipal.getViewport().setOpaque(false);
        scrollPrincipal.getVerticalScrollBar().setUnitIncrement(16);

        panelPrincipal.add(scrollPrincipal, BorderLayout.CENTER);
        getContentPane().add(panelPrincipal);
    }

    private void configurarEstilos() {
        Color fondoOscuro = new Color(7, 16, 51);
        panelPrincipal.setBackground(fondoOscuro);
    }

    private void cargarDatosPrueba() {
        // INVITACIONES RECIBIDAS 
        JPanel cardRecibidas = crearTarjetaSeccion("Invitaciones recibidas");
        cardRecibidas.add(crearFilaInvitacion("Quiere ser tu amigo", "UsuarioX quiere ser tu amigo", true, cardRecibidas));
        cardRecibidas.add(crearFilaInvitacion("Nuevo grupo", "Nombre del grupo", true, cardRecibidas));
        
        containerInvitaciones.add(cardRecibidas);
        containerInvitaciones.add(Box.createVerticalStrut(20)); 

        // INVITACIONES ENVIADAS
        JPanel cardEnviadas = crearTarjetaSeccion("Invitaciones enviadas");
        cardEnviadas.add(crearFilaInvitacion("Usuario X", "Por confirmar invitacion de amistad", false, cardEnviadas));
        
        containerInvitaciones.add(cardEnviadas);
        containerInvitaciones.add(Box.createVerticalGlue()); 
    }

    private JPanel crearTarjetaSeccion(String titulo) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 31, 66)); 
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(lblTitulo);

        card.add(crearSeparadorPunteado());
        card.add(Box.createVerticalStrut(5)); 

        return card;
    }

    private JPanel crearFilaInvitacion(String titulo, String sub, boolean recibida, JPanel parentCard) {
        JPanel filaContenedor = new JPanel(new CardLayout());
        filaContenedor.setOpaque(false);
        filaContenedor.setPreferredSize(new Dimension(320, 48));
        filaContenedor.setMinimumSize(new Dimension(200, 48));
        filaContenedor.setMaximumSize(new Dimension(4000, 48));

        // SOLICITUD NORMAL 
        JPanel panelNormal = new JPanel(new BorderLayout());
        panelNormal.setOpaque(false);
        panelNormal.setBorder(new EmptyBorder(4, 22, 4, 22));

        JPanel panelTextos = new JPanel(new GridLayout(2, 1, 0, -2));
        panelTextos.setOpaque(false);
        
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblT.setForeground(Color.WHITE);
        
        JLabel lblS = new JLabel(sub);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblS.setForeground(new Color(130, 143, 179));

        panelTextos.add(lblT);
        panelTextos.add(lblS);
        panelNormal.add(panelTextos, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panelBotones.setOpaque(false);

        // Acción de aceptar, rechazar y deshacer
        JPanel panelFeedback = new JPanel(new BorderLayout());
        panelFeedback.setOpaque(false);
        panelFeedback.setBorder(new EmptyBorder(4, 22, 4, 22));

        JLabel lblMensajeFeedback = new JLabel();
        lblMensajeFeedback.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblMensajeFeedback.setForeground(new Color(160, 175, 210));
        panelFeedback.add(lblMensajeFeedback, BorderLayout.CENTER);

        JButton btnDeshacer = new JButton("Deshacer");
        btnDeshacer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDeshacer.setForeground(new Color(112, 142, 255));
        btnDeshacer.setContentAreaFilled(false);
        btnDeshacer.setBorderPainted(false);
        btnDeshacer.setFocusPainted(false);
        btnDeshacer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelFeedback.add(btnDeshacer, BorderLayout.EAST);

        filaContenedor.add(panelNormal, "NORMAL");
        filaContenedor.add(panelFeedback, "FEEDBACK");

        CardLayout cl = (CardLayout) filaContenedor.getLayout();

        // EVENTOS
        if (recibida) {
            JButton btnCheck = crearBotonCircular(true, new Color(28, 55, 219));
            JButton btnCross = crearBotonCircular(false, new Color(34, 46, 84));

            btnCheck.addActionListener(e -> {
                lblMensajeFeedback.setText("Invitación aceptada");
                cl.show(filaContenedor, "FEEDBACK");
            });

            btnCross.addActionListener(e -> {
                lblMensajeFeedback.setText("Invitación rechazada");
                cl.show(filaContenedor, "FEEDBACK");
            });

            panelBotones.add(btnCheck);
            panelBotones.add(btnCross);
        } else {
            JButton btnCancel = crearBotonCircular(false, new Color(34, 46, 84));
            
            btnCancel.addActionListener(e -> {
                lblMensajeFeedback.setText("Solicitud cancelada");
                cl.show(filaContenedor, "FEEDBACK");
            });

            panelBotones.add(btnCancel);
        }

        // Evento del botón deshacer (regresa a la vista normal)
        btnDeshacer.addActionListener(e -> cl.show(filaContenedor, "NORMAL"));

        panelNormal.add(panelBotones, BorderLayout.EAST);
        return filaContenedor;
    }

    private JButton crearBotonCircular(boolean esCheck, Color fondo) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(fondo);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                if (esCheck) {
                    g2.drawLine(9, 15, 13, 19);
                    g2.drawLine(13, 19, 21, 9);
                } else {
                    g2.drawLine(10, 10, 20, 20);
                    g2.drawLine(20, 10, 10, 20);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComponent crearSeparadorPunteado() {
        return new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                float[] dash = {5f, 4f};
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, dash, 0.0f));
                g2.drawLine(22, 0, getWidth() - 22, 0);
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(10, 2); }
            @Override
            public Dimension getMaximumSize() { return new Dimension(4000, 2); }
        };
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {}

        EventQueue.invokeLater(() -> {
            new FriendsRequestView().setVisible(true);
        });
    }
}