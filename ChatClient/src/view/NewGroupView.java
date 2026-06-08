package view;

/**
 *
 * @author adotal
 */
import model.Group;
import model.UserGroup;
import model.GroupInvitation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class NewGroupView extends JFrame {
    private JPanel panelLista;
    private JTextField txtNombreGrupo;
    private JButton btnCrear;

    // REFERENCIAS DE CONEXIÓN CON LA VISTA PADRE Y ESTRUCTURAS REALES DEL MODELO
    private ArrayList<Group> listaGruposReferencia;
    private GroupsView vistaPadreReferencia;
    private ArrayList<FilaSeleccion> listadoFilasGraficas = new ArrayList<>();

    public NewGroupView(ArrayList<Group> listaOriginal, GroupsView padre) {
        this.listaGruposReferencia = listaOriginal;
        this.vistaPadreReferencia = padre;

        // CONFIGURACIÓN DE PROPIEDADES DE LA VENTANA PRINCIPAL (DIMENSIONES Y CIERRE)
        setTitle("Crear Grupo");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);

        // CREACIÓN DEL CONTENEDOR PRINCIPAL CON DISEÑO DE BORDES Y MÁRGENES PERIMETRALES
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(new Color(45, 60, 120));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // PANEL SUPERIOR CON ALINEACIÓN EN EJE VERTICAL PARA ENTRADAS DE TEXTO
        JPanel panelSuperior = new JPanel();
        panelSuperior.setOpaque(false);
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));

        // ETIQUETA DE TEXTO PARA IDENTIFICAR EL CAMPO DE ASIGNACIÓN DE NOMBRE
        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(lblNombre);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5))); 

        // CAMPO DE TEXTO DE ENTRADA DONDE SE DIGITA EL NOMBRE DEL NUEVO GRUPO
        txtNombreGrupo = new JTextField();
        txtNombreGrupo.setBackground(new Color(25, 45, 125)); 
        txtNombreGrupo.setForeground(Color.WHITE);
        txtNombreGrupo.setCaretColor(Color.WHITE); 
        txtNombreGrupo.setFont(new Font("SansSerif", Font.PLAIN, 18));
        txtNombreGrupo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(25, 45, 125), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10) 
        ));
        txtNombreGrupo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        txtNombreGrupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(txtNombreGrupo);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 15)));

        // ETIQUETA DE SECCIÓN PARA EL LISTADO GENERAL DE CONTACTOS DISPONIBLES
        JLabel lblTodos = new JLabel("Todos");
        lblTodos.setForeground(Color.WHITE);
        lblTodos.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(lblTodos);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5)));

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // PANEL INTERNO ENCARGADO DE ALBERGAR Y APILAR LAS FILAS DE USUARIOS
        panelLista = new JPanel();
        panelLista.setBackground(new Color(25, 35, 80)); 
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));

        // CONTENEDOR CON BARRA DE DESPLAZAMIENTO PARA PROTEGER EL LÍMITE DE PANTALLA
        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // PANEL INFERIOR DE DISEÑO FLUIDO EN RETAGUARDIA PARA UBICAR EL BOTÓN ACCIONADOR
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        panelInferior.setOpaque(false);
        
        // BOTÓN ENCARGADO DE DISPARAR LA RECOLECCIÓN DE DATOS Y CREACIÓN DEL GRUPO
        btnCrear = new JButton("Crear");
        btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCrear.setBackground(new Color(140, 85, 255)); 
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFocusPainted(false);
        btnCrear.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); 
        panelInferior.add(btnCrear);
        
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        add(panelPrincipal);

        // BORRA CUALQUIER COMPONENTE RESIDUAL PREVIO ANTES DE RECONSTRUIR LA INTERFAZ
        this.limpiarLista();

        // INSERCIÓN DE DATOS DE SIMULACIÓN ASOCIANDO LA ENTIDAD USERGROUP ORIGINAL
        ArrayList<UserGroup> listaUsuarios = new ArrayList<>();
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 1, "Carlos Mendoza", "carlos@mail.com", true));
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 2, "Ana Gómez", "ana@mail.com", true));
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 3, "Adro Adro", "proyecto@mail.com", false));
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 4, "David Silva", "david@mail.com", false));
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 5, "Elena Rostova", "elena@mail.com", false));

        // RECORRE LA COLECCIÓN DE INTEGRANTES DISPONIBLES PARA INYECTARLOS EN LA VISTA
        for (UserGroup usuario : listaUsuarios) {
            this.agregarFilaSeleccionUI(usuario);
        }

        // CONFIGURA EL GESTOR DE EVENTOS DE ACCIÓN AL HACER CLIC EN EL BOTÓN CREAR
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombreGrupo = txtNombreGrupo.getText().trim();

                // EVALÚA QUE EL CAMPO DE ENTRADA NO SE ENCUENTRE VACÍO
                if (!nombreGrupo.isEmpty()) {
                    ArrayList<UserGroup> seleccionados = new ArrayList<>();

                    // COMPRUEBA EL ESTADO INTERNO DE SELECCIÓN DE CADA FILA RENDERIZADA
                    for (FilaSeleccion f : listadoFilasGraficas) {
                        if (f.isSeleccionado()) {
                            seleccionados.add(f.getUsuarioAsociado());
                        }
                    }

                    // CALCULA UN IDENTIFICADOR ÚNICO VECTORIAL Y CONSTRUYE LA ENTIDAD GROUP
                    int nuevoId = 100 + listaGruposReferencia.size() + 1;
                    Group nuevoGrupo = new Group(nuevoId, nombreGrupo, seleccionados);
                    listaGruposReferencia.add(nuevoGrupo);

                    // COMUNICA A LA VISTA PADRE CONTENEDORA QUE DEBE RECONSTRUIR SU LISTA GRÁFICA
                    vistaPadreReferencia.construirListaGrafica();

                    // CONCLUYE EL CICLO DE VIDA Y LIBERA LOS RECURSOS DE LA VENTANA ACTUAL
                    NewGroupView.this.dispose();
                } else {
                    // DESPLIEGA EN PANTALLA UN DIÁLOGO EMERGENTE DE ADVERTENCIA POR CAMPO REQUERIDO
                    JOptionPane.showMessageDialog(NewGroupView.this, "Debes ingresar el nombre del grupo.");
                }
            }
        });

        // RECALCULA LAS GEOMETRÍAS DE DISEÑO Y ACTUALIZA EL LIENZO GRÁFICO DEL PANEL
        this.refrescarInterfaz();
    }
    
    // VACÍA EL PANEL CONTENEDOR Y REINICIALIZA EL VECTOR DINÁMICO DE CONTROL GRÁFICO
    public void limpiarLista() {
        panelLista.removeAll();
        listadoFilasGraficas.clear();
    }

    // INSTANCIA UNA NUEVA FILASELECCION Y LA ENGANCHA AL CONTENEDOR VERTICAL DE LA LISTA
    public void agregarFilaSeleccionUI(UserGroup usuario) {
        // DETERMINA EL COLOR DE ESTADO BASADO EN LA CONEXIÓN BOOLEANA DE LA ENTIDAD
        Color colorEstado = usuario.getIsConnected() ? new Color(15, 50, 200) : new Color(165, 210, 255); 

        FilaSeleccion fila = new FilaSeleccion(usuario, colorEstado);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        
        panelLista.add(fila);
        panelLista.add(Box.createRigidArea(new Dimension(0, 5))); 
        
        // REGISTRA LA FILA DENTRO DEL ARRAYLIST DE REFERENCIA PARA LEER SU ESTADO DE RECOLECCIÓN
        listadoFilasGraficas.add(fila);
    }

    // SOLICITA AL ADMINISTRADOR DE DISEÑO QUE REEVALÚE Y REDIBUJE EL PANEL CONTENEDOR
    public void refrescarInterfaz() {
        panelLista.revalidate();
        panelLista.repaint();
    }

    // MÉTODOS ENCAPSULADOS DE ACCESO (GETTERS) PARA LOS COMPONENTES DE LA INTERFAZ
    public JButton getBtnCrear() { return btnCrear; }
    public JTextField getTxtNombreGrupo() { return txtNombreGrupo; }
    public JPanel getPanelLista() { return panelLista; }
}

