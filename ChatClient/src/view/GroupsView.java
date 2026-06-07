package view;

/**
 *
 * @author adotal
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

// CLASE PRINCIPAL QUE GENERA LA VENTANA DE GRUPOS
public class GroupsView extends JFrame {

    // VARIABLES DE CONFIGURACIÓN PARA EL LAYOUT DE LA LISTA
    private GroupLayout layoutLista;
    private GroupLayout.SequentialGroup grupoVerticalPrincipal;
    private GroupLayout.ParallelGroup grupoHorizontalPrincipal;

    // COMPONENTES GRÁFICOS DE LA INTERFAZ
    private JTextField txtHeader;   
    private JPanel panelTabs;       
    private JPanel panelLista;      
    private JButton btnAgregar;      

    // LISTA EN MEMORIA QUE ALMACENA LOS GRUPOS MOCK
    private ArrayList<MockGroup> listaDeGrupos = new ArrayList<>();

    // CONSTRUCTOR DE LA CLASE: CONFIGURA LA VENTANA Y CREA LOS PANELES
    public GroupsView() {
        // CONFIGURACIÓN DE PROPIEDADES DE LA VENTANA PRINCIPAL
        setTitle("Grupos");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // ASIGNA DISPOSICIÓN DE BORDES Y COLOR DE FONDO AZUL
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(45, 60, 120));

        // CREA EL TEXTFIELD DE LA CABECERA SUPERIOR
        txtHeader = new JTextField("Grupos");
        txtHeader.setEditable(false); 
        txtHeader.setBorder(new EmptyBorder(0, 50, 0, 0)); 
        txtHeader.setBackground(new Color(11, 22, 64)); 
        txtHeader.setForeground(Color.WHITE); 
        txtHeader.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        txtHeader.setPreferredSize(new Dimension(450, 60)); 

        // CREA EL PANEL PARA LAS PESTAÑAS DE NAVEGACIÓN (TABS)
        panelTabs = new JPanel(new GridLayout(1, 3, 10, 0));
        panelTabs.setOpaque(false);
        panelTabs.setBorder(new EmptyBorder(15, 20, 5, 20)); 
        // AÑADE LOS TRES BOTONES DE PESTAÑA AL PANEL
        panelTabs.add(crearBotonTab("Todos"));
        panelTabs.add(crearBotonTab("Amigos"));
        panelTabs.add(crearBotonTab("Grupos"));

        // CREA UN PANEL CONTENEDOR PARA UNIR CABECERA Y PESTAÑAS
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);
        panelSuperior.add(txtHeader, BorderLayout.NORTH);
        panelSuperior.add(panelTabs, BorderLayout.CENTER);
        getContentPane().add(panelSuperior, BorderLayout.NORTH);

        // CREA EL PANEL DONDE SE ALBERGARÁ LA LISTA DINÁMICA
        panelLista = new JPanel();
        panelLista.setBackground(new Color(45, 60, 120)); 
        
        // INSERCIÓN DE DATOS DE PRUEBA: GRUPO DE SISTEMAS OPERATIVOS
        ArrayList<MockUser> usuariosSistemas = new ArrayList<>();
        usuariosSistemas.add(new MockUser(1, "Carlos Gomez", "carlos@mail.com", true, new Color(82, 113, 255)));
        usuariosSistemas.add(new MockUser(2, "Ana Martinez", "ana@mail.com", false, new Color(141, 75, 255)));
        usuariosSistemas.add(new MockUser(3, "Juan Perez", "juan@mail.com", true, new Color(56, 182, 255)));
        listaDeGrupos.add(new MockGroup(101, "Sistemas Operativos II", usuariosSistemas));

        // INSERCIÓN DE DATOS DE PRUEBA: GRUPO DE VIDEOJUEGOS 3D
        ArrayList<MockUser> usuariosUnity = new ArrayList<>();
        usuariosUnity.add(new MockUser(4, "Luis Hernandez", "luis@mail.com", true, new Color(82, 113, 255)));
        usuariosUnity.add(new MockUser(5, "Sofia Ruiz", "sofia@mail.com", false, new Color(141, 75, 255)));
        listaDeGrupos.add(new MockGroup(102, "Desarrollo de Videojuegos 3D", usuariosUnity));

        // INVOCACIÓN DEL MÉTODO QUE DIBUJA LOS GRUPOS EN PANTALLA
        construirListaGrafica();

        // CREA EL CONTENEDOR CON SCROLLBAR PARA DESLIZAR LA LISTA
        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // CREA EL BOTÓN FLOTANTE CON EL SIGNO MÁS
        btnAgregar = new JButton("+");
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnAgregar.setBackground(new Color(140, 85, 255)); 
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // CONFIGURA EL ESCUCHADOR DE ACCIÓN PARA EL BOTÓN DE AGREGAR
        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ABRE LA VENTANA DE CREACIÓN DE NUEVO GRUPO
                NewGroupView ventanaCrear = new NewGroupView(listaDeGrupos, GroupsView.this);
                ventanaCrear.setVisible(true);
            }
        });

        // CREA EL PANEL INFERIOR PARA ALINEAR EL BOTÓN A LA DERECHA
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        panelInferior.setOpaque(false);
        panelInferior.setBorder(new EmptyBorder(10, 20, 20, 20));
        panelInferior.add(btnAgregar);
        getContentPane().add(panelInferior, BorderLayout.SOUTH);

        // CENTRA LA VENTANA EN EL MEDIO DE LA PANTALLA
        setLocationRelativeTo(null);
    }

    // MÉTODO: RECONSTRUYE VISUALMENTE LA LISTA VACIANTE Y ITERANDO LOS GRUPOS
    public void construirListaGrafica() {
        panelLista.removeAll();
        reiniciarLayoutBase();

        for (MockGroup g : listaDeGrupos) {
            this.inyectarContenedorGrupo(g);
        }

        CerrarYConstruirLayoutInterno();
        panelLista.revalidate();
        panelLista.repaint();
    }

    // MÉTODO: INICIALIZA DESDE CERO EL ESQUEMA DEL CONFIGURADOR GROUPLAYOUT
    private void reiniciarLayoutBase() {
        layoutLista = new GroupLayout(panelLista);
        panelLista.setLayout(layoutLista);

        grupoHorizontalPrincipal = layoutLista.createParallelGroup(GroupLayout.Alignment.LEADING);
        grupoVerticalPrincipal = layoutLista.createSequentialGroup();
    }

    // MÉTODO: CONSTRUYE E INYECTA LA TARJETA GRÁFICA INDIVIDUAL DE UN GRUPO
    private void inyectarContenedorGrupo(MockGroup grupo) {
        // CREA EL PANEL FILA QUE CONTIENE LA TARJETA DEL GRUPO
        JPanel panelFila = new JPanel(new BorderLayout());
        panelFila.setBackground(new Color(10, 20, 52)); 
        panelFila.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // CREA EL PANEL PARA ACOMODAR LOS TEXTOS EN EJE VERTICAL
        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setOpaque(false);

        // CREA EL BOTÓN INTERACTIVO CON EL NOMBRE DEL GRUPO
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

        // CONFIGURA EL ESCUCHADOR DE ACCIÓN PARA EL BOTÓN NOMBRE DEL GRUPO
        btnNombreGrupo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ABRE LA VENTANA DE CHAT CORRESPONDIENTE AL GRUPO SELECCIONADO
                GroupChatView ventanaChat = new GroupChatView(grupo);
                ventanaChat.setVisible(true);
            }
        });

        // CREA LA ETIQUETA QUE INDICA EL NÚMERO DE MIEMBROS DEL GRUPO
        int cantidadUsuarios = grupo.getUsers() != null ? grupo.getUsers().size() : 0;
        JLabel lblSubtitulo = new JLabel(cantidadUsuarios + " miembros en este grupo");
        lblSubtitulo.setForeground(new Color(160, 175, 210)); 
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // AGREGA EL NOMBRE Y EL SUBTÍTULO AL PANEL DE TEXTOS
        panelTextos.add(btnNombreGrupo);
        panelTextos.add(lblSubtitulo);

        // CREA EL BOTÓN CON SÍMBOLO DE TRES PUNTOS PARA OPCIONES
        JButton btnOpciones = new JButton("•••");
        btnOpciones.setForeground(Color.WHITE);
        btnOpciones.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnOpciones.setContentAreaFilled(false);
        btnOpciones.setBorderPainted(false);
        btnOpciones.setFocusPainted(false);
        btnOpciones.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // CONFIGURA EL ESCUCHADOR DE ACCIÓN PARA EL BOTÓN DE TRES PUNTOS
        btnOpciones.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ABRE LA VENTANA QUE DETALLA LAS PERSONAS INTEGRANTES DEL GRUPO
                PersonsInGroup ventanaDetalle = new PersonsInGroup(grupo);
                ventanaDetalle.setVisible(true);
            }
        });

        // MONTA LOS TEXTOS AL CENTRO Y EL BOTÓN DE OPCIONES A LA DERECHA
        panelFila.add(panelTextos, BorderLayout.CENTER);
        panelFila.add(btnOpciones, BorderLayout.EAST);

        // ACOMODA LA TARJETA EN LA ESTRUCTURA DEL DISEÑO HORIZONTAL
        grupoHorizontalPrincipal.addGroup(layoutLista.createSequentialGroup()
            .addGap(20)
            .addComponent(panelFila, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(20)
        );

        // ACOMODA LA TARJETA EN LA ESTRUCTURA DEL DISEÑO VERTICAL
        grupoVerticalPrincipal.addGap(12).addComponent(panelFila, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    // MÉTODO: ASIGNA Y ENLAZA LAS ALINEACIONES CALCULADAS DE FORMA DEFINITIVA
    private void CerrarYConstruirLayoutInterno() {
        layoutLista.setHorizontalGroup(grupoHorizontalPrincipal);
        layoutLista.setVerticalGroup(grupoVerticalPrincipal.addGap(15)); 
    }

    // MÉTODO: FABRICA BOTONES ESTILIZADOS PARA SER USADOS COMO PESTAÑAS (TABS)
    private JButton crearBotonTab(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(75, 110, 255));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        return btn;
    }

    // CLASE INTERNA MOCK: REPRESENTA LA ENTIDAD DE UN GRUPO DE PRUEBA
    public static class MockGroup {
        private int id;
        private String title;
        private ArrayList<MockUser> users;

        public MockGroup(int id, String title, ArrayList<MockUser> users) {
            this.id = id;
            this.title = title;
            this.users = users;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public ArrayList<MockUser> getUsers() { return users; }
    }

    // CLASE INTERNA MOCK: REPRESENTA LA ENTIDAD DE UN USUARIO DE PRUEBA
    public static class MockUser {
        private int id;
        private String userName;
        private String email;
        private boolean isConnected;
        private Color userColor; 

        public MockUser(int id, String userName, String email, boolean isConnected, Color userColor) {
            this.id = id;
            this.userName = userName;
            this.email = email;
            this.isConnected = isConnected;
            this.userColor = userColor;
        }

        public int getId() { return id; }
        public String getUserName() { return userName; }
        public String getEmail() { return email; }
        public boolean isConnected() { return isConnected; }
        public Color getUserColor() { return userColor; }
    }

}
