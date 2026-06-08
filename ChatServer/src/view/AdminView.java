package view;

import dao.ServerEventDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import server.JavaServer;

/**
 * Vista de administrador del servidor (RQF42, RQNF91-94). Muestra en tiempo
 * real los eventos generados por usuarios y por el servidor, con marca temporal
 * legible. Se suscribe a {@link JavaServer} como listener y, al iniciar, carga
 * los eventos persistidos recientes.
 *
 * @author adotal
 */
public class AdminView extends JFrame {

    private final JTextArea areaEventos;

    public AdminView(JavaServer server) {
        setTitle("LightChat - Panel de Administración del Servidor");
        setSize(700, 500);
        setMinimumSize(new Dimension(500, 350));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel header = new JLabel("Acciones del servidor");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.setOpaque(true);
        header.setBackground(new Color(11, 22, 64));
        getContentPane().add(header, BorderLayout.NORTH);

        areaEventos = new JTextArea();
        areaEventos.setEditable(false);
        areaEventos.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaEventos.setBackground(new Color(7, 16, 51));
        areaEventos.setForeground(new Color(220, 228, 255));
        areaEventos.setMargin(new java.awt.Insets(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(areaEventos);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scroll, BorderLayout.CENTER);

        // Cargar eventos persistidos recientes (orden cronológico).
        try {
            for (String line : new ServerEventDAO().getRecentEvents(200)) {
                areaEventos.append(line + "\n");
            }
        } catch (Exception ex) {
            areaEventos.append("[AdminView] No se pudieron cargar eventos previos: " + ex.getMessage() + "\n");
        }

        // Suscribirse a los eventos en vivo (RQNF93: reflejo en ≤1s).
        server.addEventListener(this::appendEvento);
    }

    /** Añade una línea de evento y baja el scroll automáticamente. */
    public void appendEvento(String line) {
        SwingUtilities.invokeLater(() -> {
            areaEventos.append(line + "\n");
            areaEventos.setCaretPosition(areaEventos.getDocument().getLength());
        });
    }
}
