package view;


import model.Group;
import model.UserGroup;
import model.GroupInvitation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class GroupsView extends JFrame {

    // ESTRUCTURAS DEL LAYOUT DINÁMICO PARA ORGANIZAR LAS FILAS DE LOS GRUPOS
    private GroupLayout layoutLista;
    private GroupLayout.SequentialGroup grupoVerticalPrincipal;
    private GroupLayout.ParallelGroup grupoHorizontalPrincipal;

    // COMPONENTES DE LA INTERFAZ GRÁFICA
    private JTextField txtHeader;   
    private JPanel panelTabs;       
    private JPanel panelLista;      
    private JButton btnAgregar;      

    // COLECCIÓN LOCAL QUE ALMACENA LOS MODELOS DE DATOS DE LOS GRUPOS
    private ArrayList<Group> listaDeGrupos = new ArrayList<>();

    public GroupsView() {
        // CONFIGURACIÓN DE LA VENTANA PRINCIPAL (TÍTULO, DIMENSIONES Y CIERRE)
        setTitle("Grupos");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // ESTABLECE EL DISEÑO BASE DE LA VENTANA Y SU COLOR DE FONDO DE Fowler
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(45, 60, 120));

        // CREACIÓN Y ESTILIZADO DEL ENCABEZADO SUPERIOR "GRUPOS"
        txtHeader = new JTextField("Grupos");
        txtHeader.setEditable(false); 
        txtHeader.setBorder(new EmptyBorder(0, 50, 0, 0)); 
        txtHeader.setBackground(new Color(11, 22, 64)); 
        txtHeader.setForeground(Color.WHITE); 
        txtHeader.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        txtHeader.setPreferredSize(new Dimension(450, 60)); 

        // CREACIÓN DE LA BARRA DE PESTAÑAS (TODOS, AMIGOS, GRUPOS)
        panelTabs = new JPanel(new GridLayout(1, 3, 10, 0));
        panelTabs.setOpaque(false);
        panelTabs.setBorder(new EmptyBorder(15, 20, 5, 20)); 
        panelTabs.add(crearBotonTab("Todos"));
        panelTabs.add(crearBotonTab("Amigos"));
        panelTabs.add(crearBotonTab("Grupos"));

        // CONTENEDOR SUPERIOR QUE AGRUPA EL ENCABEZADO Y LAS PESTAÑAS
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);
        panelSuperior.add(txtHeader, BorderLayout.NORTH);
        panelSuperior.add(panelTabs, BorderLayout.CENTER);
        getContentPane().add(panelSuperior, BorderLayout.NORTH);

        // CREACIÓN DEL PANEL CENTRAL QUE ALBERGARÁ LAS FILAS DE LOS GRUPOS
        panelLista = new JPanel();
        panelLista.setBackground(new Color(45, 60, 120)); 

        // INICIALIZACIÓN DE DATOS SIMULADOS: GRUPO DE SISTEMAS OPERATIVOS II
        ArrayList<UserGroup> usuariosSistemas = new ArrayList<>();
        usuariosSistemas.add(new UserGroup(new GroupInvitation(), 1, "Carlos Gomez", "carlos@mail.com", true));
        usuariosSistemas.add(new UserGroup(new GroupInvitation(), 2, "Ana Martinez", "ana@mail.com", false));
        usuariosSistemas.add(new UserGroup(new GroupInvitation(), 3, "Juan Perez", "juan@mail.com", true));
        listaDeGrupos.add(new Group(101, "Sistemas Operativos II", usuariosSistemas));

        // INICIALIZACIÓN DE DATOS SIMULADOS: GRUPO DE DESARROLLO DE VIDEOJUEGOS 3D
        ArrayList<UserGroup> usuariosUnity = new ArrayList<>();
        usuariosUnity.add(new UserGroup(new GroupInvitation(), 4, "Luis Hernandez", "luis@mail.com", true));
        usuariosUnity.add(new UserGroup(new GroupInvitation(), 5, "Sofia Ruiz", "sofia@mail.com", false));
        listaDeGrupos.add(new Group(102, "Desarrollo de Videojuegos 3D", usuariosUnity));

        // CONSTRUCCIÓN E INYECCIÓN GRÁFICA DE LOS GRUPOS EN EL PANEL
        construirListaGrafica();

        // CONFIGURACIÓN DEL PANEL DE DESPLAZAMIENTO (SCROLL) PARA LA LISTA DE GRUPOS
        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // CREACIÓN Y DISEÑO DEL BOTÓN FLOTANTE "+" PARA AGREGAR NUEVOS GRUPOS
        btnAgregar = new JButton("+");
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnAgregar.setBackground(new Color(140, 85, 255)); 
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // EVENTO DEL BOTÓN AGREGAR: ABRE LA VENTANA DE CREACIÓN DE NUEVOS GRUPOS
        btnAgregar.addActionListener(e -> {
            NewGroupView ventanaCrear = new NewGroupView(listaDeGrupos, GroupsView.this);
            ventanaCrear.setVisible(true);
        });

        // PANEL INFERIOR PARA ALINEAR EL BOTÓN DE AGREGAR A LA DERECHA
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        panelInferior.setOpaque(false);
        panelInferior.setBorder(new EmptyBorder(10, 20, 20, 20));
        panelInferior.add(btnAgregar);
        getContentPane().add(panelInferior, BorderLayout.SOUTH);

        // CENTRA LA VENTANA PRINCIPAL EN LA PANTALLA
        setLocationRelativeTo(null);
    }

    // MÉTODO ENCARGADO DE REFRESCAR Y REDIBUJAR LA LISTA DE GRUPOS EN LA INTERFAZ
    public void construirListaGrafica() {
        panelLista.removeAll();
        reiniciarLayoutBase();

        // RECORRE LA LISTA DE MODELOS PARA GENERAR CADA FILA VISUAL
        for (Group g : listaDeGrupos) {
            this.inyectarContenedorGrupo(g);
        }

        CerrarYConstruirLayoutInterno();
        panelLista.revalidate();
        panelLista.repaint();
    }

    // REINICIALIZA EL COMPORTAMIENTO Y LAS REGLAS DEL GROUP LAYOUT
    private void reiniciarLayoutBase() {
        layoutLista = new GroupLayout(panelLista);
        panelLista.setLayout(layoutLista);
        grupoHorizontalPrincipal = layoutLista.createParallelGroup(GroupLayout.Alignment.LEADING);
        grupoVerticalPrincipal = layoutLista.createSequentialGroup();
    }

    // CREA E INYECTA VISUALMENTE LA FILA CONTENEDORA DE UN GRUPO ESPECÍFICO
    private void inyectarContenedorGrupo(Group grupo) {
        // PANEL PRINCIPAL DE LA FILA CON DISEÑO HORIZONTAL
        JPanel panelFila = new JPanel(new BorderLayout());
        panelFila.setBackground(new Color(10, 20, 52)); 
        panelFila.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // CONTENEDOR INTERNO VERTICAL PARA TEXTOS (NOMBRE Y MIEMBROS)
        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setOpaque(false);

        // BOTÓN CON EL NOMBRE DEL GRUPO (ACTÚA COMO ENLACE AL CHAT)
        JButton btnNombreGrupo = new JButton(grupo.getTitle());
        btnNombreGrupo.setForeground(Color.WHITE);
        btnNombreGrupo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnNombreGrupo.setContentAreaFilled(false); 
        btnNombreGrupo.setBorderPainted(false); 
        btnNombreGrupo.setFocusPainted(false); 
        btnNombreGrupo.setHorizontalAlignment(SwingConstants.LEADING); 
        btnNombreGrupo.setBorder(BorderFactory.createEmptyBorder()); 
        btnNombreGrupo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNombreGrupo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // EVENTO DEL NOMBRE: ABRE LA VENTANA DEL CHAT DEL GRUPO SELECCIONADO
        btnNombreGrupo.addActionListener(e -> {
            GroupChatView ventanaChat = new GroupChatView(grupo);
            ventanaChat.setVisible(true);
        });

        // ETIQUETA QUE MUESTRA LA CANTIDAD DE MIEMBROS DEL GRUPO
        int cantidadUsuarios = grupo.getUsers() != null ? grupo.getUsers().size() : 0;
        JLabel lblSubtitulo = new JLabel(cantidadUsuarios + " miembros en este grupo");
        lblSubtitulo.setForeground(new Color(160, 175, 210)); 
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // SE AGREGAN LOS COMPONENTES DE TEXTO AL CONTENEDOR VERTICAL
        panelTextos.add(btnNombreGrupo);
        panelTextos.add(lblSubtitulo);

        // BOTÓN DE TRES PUNTOS EN EL EXTREMO DERECHO PARA VER OPCIONES/DETALLES
        JButton btnOpciones = new JButton("•••");
        btnOpciones.setForeground(Color.WHITE);
        btnOpciones.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnOpciones.setContentAreaFilled(false);
        btnOpciones.setBorderPainted(false);
        btnOpciones.setFocusPainted(false);
        btnOpciones.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // EVENTO DEL BOTÓN OPCIONES: ABRE LA VENTANA DE INTEGRANTES DEL GRUPO
        btnOpciones.addActionListener(e -> {
            PersonsInGroup ventanaDetalle = new PersonsInGroup(grupo);
            ventanaDetalle.setVisible(true);
        });

        // DISTRIBUCIÓN DE COMPONENTES DENTRO DEL PANEL DE LA FILA
        panelFila.add(panelTextos, BorderLayout.CENTER);
        panelFila.add(btnOpciones, BorderLayout.EAST);

        // ASIGNACIÓN DE LAS REGLAS DE POSICIONAMIENTO HORIZONTAL EN EL GROUP LAYOUT
        grupoHorizontalPrincipal.addGroup(layoutLista.createSequentialGroup()
            .addGap(20)
            .addComponent(panelFila, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(20)
        );
        
        // ASIGNACIÓN DE LAS REGLAS DE POSICIONAMIENTO VERTICAL EN EL GROUP LAYOUT
        grupoVerticalPrincipal.addGap(12).addComponent(panelFila, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    // APLICA FINALMENTE LAS ESTRUCTURAS HORIZONTALES Y VERTICALES AL PANEL DE LA LISTA
    private void CerrarYConstruirLayoutInterno() {
        layoutLista.setHorizontalGroup(grupoHorizontalPrincipal);
        layoutLista.setVerticalGroup(grupoVerticalPrincipal.addGap(15)); 
    }

    // MÉTODO AUXILIAR PARA GENERAR Y ESTILIZAR LOS BOTONES DE LAS PESTAÑAS SUPERIORES
    private JButton crearBotonTab(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(75, 110, 255));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        return btn;
    }

    // MÉTODO DE ENTRADA PRINCIPAL PARA COLOCAR EN MARCHA LA APLICACIÓN EN EL HILO DE SWING
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GroupsView().setVisible(true));
    }
}
