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
import java.util.List;


// CLASE PRINCIPAL QUE GENERA LA VENTANA PARA CREAR UN NUEVO GRUPO
public class NewGroupView extends JFrame {
    private JPanel panelLista;
    private JTextField txtNombreGrupo;
    private JButton btnCrear;

    // REFERENCIAS DE CONEXIÓN CON LA VISTA PADRE Y ESTRUCTURAS COMPARTIDAS
    private ArrayList<GroupsView.MockGroup> listaGruposReferencia;
    private GroupsView vistaPadreReferencia;
    private ArrayList<FilaSeleccion> listadoFilasGraficas = new ArrayList<>();

    // CONSTRUCTOR PARAMETRIZADO: ENLAZA CON EL PADRE Y RECOGE LA LISTA ORIGINAL
    public NewGroupView(ArrayList<GroupsView.MockGroup> listaOriginal, GroupsView padre) {
        this.listaGruposReferencia = listaOriginal;
        this.vistaPadreReferencia = padre;

        // CONFIGURACIÓN DE PROPIEDADES DE LA VENTANA
        setTitle("Crear Grupo");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);

        // CREA EL CONTENEDOR PRINCIPAL CON DISEÑO DE BORDES Y MÁRGENES
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(new Color(45, 60, 120));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // CREA EL PANEL SUPERIOR CON ALINEACIÓN EN EJE VERTICAL Y
        JPanel panelSuperior = new JPanel();
        panelSuperior.setOpaque(false);
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));

        // CREA LA ETIQUETA DE TEXTO PARA IDENTIFICAR EL CAMPO NOMBRE
        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(lblNombre);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5))); 

        // CREA EL CAMPO DE TEXTO DONDE SE DIGITA EL NOMBRE DEL GRUPO
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

        // CREA LA ETIQUETA DE SECCIÓN PARA EL LISTADO DE USUARIOS
        JLabel lblTodos = new JLabel("Todos");
        lblTodos.setForeground(Color.WHITE);
        lblTodos.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(lblTodos);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5)));

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // CREA EL PANEL INTERNO QUE CONTENDRÁ LAS FILAS DE USUARIOS
        panelLista = new JPanel();
        panelLista.setBackground(new Color(25, 35, 80)); 
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));

        // CREA EL CONTENEDOR CON SCROLLBAR PARA EL PANEL DE USUARIOS
        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // CREA EL PANEL INFERIOR PARA ALINEAR EL BOTÓN A LA RECTAGUARDIA
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        panelInferior.setOpaque(false);
        
        // CREA EL BOTÓN ENCARGADO DE DISPARAR LA CREACIÓN DEL GRUPO
        btnCrear = new JButton("Crear");
        btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCrear.setBackground(new Color(140, 85, 255)); 
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFocusPainted(false);
        btnCrear.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); 
        panelInferior.add(btnCrear);
        
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);

        // BORRA CUALQUIER ELEMENTO RESIDUAL ANTES DE CARGAR LA UI
        this.limpiarLista();

        // INSERCIÓN DE DATOS DE PRUEBA: LISTADO DE USUARIOS SELECCIONABLES
        ArrayList<GroupsView.MockUser> listaUsuarios = new ArrayList<>();
        listaUsuarios.add(new GroupsView.MockUser(1, "Carlos Mendoza", "carlos@mail.com", true, new Color(82, 113, 255)));
        listaUsuarios.add(new GroupsView.MockUser(2, "Ana Gómez", "ana@mail.com", true, new Color(141, 75, 255)));
        listaUsuarios.add(new GroupsView.MockUser(3, "Adro Adro", "proyecto@mail.com", false, new Color(56, 182, 255)));
        listaUsuarios.add(new GroupsView.MockUser(4, "David Silva", "david@mail.com", false, new Color(200, 80, 80)));
        listaUsuarios.add(new GroupsView.MockUser(5, "Elena Rostova", "elena@mail.com", false, new Color(80, 180, 100)));

        // RECORRE LA LISTA DE USUARIOS PARA INYECTARLOS EN LA INTERFAZ
        for (GroupsView.MockUser usuario : listaUsuarios) {
            this.agregarFilaSeleccionUI(usuario);
        }

        // CONFIGURA EL ESCUCHADOR DE ACCIÓN DEL BOTÓN CREAR
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombreGrupo = txtNombreGrupo.getText().trim();

                // EVALÚA QUE EL CAMPO DE TEXTO NO SE ENCUENTRE VACÍO
                if (!nombreGrupo.isEmpty()) {
                    ArrayList<GroupsView.MockUser> seleccionados = new ArrayList<>();

                    // COMPRUEBA CUÁLES FILAS SELECCIONÓ EL USUARIO EN LA PANTALLA
                    for (FilaSeleccion f : listadoFilasGraficas) {
                        if (f.isSeleccionado()) {
                            seleccionados.add(f.getUsuarioAsociado());
                        }
                    }

                    // CALCULA EL ID Y CONSTRUYE LA NUEVA ENTIDAD MOCKGROUP
                    int nuevoId = 100 + listaGruposReferencia.size() + 1;
                    GroupsView.MockGroup nuevoGrupo = new GroupsView.MockGroup(nuevoId, nombreGrupo, seleccionados);
                    listaGruposReferencia.add(nuevoGrupo);

                    // SOLICITA A LA VISTA PADRE REFRESCAR Y VOLVER A DIBUJAR SU INTERFAZ
                    vistaPadreReferencia.construirListaGrafica();

                    // CIERRA Y LIBERA LOS RECURSOS DE LA VENTANA ACTUAL
                    NewGroupView.this.dispose();
                } else {
                    // DESPLIEGA EN PANTALLA UN DIÁLOGO DE ADVERTENCIA DE CAMPO REQUERIDO
                    JOptionPane.showMessageDialog(NewGroupView.this, "Debes ingresar el nombre del grupo.");
                }
            }
        });

        // ACTUALIZA GRÁFICAMENTE EL LIENZO DEL PANEL
        this.refrescarInterfaz();
    }
    
    // MÉTODO: VACÍA EL PANEL GRÁFICO Y LIMPIA EL VECTOR DE CONTROL DE FILAS
    public void limpiarLista() {
        panelLista.removeAll();
        listadoFilasGraficas.clear();
    }

    // MÉTODO: INSTANCIA UNA FILA DE SELECCIÓN Y LA ADHIERE AL PANEL CONTENEDOR
    public void agregarFilaSeleccionUI(GroupsView.MockUser usuario) {
        // ASIGNA COLOR SEGÚN EL ESTADO DE CONEXIÓN DEL USUARIO
        Color colorEstado = usuario.isConnected() ? new Color(15, 50, 200) : new Color(165, 210, 255); 

        FilaSeleccion fila = new FilaSeleccion(usuario, colorEstado);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        
        panelLista.add(fila);
        panelLista.add(Box.createRigidArea(new Dimension(0, 5))); 
        
        // GUARDA LA FILA EN EL ARRAYLIST GLOBAL PARA LEER SU ESTADO DE SELECCIÓN POSTERIORMENTE
        listadoFilasGraficas.add(fila);
    }

    // MÉTODO: FUERZA EL REFRESCO DE DIBUJO Y ACTUALIZACIÓN INTERNA DEL PANEL DE LA LISTA
    public void refrescarInterfaz() {
        panelLista.revalidate();
        panelLista.repaint();
    }

    // MÉTODOS GETTERS DE COMPONENTES INTERNOS de la interfaz
    public JButton getBtnCrear() { return btnCrear; }
    public JTextField getTxtNombreGrupo() { return txtNombreGrupo; }
    public JPanel getPanelLista() { return panelLista; }
}