class FilaSeleccion extends JPanel {
    private JPanel cuadroIzquierdo;
    private JButton btnDerecho;
    private boolean seleccionado = false;
    
    // ENLACE DIRECTO DE CONTROL ASOCIADO AL MODELO DE DATOS USERGROUP ORIGINAL
    private UserGroup usuarioAsociado;

    // CONSTRUCTOR DEL PANEL DE FILA ENCARGADO DE MODULAR Y ACOMODAR LOS COMPONENTES CON GROUPLAYOUT
    public FilaSeleccion(UserGroup usuario, Color colorIzquierdo) {
        this.usuarioAsociado = usuario;
        
        // CONFIGURA EL TONO DE FONDO OSCURO EN BLOQUE Y LOS CONTORNOS PERIMETRALES DE LA FILA
        setBackground(new Color(10, 20, 52)); 
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // COMPONENTE EN FORMA DE MINIATURA UTILIZADO COMO INDICADOR GRÁFICO DE AVATAR
        cuadroIzquierdo = new JPanel();
        cuadroIzquierdo.setBackground(colorIzquierdo);
        cuadroIzquierdo.setPreferredSize(new Dimension(40, 40));

        // ETIQUETA CONTENEDORA ENCARGADA DE DESPLEGAR EL NOMBRE DE LA INSTANCIA DE USUARIO
        JLabel lblName = new JLabel(usuario.getName());
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // COMPONENTE BOTÓN DE ACCIÓN DERECHO CON COMPORTAMIENTO DE CHECKBOX CONMUTABLE
        btnDerecho = new JButton();
        btnDerecho.setBackground(new Color(55, 180, 255)); 
        btnDerecho.setPreferredSize(new Dimension(35, 35));
        btnDerecho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDerecho.setFocusPainted(false);
        btnDerecho.setBorder(BorderFactory.createEmptyBorder());

        // REGISTRO DEL GESTOR DE EVENTOS DE ACCIÓN ASOCIADO AL CAMBIO DE ESTADO DEL CONMUTADOR
        btnDerecho.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // INVIERTE EL VALOR LÓGICO DEL ATRIBUTO BOOLEANO DE CONTROL INTERNO
                seleccionado = !seleccionado;
                
                // ACTUALIZA DINÁMICAMENTE EL COLOR DEL COMPONENTE SEGÚN LA SELECCIÓN ACTIVA
                if (seleccionado) {
                    btnDerecho.setBackground(new Color(40, 180, 120)); 
                } else {
                    btnDerecho.setBackground(new Color(55, 180, 255)); 
                }
            }
        });

        // INICIALIZACIÓN Y CONFIGURACIÓN DE REGLAS DE POSICIONAMIENTO EN GROUP LAYOUT
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        // ESTABLECE LAS RELACIONES, DISTANCIAS Y REDIMENSIONES EN SENTIDO HORIZONTAL
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(cuadroIzquierdo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(15) 
                .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDerecho, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        // ESTABLECE LA COORDINACIÓN Y EL ALINEAMIENTO DE ALTURAS EN SENTIDO VERTICAL
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(cuadroIzquierdo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblName)
                .addComponent(btnDerecho, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }

    // MÉTODOS DE RETORNO Y CONSULTA (GETTERS) PARA LOS ATRIBUTOS OPERATIVOS DE LA FILA
    public JButton getBtnDerecho() { return btnDerecho; }
    public boolean isSeleccionado() { return seleccionado; }
    public UserGroup getUsuarioAsociado() { return usuarioAsociado; }
}
