package view;

/**
 *
 * @author adotal
 */
import controller.NewGroupController;
import model.Group;
import model.User;
import model.UserGroup;
import model.GroupInvitation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class NewGroupView extends JFrame implements NewGroupController.View {
    private JPanel panelLista;
    private JTextField txtNombreGrupo;
    private JButton btnCrear;

    // REFERENCIAS DE CONEXIÓN CON LA VISTA PADRE Y ESTRUCTURAS REALES DEL MODELO
    private ArrayList<Group> listaGruposReferencia;
    private GroupsView vistaPadreReferencia;
    private ArrayList<FilaSeleccion> listadoFilasGraficas = new ArrayList<>();

    // Conexión real con el servidor (modo standalone desde la lista principal)
    private NewGroupController controller;

    /**
     * Constructor real: crea grupos contra el servidor cargando los usuarios
     * registrados. Se usa desde el flujo principal (UsersListView).
     */
    public NewGroupView() {
        construirUI();

        // Acción de crear contra el servidor.
        btnCrear.addActionListener(e -> {
            String nombreGrupo = txtNombreGrupo.getText().trim();
            if (nombreGrupo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debes ingresar el nombre del grupo.");
                return;
            }
            ArrayList<String> emails = new ArrayList<>();
            for (FilaSeleccion f : listadoFilasGraficas) {
                if (f.isSeleccionado()) {
                    emails.add(f.getUsuarioAsociado().getEmail());
                }
            }
            controller.createGroup(nombreGrupo, emails);
        });

        controller = new NewGroupController(this);
        controller.connect();
        controller.fetchUsers();
    }

    /**
     * Constructor original basado en datos simulados, conservado para la vista
     * GroupsView independiente.
     */
    public NewGroupView(ArrayList<Group> listaOriginal, GroupsView padre) {
        this.listaGruposReferencia = listaOriginal;
        this.vistaPadreReferencia = padre;

        construirUI();

        // INSERCIÓN DE DATOS DE SIMULACIÓN
        ArrayList<UserGroup> listaUsuarios = new ArrayList<>();
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 1, "Carlos Mendoza", "carlos@mail.com", true));
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 2, "Ana Gómez", "ana@mail.com", true));
        listaUsuarios.add(new UserGroup(new GroupInvitation(), 3, "Adro Adro", "proyecto@mail.com", false));
        for (UserGroup usuario : listaUsuarios) {
            this.agregarFilaSeleccionUI(usuario);
        }

        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombreGrupo = txtNombreGrupo.getText().trim();
                if (!nombreGrupo.isEmpty()) {
                    ArrayList<UserGroup> seleccionados = new ArrayList<>();
                    for (FilaSeleccion f : listadoFilasGraficas) {
                        if (f.isSeleccionado()) {
                            seleccionados.add(f.getUsuarioAsociado());
                        }
                    }
                    int nuevoId = 100 + listaGruposReferencia.size() + 1;
                    Group nuevoGrupo = new Group(nuevoId, nombreGrupo, seleccionados);
                    listaGruposReferencia.add(nuevoGrupo);
                    vistaPadreReferencia.construirListaGrafica();
                    NewGroupView.this.dispose();
                } else {
                    JOptionPane.showMessageDialog(NewGroupView.this, "Debes ingresar el nombre del grupo.");
                }
            }
        });

        this.refrescarInterfaz();
    }

    /** Construye la interfaz común (sin la acción del botón Crear). */
    private void construirUI() {
        setTitle("Crear Grupo");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(new Color(45, 60, 120));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel();
        panelSuperior.setOpaque(false);
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(lblNombre);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5)));

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

        JLabel lblTodos = new JLabel("Todos");
        lblTodos.setForeground(Color.WHITE);
        lblTodos.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSuperior.add(lblTodos);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5)));

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        panelLista = new JPanel();
        panelLista.setBackground(new Color(25, 35, 80));
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        panelInferior.setOpaque(false);

        btnCrear = new JButton("Crear");
        btnCrear.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCrear.setBackground(new Color(140, 85, 255));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFocusPainted(false);
        btnCrear.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        panelInferior.add(btnCrear);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        add(panelPrincipal);

        this.limpiarLista();
    }

    // ===== Callbacks de NewGroupController.View =====

    @Override
    public void onUsersForGroup(ArrayList<User> users) {
        SwingUtilities.invokeLater(() -> {
            limpiarLista();
            for (User u : users) {
                agregarFilaSeleccionUI(new UserGroup(new GroupInvitation(),
                        u.getIdUser(), u.getName(), u.getEmail(), u.getIsConnected()));
            }
            refrescarInterfaz();
        });
    }

    @Override
    public void onGroupCreated() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Grupo creado correctamente.");
            new UsersListView().setVisible(true);
            dispose();
        });
    }

    @Override
    public void onError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    // VACÍA EL PANEL CONTENEDOR Y REINICIALIZA EL VECTOR DINÁMICO DE CONTROL GRÁFICO
    public void limpiarLista() {
        panelLista.removeAll();
        listadoFilasGraficas.clear();
    }

    // INSTANCIA UNA NUEVA FILASELECCION Y LA ENGANCHA AL CONTENEDOR VERTICAL DE LA LISTA
    public void agregarFilaSeleccionUI(UserGroup usuario) {
        Color colorEstado = usuario.getIsConnected() ? new Color(15, 50, 200) : new Color(165, 210, 255);
        FilaSeleccion fila = new FilaSeleccion(usuario, colorEstado);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panelLista.add(fila);
        panelLista.add(Box.createRigidArea(new Dimension(0, 5)));
        listadoFilasGraficas.add(fila);
    }

    public void refrescarInterfaz() {
        panelLista.revalidate();
        panelLista.repaint();
    }

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

    public FilaSeleccion(UserGroup usuario, Color colorIzquierdo) {
        this.usuarioAsociado = usuario;

        setBackground(new Color(10, 20, 52));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        cuadroIzquierdo = new JPanel();
        cuadroIzquierdo.setBackground(colorIzquierdo);
        cuadroIzquierdo.setPreferredSize(new Dimension(40, 40));

        JLabel lblName = new JLabel(usuario.getName());
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));

        btnDerecho = new JButton();
        btnDerecho.setBackground(new Color(55, 180, 255));
        btnDerecho.setPreferredSize(new Dimension(35, 35));
        btnDerecho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDerecho.setFocusPainted(false);
        btnDerecho.setBorder(BorderFactory.createEmptyBorder());

        btnDerecho.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seleccionado = !seleccionado;
                if (seleccionado) {
                    btnDerecho.setBackground(new Color(40, 180, 120));
                } else {
                    btnDerecho.setBackground(new Color(55, 180, 255));
                }
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(cuadroIzquierdo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(15)
                .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDerecho, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(cuadroIzquierdo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblName)
                .addComponent(btnDerecho, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }

    public JButton getBtnDerecho() { return btnDerecho; }
    public boolean isSeleccionado() { return seleccionado; }
    public UserGroup getUsuarioAsociado() { return usuarioAsociado; }
}