// CLASE SECUNDARIA: GENERA EL PANEL INDIVIDUAL (FILA) PARA CADA USUARIO
class FilaSeleccion extends JPanel {
    private JPanel cuadroIzquierdo;
    private JButton btnDerecho;
    private boolean seleccionado = false;
    
    // ENTIDAD DE CONTROL: CONECTA EL PANEL CON EL OBJETO DE DATOS INTERNO
    private GroupsView.MockUser usuarioAsociado;

    // CONSTRUCTOR DEL PANEL FILA: ACOMODA LOS ELEMENTOS CON UN GROUPLAYOUT
    public FilaSeleccion(GroupsView.MockUser usuario, Color colorIzquierdo) {
        this.usuarioAsociado = usuario;
        
        // CONFIGURA EL COLOR DE FONDO OSCURO Y LOS MÁRGENES DE LA FILA
        setBackground(new Color(10, 20, 52)); 
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // CREA UN CUADRO MINIATURA COMO INDICADOR DE COLOR IZQUIERDO
        cuadroIzquierdo = new JPanel();
        cuadroIzquierdo.setBackground(colorIzquierdo);
        cuadroIzquierdo.setPreferredSize(new Dimension(40, 40));

        // CREA EL TEXTO GRÁFICO CON EL NOMBRE COMPLETO DEL USUARIO
        JLabel lblName = new JLabel(usuario.getUserName());
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // CREA EL BOTÓN CUADRADO DERECHO QUE FUNCIONA COMO CHECKBOX INTERACTIVO
        btnDerecho = new JButton();
        btnDerecho.setBackground(new Color(55, 180, 255)); 
        btnDerecho.setPreferredSize(new Dimension(35, 35));
        btnDerecho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDerecho.setFocusPainted(false);
        btnDerecho.setBorder(BorderFactory.createEmptyBorder());

        // CONFIGURA EL ESCUCHADOR DE ACCIÓN DEL BOTÓN CHECKBOX DERECHO
        btnDerecho.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // CONMUTA EL VALOR DEL ESTADO LÓGICO DE SELECCIÓN
                seleccionado = !seleccionado;
                
                // CAMBIA EL COLOR DEL BOTÓN DEPENDIENDO DE SI ESTÁ SELECCIONADO O NO
                if (seleccionado) {
                    btnDerecho.setBackground(new Color(40, 180, 120)); 
                } else {
                    btnDerecho.setBackground(new Color(55, 180, 255)); 
                }
            }
        });

        // INICIALIZA EL ESQUEMA DE DISEÑO DE TIPO GROUPLAYOUT PARA ESTA COMPONENTE
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        // DEFINE LA ESTRUCTURA Y EL DISTANCIAMIENTO EN SENTIDO HORIZONTAL
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(cuadroIzquierdo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(15) 
                .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDerecho, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        // DEFINE LA ESTRUCTURA Y EL ALINEAMIENTO CENTRADO EN SENTIDO VERTICAL
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(cuadroIzquierdo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblName)
                .addComponent(btnDerecho, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }

    // MÉTODOS GETTERS PARA CONSULTAR COMPONENTES Y ATRIBUTOS DESDE FUERA
    public JButton getBtnDerecho() { return btnDerecho; }
    public boolean isSeleccionado() { return seleccionado; }
    public GroupsView.MockUser getUsuarioAsociado() { return usuarioAsociado; }
}
