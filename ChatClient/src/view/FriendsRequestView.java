package view;

import controller.FriendsRequestController;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author alond
 */
public class FriendsRequestView extends JFrame implements FriendsRequestController.View {

    private JPanel panelPrincipal;
    private JScrollPane scrollPrincipal;
    private JPanel containerInvitaciones;
    private JButton btnVolver; // Nuevo botón de regreso

    private FriendsRequestController controller;
    private List<FriendsRequestController.FriendReqItem> recibidasAmistad = new ArrayList<>();
    private List<FriendsRequestController.FriendReqItem> enviadasAmistad = new ArrayList<>();
    private List<FriendsRequestController.GroupInvItem> invitacionesGrupo = new ArrayList<>();

    public FriendsRequestView() {
        initComponents();
        configurarEstilos();
        controller = new FriendsRequestController(this);
        controller.connect();
        controller.fetchAll();
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

    // ===== Callbacks del controller =====

    @Override
    public void onFriendRequests(List<FriendsRequestController.FriendReqItem> received,
            List<FriendsRequestController.FriendReqItem> sent) {
        SwingUtilities.invokeLater(() -> {
            this.recibidasAmistad = received;
            this.enviadasAmistad = sent;
            reconstruir();
        });
    }

    @Override
    public void onGroupInvitations(List<FriendsRequestController.GroupInvItem> invitations) {
        SwingUtilities.invokeLater(() -> {
            this.invitacionesGrupo = invitations;
            reconstruir();
        });
    }

    /** Reconstruye las tarjetas con los datos reales actuales. */
    private void reconstruir() {
        containerInvitaciones.removeAll();

        JPanel cardRecibidas = crearTarjetaSeccion("Invitaciones recibidas");
        for (FriendsRequestController.FriendReqItem fr : recibidasAmistad) {
            cardRecibidas.add(crearFilaAccion(
                    fr.name + " quiere ser tu amigo", fr.email,
                    () -> controller.respondFriend(fr.id, true),
                    () -> controller.respondFriend(fr.id, false)));
        }
        for (FriendsRequestController.GroupInvItem gi : invitacionesGrupo) {
            cardRecibidas.add(crearFilaAccion(
                    gi.ownerName + " te invitó a " + gi.groupTitle, "Invitación de grupo",
                    () -> controller.respondGroup(gi.id, true),
                    () -> controller.respondGroup(gi.id, false)));
        }
        containerInvitaciones.add(cardRecibidas);
        containerInvitaciones.add(Box.createVerticalStrut(20));

        JPanel cardEnviadas = crearTarjetaSeccion("Invitaciones enviadas");
        for (FriendsRequestController.FriendReqItem fr : enviadasAmistad) {
            cardEnviadas.add(crearFilaEstado(fr.name, "Estado: " + traducirEstado(fr.status)));
        }
        containerInvitaciones.add(cardEnviadas);
        containerInvitaciones.add(Box.createVerticalGlue());

        containerInvitaciones.revalidate();
        containerInvitaciones.repaint();
    }

    private String traducirEstado(String status) {
        switch (status) {
            case "APPROVED": return "Aceptada";
            case "DENIED":   return "Rechazada";
            default:          return "Pendiente";
        }
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

    /** Fila con botones aceptar/rechazar que ejecutan acciones reales del controller. */
    private JPanel crearFilaAccion(String titulo, String sub, Runnable onAceptar, Runnable onRechazar) {
        JPanel panelNormal = new JPanel(new BorderLayout());
        panelNormal.setOpaque(false);
        panelNormal.setBorder(new EmptyBorder(4, 22, 4, 22));
        panelNormal.setMaximumSize(new Dimension(4000, 48));

        panelNormal.add(crearTextos(titulo, sub), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panelBotones.setOpaque(false);

        JButton btnCheck = crearBotonCircular(true, new Color(28, 55, 219));
        JButton btnCross = crearBotonCircular(false, new Color(34, 46, 84));
        btnCheck.addActionListener(e -> onAceptar.run());
        btnCross.addActionListener(e -> onRechazar.run());
        panelBotones.add(btnCheck);
        panelBotones.add(btnCross);

        panelNormal.add(panelBotones, BorderLayout.EAST);
        return panelNormal;
    }

    /** Fila de solo lectura que muestra el estado de una solicitud enviada. */
    private JPanel crearFilaEstado(String titulo, String sub) {
        JPanel panelNormal = new JPanel(new BorderLayout());
        panelNormal.setOpaque(false);
        panelNormal.setBorder(new EmptyBorder(4, 22, 4, 22));
        panelNormal.setMaximumSize(new Dimension(4000, 48));
        panelNormal.add(crearTextos(titulo, sub), BorderLayout.CENTER);
        return panelNormal;
    }

    private JPanel crearTextos(String titulo, String sub) {
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
        return panelTextos;
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