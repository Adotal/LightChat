package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * UI base compartida para las vistas de chat (directo y de grupo).
 *
 * Concentra el header con botón "volver", el historial de mensajes con burbujas
 * redondeadas, y el input redondeado con placeholder y botón de envío. Las
 * subclases personalizan los extremos del header y definen qué ocurre al enviar
 * un mensaje.
 */
public abstract class BaseChatView extends JFrame {

    protected JPanel panelHeader;
    protected JLabel lblTitle;
    protected JButton btnVolver;
    protected JPanel panelHistorialMensajes;
    protected JScrollPane scrollChat;
    protected JPanel panelInput;
    protected JTextField txtMensaje;
    protected JButton btnEnviar;

    protected static final Color FONDO_OSCURO_PRINCIPAL = new Color(7, 16, 51);
    private static final String PLACEHOLDER = "Ingresa un mensaje";

    /** Construye toda la estructura compartida de la ventana. */
    protected void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("LightChat");
        setSize(500, 600);
        setMinimumSize(new Dimension(400, 500));
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // ===== Header =====
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
        btnVolver.addActionListener(e -> volverALista());

        lblTitle = new JLabel("UserName");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        panelHeader.add(btnVolver);
        JComponent leading = getHeaderLeadingIndicator();
        if (leading != null) {
            panelHeader.add(leading);
        }
        panelHeader.add(lblTitle);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(panelHeader, BorderLayout.CENTER);
        JComponent trailing = getHeaderTrailing();
        if (trailing != null) {
            headerContainer.add(trailing, BorderLayout.EAST);
        }
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 30));
        headerContainer.add(separator, BorderLayout.SOUTH);

        getContentPane().add(headerContainer, BorderLayout.NORTH);

        // ===== Historial de mensajes =====
        panelHistorialMensajes = new JPanel(new GridBagLayout());
        panelHistorialMensajes.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollChat = new JScrollPane(panelHistorialMensajes);
        scrollChat.setBorder(null);
        scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);

        getContentPane().add(scrollChat, BorderLayout.CENTER);

        // ===== Input =====
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

        txtMensaje = new JTextField(PLACEHOLDER);
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
                if (txtMensaje.getText().equals(PLACEHOLDER)) {
                    txtMensaje.setText("");
                    txtMensaje.setForeground(Color.WHITE);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtMensaje.getText().isEmpty()) {
                    txtMensaje.setText(PLACEHOLDER);
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

        configurarEstilos();
    }

    private void configurarEstilos() {
        panelHeader.setBackground(FONDO_OSCURO_PRINCIPAL);
        panelHistorialMensajes.setBackground(FONDO_OSCURO_PRINCIPAL);
        scrollChat.getViewport().setBackground(FONDO_OSCURO_PRINCIPAL);
        panelInput.getParent().setBackground(FONDO_OSCURO_PRINCIPAL);
    }

    /** Fija el título mostrado en el header. */
    protected void setHeaderTitle(String titulo) {
        lblTitle.setText(titulo);
    }

    private void procesarMensajeEnviado() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty() || texto.equals(PLACEHOLDER)) {
            return;
        }
        onEnviar(texto);
        txtMensaje.setText("");
        txtMensaje.requestFocus();
    }

    /**
     * Agrega una burbuja de mensaje al historial.
     *
     * @param texto        contenido del mensaje
     * @param esMio        true si lo envió el usuario actual (alineado a la derecha)
     * @param autor        nombre a mostrar encima de la burbuja para mensajes ajenos
     *                     (puede ser null para no mostrar etiqueta)
     * @param colorBurbuja color de fondo de la burbuja; si es null se usa el
     *                     esquema de 2 colores por defecto (propio/ajeno)
     */
    protected void agregarBurbujaMensaje(String texto, boolean esMio, String autor, Color colorBurbuja) {
        JPanel filaPanel = new JPanel(new GridBagLayout());
        filaPanel.setOpaque(false);
        filaPanel.setBorder(new EmptyBorder(3, 5, 3, 5));

        final Color colorFinal = (colorBurbuja != null)
                ? colorBurbuja
                : (esMio ? new Color(57, 79, 133) : new Color(112, 142, 255));

        JPanel burbuja = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorFinal);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                g2.dispose();
            }
        };
        burbuja.setLayout(new BorderLayout());
        burbuja.setBorder(new EmptyBorder(10, 16, 10, 16));
        burbuja.setOpaque(false);

        JLabel lblTexto = new JLabel("<html><p style=\"width: 200px;\">" + texto + "</p></html>");
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTexto.setForeground(Color.WHITE);
        burbuja.add(lblTexto, BorderLayout.CENTER);

        int fila = 0;
        // Etiqueta con el nombre del autor encima de la burbuja (solo mensajes ajenos).
        if (!esMio && autor != null) {
            JLabel lblAutor = new JLabel(autor);
            lblAutor.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblAutor.setForeground(new Color(200, 210, 230));
            GridBagConstraints gbcAutor = new GridBagConstraints();
            gbcAutor.gridx = 0;
            gbcAutor.gridy = fila++;
            gbcAutor.weightx = 1.0;
            gbcAutor.anchor = GridBagConstraints.WEST;
            gbcAutor.fill = GridBagConstraints.NONE;
            gbcAutor.insets = new Insets(0, 8, 2, 0);
            filaPanel.add(lblAutor, gbcAutor);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 1.0;
        gbc.anchor = esMio ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        filaPanel.add(burbuja, gbc);

        // Elimina el spacer anterior antes de añadir la nueva fila.
        for (Component c : panelHistorialMensajes.getComponents()) {
            if (c instanceof JPanel && "spacerY".equals(c.getName())) {
                panelHistorialMensajes.remove(c);
            }
        }

        GridBagConstraints gbcFila = new GridBagConstraints();
        gbcFila.gridx = 0;
        gbcFila.gridy = panelHistorialMensajes.getComponentCount();
        gbcFila.weightx = 1.0;
        gbcFila.weighty = 0.0;
        gbcFila.fill = GridBagConstraints.HORIZONTAL;
        gbcFila.anchor = GridBagConstraints.NORTH;
        panelHistorialMensajes.add(filaPanel, gbcFila);

        GridBagConstraints gbcSpacer = new GridBagConstraints();
        gbcSpacer.gridx = 0;
        gbcSpacer.gridy = gbcFila.gridy + 1;
        gbcSpacer.weightx = 1.0;
        gbcSpacer.weighty = 1.0;
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

    /** Vuelve a la lista de usuarios y cierra esta ventana. */
    protected void volverALista() {
        try {
            new UsersListView().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "UsersListView no encontrada.");
        }
    }

    // ===== Hooks para subclases =====

    /** Acción a ejecutar cuando el usuario envía un mensaje. */
    protected abstract void onEnviar(String texto);

    /** Componente opcional entre el botón volver y el título (p. ej. estado). */
    protected JComponent getHeaderLeadingIndicator() {
        return null;
    }

    /** Componente opcional al extremo derecho del header (p. ej. opciones). */
    protected JComponent getHeaderTrailing() {
        return null;
    }
}
