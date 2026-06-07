/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Beetl
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// CLASE PRINCIPAL QUE GENERA LA VENTANA DE DETALLE DE INTEGRANTES DE UN GRUPO
public class PersonsInGroup extends JFrame {
    private JPanel panelLista;
    private JLabel txtHeader;

    // CONSTRUCTOR PARAMETRIZADO: RECIBE EL OBJETO GRUPO SELECCIONADO DESDE GROUPSVIEW
    public PersonsInGroup(GroupsView.MockGroup grupo) {
        // CONFIGURACIÓN DE PROPIEDADES DE LA VENTANA PRINCIPAL
        setTitle("Detalle del Grupo");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // CREA EL CONTENEDOR MAESTRO DE LA VENTANA CON DISEÑO DE BORDES Y MÁRGENES
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(new Color(45, 60, 120));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // CREA EL PANEL DE LA SECCIÓN SUPERIOR
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setOpaque(false);
        
        // CREA LA ETIQUETA TITULAR DENTRO DE LA BARRA CON EL NOMBRE DEL GRUPO
        txtHeader = new JLabel(grupo.getTitle()); 
        txtHeader.setOpaque(true);
        txtHeader.setBackground(new Color(11, 22, 64)); 
        txtHeader.setForeground(Color.WHITE);
        txtHeader.setFont(new Font("SansSerif", Font.BOLD, 28));
        txtHeader.setBorder(new EmptyBorder(15, 50, 15, 0)); 
        panelSuperior.add(txtHeader, BorderLayout.NORTH);

        // CREA EL SUBTÍTULO GRÁFICO "INTEGRANTES"
        JLabel lblIntegrantes = new JLabel("Integrantes");
        lblIntegrantes.setForeground(Color.WHITE);
        lblIntegrantes.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIntegrantes.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        panelSuperior.add(lblIntegrantes, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // CREA EL PANEL DONDE SE IRÁN ACOMODANDO LOS INTEGRANTES DE ARRIBA A ABAJO
        panelLista = new JPanel();
        panelLista.setBackground(new Color(25, 35, 80)); 
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS)); 

        // CREA EL CONTENEDOR CON SCROLLBAR PARA LA LISTA DE COMPONENTES
        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        add(panelPrincipal);

        // LIMPIA EL PANEL DE CUALQUIER RENDERIZADO PREVIO antes de inyectar datos
        this.limpiarLista();
        
        // EVALÚA SI LA LISTA DE INTEGRANTES TIENE INFORMACIÓN ASIGNADA
        if (grupo.getUsers() != null) {
            // RECORRE INDIVIDUALMENTE CADA USUARIO PERTENECIENTE AL GRUPO
            for (GroupsView.MockUser usuario : grupo.getUsers()) {
                // DETERMINA EL COLOR DEL AVATAR DEPENDIENDO DE SI ESTÁ CONECTADO O DESCONECTADO
                Color colorAvatar = usuario.isConnected() ? new Color(15, 50, 200) : new Color(165, 210, 255);
                
                // GENERA E INYECTA LA FILA GRÁFICA USANDO EL NOMBRE Y SUS COLORES EXCLUSIVOS
                this.agregarIntegranteUI(usuario.getUserName(), colorAvatar, usuario.getUserColor());
            }
        }
        
        // FUERZA LA ACTUALIZACIÓN GRÁFICA EN EL LIENZO DE LA PANTALLA
        this.refrescarInterfaz();
    }

    // MÉTODO: PERMITE MODIFICAR DINÁMICAMENTE EL TEXTO DEL TÍTULO SUPERIOR
    public void cambiarNombreGrupoUI(String nombreGrupo) {
        txtHeader.setText(nombreGrupo);
    }

    // MÉTODO: ELIMINA POR COMPLETO TODOS LOS COMPONENTES ADHERIDOS AL PANEL
    public void limpiarLista() {
        panelLista.removeAll();
    }

    // MÉTODO: FABRICA LA COMPONENTE FILAINTEGRANTE Y LA AGREGA AL CONTENEDOR VERTICAL
    public void agregarIntegranteUI(String nombre, Color colorAvatar, Color colorAccion) {
        FilaIntegrante fila = new FilaIntegrante(nombre, colorAvatar, colorAccion);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        
        panelLista.add(fila);
        panelLista.add(Box.createRigidArea(new Dimension(0, 5))); 
    }

    // MÉTODO: REESTRUCTURA E INVOCA EL REDIBUJADO DE LA INTERFAZ DE USUARIO
    public void refrescarInterfaz() {
        panelLista.revalidate();
        panelLista.repaint();
    }

    // MÉTODO GETTER: EXPONE EL PANEL DE LA LISTA DE INTEGRANTES
    public JPanel getPanelLista() {
        return panelLista;
    }
}

// CLASE SECUNDARIA: ENCARGADA DE ARQUITECTURAR EL PANEL DE FILA PARA UN INTEGRANTE
class FilaIntegrante extends JPanel {

    // CONSTRUCTOR: CREA LOS ELEMENTOS VISUALES Y LOS ORDENA USANDO GROUPLAYOUT
    public FilaIntegrante(String nombre, Color colorAvatar, Color colorAccion) {
        // ASIGNA ESTILOS BÁSICOS DE FONDO OSCURO Y MÁRGENES DE LA RECUADRO FILA
        setBackground(new Color(10, 20, 52)); 
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // CREA EL PANEL DE COLOR QUE SE UTILIZA COMO AVATAR DEL INTEGRANTE
        JPanel cuadroAvatar = new JPanel();
        cuadroAvatar.setBackground(colorAvatar);
        cuadroAvatar.setPreferredSize(new Dimension(40, 40));

        // CREA LA ETIQUETA DE TEXTO CON EL NOMBRE COMPLETO DEL INTEGRANTE
        JLabel lblName = new JLabel(nombre);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // CONFIGURACIÓN DE REGLAS BASADAS EN EL ASIGNADOR GROUPLAYOUT
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        // DECLARA LAS SECUENCIAS Y GRUPOS DIRECCIONALES DEL DISEÑO
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER);

        // ESTABLECE LAS DISTANCIAS Y ELEMENTOS INICIALES HORIZONTALES (AVATAR Y NOMBRE)
        hGroup.addComponent(cuadroAvatar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addGap(15)
              .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        // ESTABLECE LA COORDINACIÓN Y CENTRADO DE AMBOS COMPONENTES EN SENTIDO VERTICAL
        vGroup.addComponent(cuadroAvatar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addComponent(lblName);

        // CONDICIONAL DE CONTROL: EVALÚA SI SE DEBE INYECTAR EL SEGUNDO INDICADOR DE COLOR (ACCIÓN)
        if (colorAccion != null) {
            // CREA EL PANEL DE COLOR QUE HARÁ FUNCIÓN DE INDICADOR DE ACCIÓN
            JPanel cuadroAccion = new JPanel();
            cuadroAccion.setBackground(colorAccion);
            cuadroAccion.setPreferredSize(new Dimension(35, 35));
            
            // ACOMODA EL ELEMENTO EXTRAYENDO EL ESPACIO RESTANTE EN EL EXTREMO DERECHO
            hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(cuadroAccion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            
            // AGREGA EL ELEMENTO AL CONTROL VERTICAL COMPARTIDO PARA COMPACTAR LA FILA
            vGroup.addComponent(cuadroAccion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
        }

        // AMARRA LAS ESTRUCTURAS FINALES DIRECCIONALES DE MANERA OFICIAL AL LAYOUT
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
    }
}