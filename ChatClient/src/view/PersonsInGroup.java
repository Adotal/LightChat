/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Beetl
 */

import model.Group;
import model.UserGroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PersonsInGroup extends JFrame {
    
    // COMPONENTES PRINCIPALES PARA LA LISTA CONTENEDORA Y EL ENCABEZADO DE LA VISTA
    private JPanel panelLista;
    private JLabel txtHeader;

    public PersonsInGroup(Group grupo) {
        // CONFIGURACIÓN DE LAS PROPIEDADES INICIALES DE LA VENTANA (DIMENSIONES Y CIERRES)
        setTitle("Detalle del Grupo");
        setSize(450, 550);
        setMinimumSize(new Dimension(350, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // CREACIÓN DEL PANEL BASE EN CAPAS CON MÁRGENES PERIMETRALES PARA TODA LA VENTANA
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(new Color(45, 60, 120));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // CONTENEDOR EN LA SECCIÓN SUPERIOR PARA AGRUPAR TÍTULOS Y SUBTÍTULOS
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setOpaque(false);
        
        // ETIQUETA EN FORMA DE BARRA DE TÍTULO QUE DESPLIEGA EL NOMBRE DEL GRUPO SELECCIONADO
        txtHeader = new JLabel(grupo.getTitle()); 
        txtHeader.setOpaque(true);
        txtHeader.setBackground(new Color(11, 22, 64)); 
        txtHeader.setForeground(Color.WHITE);
        txtHeader.setFont(new Font("SansSerif", Font.BOLD, 28));
        txtHeader.setBorder(new EmptyBorder(15, 50, 15, 0)); 
        panelSuperior.add(txtHeader, BorderLayout.NORTH);

        // ETIQUETA INFORMATIVA QUE INDICA EL INICIO DE LA LISTA DE INTEGRANTES
        JLabel lblIntegrantes = new JLabel("Integrantes");
        lblIntegrantes.setForeground(Color.WHITE);
        lblIntegrantes.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIntegrantes.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        panelSuperior.add(lblIntegrantes, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // PANEL CENTRAL CON ALINEACIÓN EN EL EJE VERTICAL (Y) DONDE SE APILARÁN LAS FILAS
        panelLista = new JPanel();
        panelLista.setBackground(new Color(25, 35, 80)); 
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS)); 

        // CONTENEDOR CON BARRA DE DESPLAZAMIENTO PARA EVITAR DESBORDAMIENTOS GRÁFICOS
        JScrollPane scrollPane = new JScrollPane(panelLista);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        add(panelPrincipal);

        // ELIMINA CUALQUIER COMPONENTE PREVIO DEL CONTENEDOR ANTES DE VOLVER A INYECTAR DATOS
        this.limpiarLista();

        // CONTROL DE FLUJO: VERIFICA SI EL MODELO DE GRUPO CONTIENE USUARIOS ASOCIADOS
        if (grupo.getUsers() != null) {
            // RECORRE SECUENCIALMENTE LA COLECCIÓN DE MIEMBROS DE LA INSTANCIA
            for (UserGroup usuario : grupo.getUsers()) {
                
                // ASIGNACIÓN DEL COLOR DEL AVATAR SEGÚN EL ESTADO BOOLEANO DE CONEXIÓN DEL USUARIO
                Color colorAvatar = usuario.getIsConnected() ? new Color(15, 50, 200) : new Color(165, 210, 255);

                // CREACIÓN E INYECCIÓN DE LA FILA DE COMPONENTES DE FORMA DINÁMICA
                this.agregarIntegranteUI(usuario.getName(), colorAvatar);
            }
        }
        
        // FUERZA EL REFRESCO DE DIBUJO E INTERFAZ EN EL LIENZO GRÁFICO
        this.refrescarInterfaz();
    }

    // ACTUALIZA EL CONTENIDO DE TEXTO EN EL ENCABEZADO SUPERIOR DE LA VENTANA
    public void cambiarNombreGrupoUI(String nombreGrupo) {
        txtHeader.setText(nombreGrupo);
    }

    // REMUEVE LA TOTALIDAD DE LAS FILAS INSTANCIADAS DENTRO DEL CONTENEDOR DE LA LISTA
    public void limpiarLista() {
        panelLista.removeAll();
    }

    // CONSTRUYE LA INSTANCIA DE FILAINTEGRANTE Y ADICIONA UN ESPACIADO FIJO INVISIBLE ABAJO
    public void agregarIntegranteUI(String nombre, Color colorAvatar) {
        FilaIntegrante fila = new FilaIntegrante(nombre, colorAvatar);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        
        panelLista.add(fila);
        panelLista.add(Box.createRigidArea(new Dimension(0, 5))); 
    }

    // NOTIFICA AL GESTOR DE DISEÑO QUE RECALCULE LA GEOMETRÍA Y REDIBUJE LOS COMPONENTES
    public void refrescarInterfaz() {
        panelLista.revalidate();
        panelLista.repaint();
    }

    // ENCAPSULA Y EXPONE LA REFERENCIA COMPLETA DEL PANEL DE INTEGRANTES
    public JPanel getPanelLista() {
        return panelLista;
    }
}

class FilaIntegrante extends JPanel {

    // CONSTRUCTOR PARA DISEÑAR, ESTILIZAR Y ORDENAR CADA REGISTRO INDIVIDUAL DE INTEGRANTE
    public FilaIntegrante(String nombre, Color colorAvatar) {
        // ESTABLECE EL FONDO OSCURO EN BLOQUE Y LOS CONTORNOS DE MÁRGENES DE LA FILA
        setBackground(new Color(10, 20, 52)); 
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // COMPONENTE CUADRADO UTILIZADO DE MANERA ABSTRACTA COMO AVATAR DEL MIEMBRO
        JPanel cuadroAvatar = new JPanel();
        cuadroAvatar.setBackground(colorAvatar);
        cuadroAvatar.setPreferredSize(new Dimension(40, 40));

        // ETIQUETA GRÁFICA CONTENEDORA ENCARGADA DE PINTAR EL NOMBRE DEL INTEGRANTE
        JLabel lblName = new JLabel(nombre);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // CONFIGURACIÓN E INSTANCIACIÓN DE REGLAS DE GROUP LAYOUT SOBRE ESTE PANEL
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        // CREACIÓN DE LAS SECUENCIAS ESTRUCTURALES HORIZONTALES Y VERTICALES DEL GESTOR
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER);

        // DISTRIBUCIÓN HORIZONTAL: ACOMODA EL AVATAR Y EXPANDE EL TEXTO EN TODO EL ANCHO DISPONIBLE
        hGroup.addComponent(cuadroAvatar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addGap(15)
              .addComponent(lblName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        // DISTRIBUCIÓN VERTICAL: ESTABLECE LA COORDINACIÓN DEL CENTRADO ALINEADO PARA AMBOS ELEMENTOS
        vGroup.addComponent(cuadroAvatar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addComponent(lblName);

        // CONSOLIDA Y ACOPLA LAS ESTRUCTURAS HORIZONTALES Y VERTICALES FINALES AL DISEÑO
        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);
    }
}
