package view;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.dbrequest.RecoverPasswordRequest;
import socket.ClientSocket;

/**
 * @author alond
 */
public class RecoverPasswordView extends JFrame {

    private JPanel panelPrincipal;
    private JPasswordField txtNuevaPassword;
    private JButton btnGuardar;
    private JLabel lblVolverLogin;
    private JLabel lblEmail;
    private String email;

    // Recibe el correo electrónico que estaba en el login
    public RecoverPasswordView(String email) {
        super();
        this.email = email;
        initComponents();
        initSocket();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat - Recuperar Contraseña");
        setSize(420, 720);
        setMinimumSize(new Dimension(420, 720));
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(new Color(8, 18, 68));
        panelPrincipal.setBorder(new EmptyBorder(40, 35, 40, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitulo = new JLabel("<html><div style='text-align:center;'>Nueva<br>contraseña</div></html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 55, 0);
        panelPrincipal.add(lblTitulo, gbc);

        // Email del cual se reestablece contraseña
        lblEmail = crearLabelCampo("EMAIL: " + email);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 4, 40, 4);
        panelPrincipal.add(lblEmail, gbc);

        // CAMPO: NUEVA PASSWORD 
        JLabel lblPass = crearLabelCampo("INGRESA TU NUEVA CONTRASEÑA");
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 4, 8, 4);
        panelPrincipal.add(lblPass, gbc);

        txtNuevaPassword = crearCampoPassword("");
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 60, 0);
        panelPrincipal.add(txtNuevaPassword, gbc);

        //BOTÓN GUARDAR (Regresa al Login)
        btnGuardar = new JButton("Guardar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new Dimension(0, 56));

        // Al hacer clic en Guardar, vuelve al Login
        btnGuardar.addActionListener(e -> {
            String password = new String(txtNuevaPassword.getPassword());

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor llena ambos campos.");
                return;
            }

            // Send recover password request
            try {
                // Create object from data
                RecoverPasswordRequest request = new RecoverPasswordRequest(email, password);

                // Convert object to JSON usign Jackson
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(request);

                // Enviar el JSON al servidor
                ClientSocket.getInstance().sendText(jsonString);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al enviar solicitud: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 35, 0);
        panelPrincipal.add(btnGuardar, gbc);

        //ENLACE PARA CANCELAR / VOLVER
        lblVolverLogin = new JLabel(
                "<html>¿Recordaste tu contraseña? Haz "
                + "<span style='color:#8095FF; font-weight:bold; text-decoration:underline;'>click</span> aquí</html>");
        lblVolverLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblVolverLogin.setForeground(new Color(150, 160, 190));
        lblVolverLogin.setHorizontalAlignment(SwingConstants.CENTER);
        lblVolverLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblVolverLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginView().setVisible(true);
                dispose();
            }
        });

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 0, 0);
        panelPrincipal.add(lblVolverLogin, gbc);

        add(panelPrincipal);
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(220, 225, 235));
        return lbl;
    }

    private JPasswordField crearCampoPassword(String texto) {
        JPasswordField field = new JPasswordField(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(34, 48, 108));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        field.putClientProperty("Nimbus.Overrides", null);
        field.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        field.setPreferredSize(new Dimension(0, 56));
        return field;
    }

    // Getters
    public JPasswordField getTxtNuevaPassword() {
        return txtNuevaPassword;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }

//        EventQueue.invokeLater(() -> {
//            new RecoverPasswordView().setVisible(true);
//        });
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
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(rawJson);

                if (rootNode.has("type")) {
                    String tipo = rootNode.get("type").asText();

                    if (tipo.equals("RECOVER_PASSWORD_SUCCESS")) {
                        // On signup success, go to login
                        JOptionPane.showMessageDialog(this, "Contraseña reestablecida exitosamente");
                        new LoginView().setVisible(true);
                        this.dispose();

                    } else if (tipo.equals("RECOVER_PASSWROD_ERROR")) {
                        // Extract custom error message from server if it exists
                        String errorMsg = rootNode.has("message") ? rootNode.get("message").asText() : "Error desconocido";

                        // On Login failure, display dialog cleanly
                        JOptionPane.showMessageDialog(this, "Error en recover password: " + errorMsg, "Error de acceso", JOptionPane.ERROR_MESSAGE);
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
