package view;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.dbrequest.LoginRequest;
import model.dbrequest.SignUpRequest;
import socket.ClientSocket;

public class SignUpView extends JFrame {

    private JPanel panelPrincipal;
    private JTextField txtUsuario;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnSignUp;
    private JLabel lblIrALogin;

    public SignUpView() {
        super();
        initComponents();
        initSocket();
    }

    private void initComponents() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat - Registro");
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

        JLabel lblTitulo = new JLabel(
                "<html><div style='text-align:center;'>Crea una<br>cuenta</div></html>");

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 55, 0);
        panelPrincipal.add(lblTitulo, gbc);

        JLabel lblUser = crearLabelCampo("NOMBRE DE USUARIO");

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 4, 8, 4);
        panelPrincipal.add(lblUser, gbc);

        txtUsuario = crearCampoTexto("");        

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 24, 0);
        panelPrincipal.add(txtUsuario, gbc);

        JLabel lblEmail = crearLabelCampo("EMAIL");

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 4, 8, 4);
        panelPrincipal.add(lblEmail, gbc);

        txtEmail = crearCampoTexto("");

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 24, 0);
        panelPrincipal.add(txtEmail, gbc);

        JLabel lblPass = crearLabelCampo("PASSWORD");

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 4, 8, 4);
        panelPrincipal.add(lblPass, gbc);

        txtPassword = crearCampoPassword("");

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 60, 0);
        panelPrincipal.add(txtPassword, gbc);

        btnSignUp = new JButton("Sign up") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));

                g2.drawRoundRect(
                        1,
                        1,
                        getWidth() - 3,
                        getHeight() - 3,
                        30,
                        30);

                super.paintComponent(g);

                g2.dispose();
            }
        };

        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setContentAreaFilled(false);
        btnSignUp.setBorderPainted(false);
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignUp.setPreferredSize(new Dimension(0, 56));

        btnSignUp.addActionListener(e -> {

            // Get data from textFields
            String username = txtUsuario.getText();
            String email = txtEmail.getText();
            // getPassword() returns char[], parse to String
            String password = new String(txtPassword.getPassword());

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor llena ambos campos.");
                return;
            }

            // Send login request
            try {
                // Create object from data
                SignUpRequest request = new SignUpRequest(username, email, password);

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

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 35, 0);
        panelPrincipal.add(btnSignUp, gbc);

        //link al login
        lblIrALogin = new JLabel(
                "<html>Si ya tienes una cuenta, haz "
                + "<span style='color:#8095FF;"
                + "font-weight:bold;"
                + "text-decoration:underline;'>click</span> aquí</html>");

        lblIrALogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIrALogin.setForeground(new Color(150, 160, 190));
        lblIrALogin.setHorizontalAlignment(SwingConstants.CENTER);
        lblIrALogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Eveto de ir al login
        lblIrALogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new LoginView().setVisible(true); //abre ventana
                dispose();                        // Cierra la ventana de Registro 
            }
        });

        gbc.gridy = 8;
        gbc.insets = new Insets(5, 0, 0, 0);
        panelPrincipal.add(lblIrALogin, gbc);

        add(panelPrincipal);
    }

    private JLabel crearLabelCampo(String texto) {

        JLabel lbl = new JLabel(texto);

        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(220, 225, 235));

        return lbl;
    }

    private JTextField crearCampoTexto(String texto) {

        JTextField field = new JTextField(texto) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(34, 48, 108));

                g2.fill(new RoundRectangle2D.Double(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        30,
                        30));

                super.paintComponent(g);

                g2.dispose();
            }
        };

        estilizarInput(field);

        return field;
    }

    private JPasswordField crearCampoPassword(String texto) {

        JPasswordField field = new JPasswordField(texto) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(34, 48, 108));

                g2.fill(new RoundRectangle2D.Double(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        30,
                        30));

                super.paintComponent(g);

                g2.dispose();
            }
        };

        estilizarInput(field);

        return field;
    }

    private void estilizarInput(JTextField field) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);

        field.setOpaque(false);

        field.setBackground(new Color(0, 0, 0, 0));
        field.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        field.putClientProperty("Nimbus.Overrides", null);
        field.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

        field.setPreferredSize(new Dimension(0, 56));
    }

    public JTextField getTxtUsuario() {
        return txtUsuario;
    }

    public JTextField getTxtEmail() {
        return txtEmail;
    }

    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    public JButton getBtnSignUp() {
        return btnSignUp;
    }

    public JLabel getLblIrALogin() {
        return lblIrALogin;
    }

    public static void main(String[] args) {

        try {

            for (UIManager.LookAndFeelInfo info
                    : UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(
                            info.getClassName());
                    break;
                }
            }

        } catch (Exception e) {
        }

        EventQueue.invokeLater(() -> {
            new SignUpView().setVisible(true);
        });
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

                    if (tipo.equals("SIGNUP_SUCCESS")) {
                        // On signup success, go to login
                        JOptionPane.showMessageDialog(this, "Registro exitoso");

                        new LoginView().setVisible(true);
                        this.dispose();

                    } else if (tipo.equals("SIGNUP_ERROR")) {
                        // Extract custom error message from server if it exists
                        String errorMsg = rootNode.has("message") ? rootNode.get("message").asText() : "Registro no completado";

                        // On Login failure, display dialog cleanly
                        JOptionPane.showMessageDialog(this, "Error en signup: " + errorMsg, "Error de acceso", JOptionPane.ERROR_MESSAGE);
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
