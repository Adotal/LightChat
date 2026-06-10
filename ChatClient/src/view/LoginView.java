package view;

import controller.LoginController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author alond
 * @author adotal
 */
public class LoginView extends JFrame implements LoginController.View {

    private JPanel panelPrincipal;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblIrARegistro;
    private JLabel lblConStatus;

    private LoginController controller;

    public LoginView() {
        super();
        initComponents();
        controller = new LoginController(this);
        controller.connect();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("LightChat - Iniciar Sesión");
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

        lblConStatus = new JLabel("<html><div style='text-align:center;'>Conectando...</div></html>");
        lblConStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblConStatus.setForeground(Color.WHITE);
        lblConStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 100, 0);
        panelPrincipal.add(lblConStatus, gbc);

        JLabel lblTitulo = new JLabel("<html><div style='text-align:center;'>Iniciar<br>sesión</div></html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 55, 0);
        panelPrincipal.add(lblTitulo, gbc);

        JLabel lblEmail = crearLabelCampo("EMAIL");
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 4, 8, 4);
        panelPrincipal.add(lblEmail, gbc);

        txtEmail = crearCampoTexto("");
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 24, 0);
        panelPrincipal.add(txtEmail, gbc);

        JLabel lblPass = crearLabelCampo("CONTRASEÑA");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 4, 8, 4);
        panelPrincipal.add(lblPass, gbc);

        txtPassword = crearCampoPassword("");
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 60, 0);
        panelPrincipal.add(txtPassword, gbc);

        //BOTON DE INICIAR SESION
        btnLogin = new JButton("Log in") {
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
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 56));

        btnLogin.addActionListener(e -> {

            // Get data from textFields
            String email = txtEmail.getText();
            // getPassword() returns char[], parse to String
            String password = new String(txtPassword.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor llena ambos campos.");
                return;
            }

            // Delega la lógica de negocio al controller
            controller.login(email, password);
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 35, 0);
        panelPrincipal.add(btnLogin, gbc);

        // REGISTRAR
        lblIrARegistro = new JLabel("<html><span style='color:#8095FF; font-weight:bold; text-decoration:underline;'>Registrar</span></html>");

        lblIrARegistro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIrARegistro.setForeground(new Color(150, 160, 190));
        lblIrARegistro.setHorizontalAlignment(SwingConstants.CENTER);
        lblIrARegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblIrARegistro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignUpView().setVisible(true); // Abre la pantalla de recuperación
                dispose();                                 // Cierra el Login
            }
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 0, 0);
        panelPrincipal.add(lblIrARegistro, gbc);

        // RECUPERAR CONTRASEÑA 
        JLabel lblIrARecuperar = new JLabel(
                "<html>¿Olvidaste tu contraseña? Haz "
                + "<span style='color:#8095FF; font-weight:bold; text-decoration:underline;'>click</span> aquí</html>");
        lblIrARecuperar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIrARecuperar.setForeground(new Color(150, 160, 190));
        lblIrARecuperar.setHorizontalAlignment(SwingConstants.CENTER);
        lblIrARecuperar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblIrARecuperar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (txtEmail.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(LoginView.this, "Debes escribir un correo para poder recuperar contraseña");
                } else {
                    // Abre la pantalla de recuperación de contraseña
                    new RecoverPasswordView(txtEmail.getText()).setVisible(true);
                    // Cierra actual
                    dispose();
                }
            }
        });

        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 0, 0);
        panelPrincipal.add(lblIrARecuperar, gbc);

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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(34, 48, 108));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(34, 48, 108));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
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

    // Getters públicos
    public JTextField getTxtEmail() {
        return txtEmail;
    }

    public JPasswordField getTxtPassword() {
        return txtPassword;
    }

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public JLabel getLblIrARegistro() {
        return lblIrARegistro;
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

        EventQueue.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }

    // ===== Callbacks de LoginController.View (solo UI/navegación) =====

    @Override
    public void onStatus(String message) {
        lblConStatus.setText(message);
    }

    @Override
    public void onLoginSuccess() {
        new UsersListView().setVisible(true);
        this.dispose();
    }

    @Override
    public void onLoginError(String message) {
        JOptionPane.showMessageDialog(this, "Error en login: " + message, "Error de acceso", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onTooManyAttempts() {
        if (txtEmail.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes escribir un correo para poder recuperar contraseña");
        } else {
            new RecoverPasswordView(txtEmail.getText()).setVisible(true);
            dispose();
        }
    }
}
