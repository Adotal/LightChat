package view;

import controller.GroupsController;
import model.Group;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class GroupChatView extends JFrame implements GroupsController.View {

    // ESTRUCTURAS DE CONTROL DE POSICIONAMIENTO DINÁMICO PARA LAS BURBUJAS DE TEXTO
    private GroupLayout layoutChat;
    private GroupLayout.SequentialGroup grupoVerticalPrincipal;
    private GroupLayout.ParallelGroup grupoHorizontalPrincipal;

    // COMPONENTES PRINCIPALES DE LA INTERFAZ GRÁFICA DEL CHAT
    private JTextField txtHeader;   
    private JTextField txtInput;    
    private JButton btnEnviar;      
    private JPanel panelChat;       
    private JScrollPane scrollPane;  

    // INSTANCIAS DE DATOS Y SEGUIMIENTO DE FLUJO DE MENSAJES
    private Group grupoActual;
    private int contadorMensajes = 0;
    private GroupsController controller;

    // MATRIZ DE COLORES ASIGNADOS PARA DISTINGUIR VISUALMENTE A LOS USUARIOS
    private final Color[] PALETA_COLORES = {
        new Color(82, 113, 255),  // AZUL ELÁSTICO
        new Color(141, 75, 255),  // MORADO VIBRANTE
        new Color(0, 185, 206)    // CIAN OSCURO
    };

    public GroupChatView(Group grupo) {
        this.grupoActual = grupo;

        // CONFIGURACIÓN DE LA VENTANA PRINCIPAL (TÍTULO DINÁMICO Y COMPORTAMIENTO DE CIERRE)
        setTitle("Chat - " + grupoActual.getTitle());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(450, 600); 
        setMinimumSize(new Dimension(400, 500)); 
        
        // ESTABLECE LA DISPOSICIÓN PRINCIPAL EN CAPAS Y DEFINE EL COLOR DE FONDO
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(52, 73, 137)); 

        // CREACIÓN Y ESTILIZADO DE LA BANDEJA SUPERIOR CON EL NOMBRE DEL GRUPO ACTIVO
        txtHeader = new JTextField(grupoActual.getTitle());
        txtHeader.setEditable(false); 
        txtHeader.setBorder(new EmptyBorder(0, 50, 0, 0)); 
        txtHeader.setBackground(new Color(11, 22, 64)); 
        txtHeader.setForeground(Color.WHITE); 
        txtHeader.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        txtHeader.setPreferredSize(new Dimension(450, 60));

        // Botón de opciones del grupo (integrantes / abandonar / eliminar).
        JButton btnOpciones = new JButton("•••");
        btnOpciones.setBackground(new Color(11, 22, 64));
        btnOpciones.setForeground(Color.WHITE);
        btnOpciones.setFocusPainted(false);
        btnOpciones.setBorder(null);
        btnOpciones.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        btnOpciones.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOpciones.addActionListener(e -> mostrarMenuOpciones(btnOpciones));

        JPanel panelHeaderTop = new JPanel(new BorderLayout());
        panelHeaderTop.add(txtHeader, BorderLayout.CENTER);
        panelHeaderTop.add(btnOpciones, BorderLayout.EAST);
        getContentPane().add(panelHeaderTop, BorderLayout.NORTH);

        // CREACIÓN DEL PANEL DE FONDO QUE ALBERGA Y RENDERIZA LAS FILAS DE MENSAJES
        panelChat = new JPanel();
        panelChat.setBackground(new Color(52, 73, 137)); 
        
        // PREPARA EL CONFIGURADOR DE DISEÑO INICIAL ANTES DE RECIBIR MENSAJES
        reiniciarLayoutBase();
        CerrarYConstruirLayoutInterno();

        // INTEGRACIÓN DE LA BARRA DE DESPLAZAMIENTO PARA CONTENER EL FLUJO DE LA CONVERSACIÓN
        scrollPane = new JScrollPane(panelChat);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // DISEÑO Y PERSONALIZACIÓN DEL CAMPO DE TEXTO DE ENTRADA DE MENSAJES
        txtInput = new JTextField("");
        txtInput.setBackground(new Color(11, 23, 62));
        txtInput.setForeground(Color.WHITE);
        txtInput.setCaretColor(Color.WHITE); 
        txtInput.setBorder(new EmptyBorder(10, 20, 10, 10)); 
        txtInput.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));

        // DISEÑO Y ENFOQUE DEL BOTÓN DE ACCIÓN PARA EL ENVÍO
        btnEnviar = new JButton(">");
        btnEnviar.setBackground(new Color(11, 23, 62));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        btnEnviar.setFocusPainted(false); 
        btnEnviar.setBorder(null);

        // PANEL INFERIOR CONTENEDOR QUE CONECTA EL CAMPO DE TEXTO Y EL BOTÓN DE ENVÍO
        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        panelInferior.setBackground(new Color(52, 73, 137));
        panelInferior.setBorder(new EmptyBorder(15, 20, 20, 20)); 
        panelInferior.add(txtInput, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
        btnEnviar.setPreferredSize(new Dimension(50, 45)); 
        getContentPane().add(panelInferior, BorderLayout.SOUTH);

        // REGISTRO DEL GESTOR DE EVENTOS AL HACER CLIC SOBRE EL BOTÓN DE ENVÍO
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarNuevoMensaje();
            }
        });
        
        // REGISTRO DEL GESTOR DE EVENTOS AL PRESIONAR LA TECLA ENTER DENTRO DEL CAMPO DE TEXTO
        txtInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarNuevoMensaje();
            }
        });

        // ESTABLECE LA POSICIÓN INICIAL DE LA VENTANA CENTRADA EN EL MONITOR
        setLocationRelativeTo(null);

        // Conecta la lógica real del grupo y pide el historial persistido.
        controller = new GroupsController(this, grupoActual);
        controller.connect();
        controller.requestHistory();

        // Botón volver a la lista al cerrar la ventana.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                new UsersListView().setVisible(true);
            }
        });
    }

    // CONSTRUYE LAS COORDENADAS E INTERFACES DE ALINEACIÓN PARA EL GESTOR DE DISEÑO
    private void reiniciarLayoutBase() {
        layoutChat = new GroupLayout(panelChat);
        panelChat.setLayout(layoutChat);

        grupoHorizontalPrincipal = layoutChat.createParallelGroup(GroupLayout.Alignment.LEADING);
        grupoVerticalPrincipal = layoutChat.createSequentialGroup();
    }

    // ENVÍA EL MENSAJE AL SERVIDOR Y LO RENDERIZA LOCALMENTE
    private void procesarNuevoMensaje() {
        String texto = txtInput.getText().trim();
        if (texto.isEmpty()) {
            return; // validar no vacío
        }
        controller.sendGroupMessage(texto);
        renderizarMensaje("Tú", texto, PALETA_COLORES[0]);
        txtInput.setText("");
    }

    // Inyecta una burbuja, reconstruye el layout y baja el scroll.
    private void renderizarMensaje(String autor, String texto, Color color) {
        inyectarComponentesMensaje(autor, texto, color);
        CerrarYConstruirLayoutInterno();
        contadorMensajes++;
        panelChat.revalidate();
        panelChat.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private Color colorPara(String autor) {
        return PALETA_COLORES[Math.abs(autor.hashCode()) % PALETA_COLORES.length];
    }

    // ===== Callbacks de GroupsController.View =====

    @Override
    public void onGroupMessage(GroupsController.GMsg msg) {
        SwingUtilities.invokeLater(() ->
                renderizarMensaje(msg.mine ? "Tú" : msg.senderName, msg.text,
                        msg.mine ? PALETA_COLORES[0] : colorPara(msg.senderName)));
    }

    @Override
    public void onGroupHistory(List<GroupsController.GMsg> messages) {
        SwingUtilities.invokeLater(() -> {
            for (GroupsController.GMsg m : messages) {
                renderizarMensaje(m.mine ? "Tú" : m.senderName, m.text,
                        m.mine ? PALETA_COLORES[0] : colorPara(m.senderName));
            }
        });
    }

    @Override
    public void onGroupDeleted() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "El grupo ha sido eliminado.");
            new UsersListView().setVisible(true);
            dispose();
        });
    }

    @Override
    public void onGroupLeft() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Has abandonado el grupo.");
            new UsersListView().setVisible(true);
            dispose();
        });
    }

    @Override
    public void onGroupMembers(List<GroupsController.GMember> members, boolean isOwner) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("Integrantes de " + grupoActual.getTitle() + ":\n\n");
            sb.append("• Creador (tú)").append(isOwner ? "\n" : "");
            for (GroupsController.GMember m : members) {
                sb.append("• ").append(m.name).append(" - ").append(traducirEstado(m.status)).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Integrantes", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private String traducirEstado(String status) {
        switch (status) {
            case "APPROVED": return "Aceptado";
            case "DENIED":   return "Rechazado";
            default:          return "Pendiente";
        }
    }

    /** Menú de opciones del grupo: ver integrantes, abandonar o eliminar. */
    private void mostrarMenuOpciones(Component invoker) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem verIntegrantes = new JMenuItem("Ver integrantes");
        verIntegrantes.addActionListener(e -> controller.fetchMembers());
        menu.add(verIntegrantes);

        JMenuItem abandonar = new JMenuItem("Abandonar grupo");
        abandonar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Abandonar este grupo?",
                    "Abandonar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                controller.leaveGroup();
            }
        });
        menu.add(abandonar);

        JMenuItem eliminar = new JMenuItem("Eliminar grupo (creador)");
        eliminar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Eliminar el grupo? Esta acción es permanente.",
                    "Eliminar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                controller.deleteGroup();
            }
        });
        menu.add(eliminar);

        menu.show(invoker, 0, invoker.getHeight());
    }

    // CREA Y ACOMODA DE FORMA SECUENCIAL LA BURBUJA DE TEXTO Y LA ETIQUETA DEL USUARIO
    private void inyectarComponentesMensaje(String usuario, String mensaje, Color colorBurbuja) {
        // ENCABEZADO DE TEXTO QUE INDICA EL EMISOR DEL MENSAJE
        JLabel lblUser = new JLabel(usuario);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));

        // ÁREA DE TEXTO QUE COMPONE EL CUERPO PRINCIPAL DE LA BURBUJA DE MENSAJE
        JTextArea txtMensaje = new JTextArea(mensaje);
        txtMensaje.setBackground(colorBurbuja); 
        txtMensaje.setForeground(Color.WHITE);
        txtMensaje.setEditable(false);
        txtMensaje.setLineWrap(true);       
        txtMensaje.setWrapStyleWord(true);   
        txtMensaje.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 15));
        txtMensaje.setBorder(new EmptyBorder(8, 10, 8, 10)); 

        // DEFINE LAS REGLAS DE ANCHO, MÁRGENES Y REDIMENSIONAMIENTO HORIZONTAL DEL COMPONENTE
        grupoHorizontalPrincipal.addGroup(layoutChat.createSequentialGroup()
            .addGap(20) 
            .addGroup(layoutChat.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(lblUser, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addComponent(txtMensaje, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, 250))
            .addGap(20) 
        );

        // ESTABLECE EL DISTANCIAMIENTO VERTICAL Y CONEXIÓN ENTRE ELEMENTOS DE UNA FILA
        grupoVerticalPrincipal.addGap(15) 
            .addComponent(lblUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED) 
            .addComponent(txtMensaje, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    // APLICA Y CONSOLIDA LOS ESQUEMAS FINALES HORIZONTALES Y VERTICALES SOBRE EL CONTENEDOR
    private void CerrarYConstruirLayoutInterno() {
        layoutChat.setHorizontalGroup(grupoHorizontalPrincipal);
        layoutChat.setVerticalGroup(grupoVerticalPrincipal.addGap(15));
    }
}
