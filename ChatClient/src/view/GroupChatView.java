package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// CLASE PRINCIPAL QUE GENERA LA VENTANA DE CHAT DE GRUPO
public class GroupChatView extends JFrame {

    // VARIABLES DE CONFIGURACIÓN PARA EL LAYOUT DINÁMICO DEL CHAT
    private GroupLayout layoutChat;
    private GroupLayout.SequentialGroup grupoVerticalPrincipal;
    private GroupLayout.ParallelGroup grupoHorizontalPrincipal;

    // COMPONENTES GRÁFICOS DE LA INTERFAZ DE USUARIO
    private JTextField txtHeader;   
    private JTextField txtInput;    
    private JButton btnEnviar;      
    private JPanel panelChat;       
    private JScrollPane scrollPane;  

    // ASOCIACIÓN DE DATOS CON LA CLASE INTERNA DE GROUPSVIEW
    private GroupsView.MockGroup grupoActual;
    private int contadorMensajes = 0; 

    // CONSTRUCTOR PARAMETRIZADO: RECIBE EL MODELO DE DATOS DESDE LA VISTA DE ORIGEN
    public GroupChatView(GroupsView.MockGroup grupo) {
        this.grupoActual = grupo;

        // CONFIGURACIÓN DE PROPIEDADES DE LA VENTANA DEL CHAT
        setTitle("Chat - " + grupoActual.getTitle());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(450, 600); 
        setMinimumSize(new Dimension(400, 500)); 
        
        // ASIGNA DISPOSICIÓN DE BORDES Y COLOR DE FONDO AZUL
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(52, 73, 137)); 

        // CREA LA BARRA SUPERIOR DINÁMICA CON EL TÍTULO DEL GRUPO
        txtHeader = new JTextField(grupoActual.getTitle());
        txtHeader.setEditable(false); 
        txtHeader.setBorder(new EmptyBorder(0, 50, 0, 0)); 
        txtHeader.setBackground(new Color(11, 22, 64)); 
        txtHeader.setForeground(Color.WHITE); 
        txtHeader.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        txtHeader.setPreferredSize(new Dimension(450, 60)); 
        getContentPane().add(txtHeader, BorderLayout.NORTH);

        // CREA EL PANEL DE FONDO DONDE SE RENDERIZAN LOS MENSAJES
        panelChat = new JPanel();
        panelChat.setBackground(new Color(52, 73, 137)); 
        
        // INICIALIZA Y CONFIGURA EL LAYOUT BASE EN BLANCO
        reiniciarLayoutBase();
        CerrarYConstruirLayoutInterno();

        // CREA EL CONTENEDOR CON SCROLLBAR PARA EL PANEL DE CHAT
        scrollPane = new JScrollPane(panelChat);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // CREA EL CAMPO DE TEXTO PARA QUE EL USUARIO ESCRIBA
        txtInput = new JTextField("");
        txtInput.setBackground(new Color(11, 23, 62));
        txtInput.setForeground(Color.WHITE);
        txtInput.setCaretColor(Color.WHITE); 
        txtInput.setBorder(new EmptyBorder(10, 20, 10, 10)); 
        txtInput.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));

        // CREA EL BOTÓN CON EL SÍMBOLO MAYOR QUE PARA EL ENVÍO
        btnEnviar = new JButton(">");
        btnEnviar.setBackground(new Color(11, 23, 62));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        btnEnviar.setFocusPainted(false); 
        btnEnviar.setBorder(null);

        // CREA EL PANEL INFERIOR PARA INTEGRAR CAMPO DE TEXTO Y BOTÓN
        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        panelInferior.setBackground(new Color(52, 73, 137));
        panelInferior.setBorder(new EmptyBorder(15, 20, 20, 20)); 
        panelInferior.add(txtInput, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
        btnEnviar.setPreferredSize(new Dimension(50, 45)); 
        getContentPane().add(panelInferior, BorderLayout.SOUTH);

        // CONFIGURA EL ESCUCHADOR DE ACCIÓN AL HACER CLICK EN ENVIAR
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarNuevoMensaje();
            }
        });
        
        // CONFIGURA EL ESCUCHADOR DE ACCIÓN AL PRESIONAR ENTER EN EL CAMPO
        txtInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarNuevoMensaje();
            }
        });

        // CENTRA LA VENTANA EN EL MEDIO DE LA PANTALLA
        setLocationRelativeTo(null);
    }

    // MÉTODO: INSTANCIA EL GESTOR GROUPLAYOUT Y CREA LOS GRUPOS DIRECCIONALES
    private void reiniciarLayoutBase() {
        layoutChat = new GroupLayout(panelChat);
        panelChat.setLayout(layoutChat);

        grupoHorizontalPrincipal = layoutChat.createParallelGroup(GroupLayout.Alignment.LEADING);
        grupoVerticalPrincipal = layoutChat.createSequentialGroup();
    }

    // MÉTODO: VALIDA EL TEXTO EXTRAE EL USUARIO MOCK E INYECTA LA BURBUJA EN EL CHAT
    private void procesarNuevoMensaje() {
        String texto = txtInput.getText().trim(); 
        
        if (!texto.isEmpty() && grupoActual.getUsers() != null && !grupoActual.getUsers().isEmpty()) {
            // CALCULA CUÁL USUARIO ENVIARÁ EL MENSAJE SEGÚN EL TURNO DEL CONTADOR
            int indiceUsuario = contadorMensajes % grupoActual.getUsers().size();
            
            // EXTRAE EL USUARIO ANIDADO DE LA CLASE GENERAL GROUPSVIEW
            GroupsView.MockUser usuarioAsociado = grupoActual.getUsers().get(indiceUsuario);

            // LLAMA AL GENERADOR GRÁFICO DE MENSAJES PASANDO SUS ATRIBUTOS
            inyectarComponentesMensaje(usuarioAsociado.getUserName(), texto, usuarioAsociado.getUserColor());
            
            // RECONSTRUYE LA INTERFAZ Y LIMPIA EL CAMPO DE TEXTO
            CerrarYConstruirLayoutInterno();
            txtInput.setText("");
            contadorMensajes++;
            
            // FUERZA EL REFRESCO VISUAL COMPLETO DEL PANEL DE CHAT
            panelChat.revalidate();
            panelChat.repaint();

            // DESPLAZA AUTOMÁTICAMENTE EL SCROLLBAR HACIA LA PARTE INFERIOR
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }
    }

    // MÉTODO: CREA LAS BURBUJAS DE TEXTO Y LAS ACOMODA DENTRO DEL LAYOUT DE MANERA SECUENCIAL
    private void inyectarComponentesMensaje(String usuario, String mensaje, Color colorBurbuja) {
        // CREA LA ETIQUETA CON EL NOMBRE DEL REMITENTE
        JLabel lblUser = new JLabel(usuario);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));

        // CREA EL ÁREA DE TEXTO CON AJUSTE AUTOMÁTICO DE LÍNEA PARA EL MENSAJE
        JTextArea txtMensaje = new JTextArea(mensaje);
        txtMensaje.setBackground(colorBurbuja);
        txtMensaje.setForeground(Color.WHITE);
        txtMensaje.setEditable(false);
        txtMensaje.setLineWrap(true);       
        txtMensaje.setWrapStyleWord(true);   
        txtMensaje.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 15));
        txtMensaje.setBorder(new EmptyBorder(8, 10, 8, 10)); 

        // DEFINE LAS DISTANCIAS Y ANCHOS DENTRO DEL ESQUEMA HORIZONTAL
        grupoHorizontalPrincipal.addGroup(layoutChat.createSequentialGroup()
            .addGap(20) 
            .addGroup(layoutChat.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(lblUser, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addComponent(txtMensaje, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, 250))
            .addGap(20) 
        );

        // DEFINE LAS DISTANCIAS Y ALINEACIONES EN EL ESQUEMA VERTICAL SECUENCIAL
        grupoVerticalPrincipal.addGap(15) 
            .addComponent(lblUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED) 
            .addComponent(txtMensaje, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    // MÉTODO: CIERRA LA CONFIGURACIÓN Y ESTABLECE EL ESQUEMA FINAL AL LAYOUT
    private void CerrarYConstruirLayoutInterno() {
        layoutChat.setHorizontalGroup(grupoHorizontalPrincipal);
        layoutChat.setVerticalGroup(grupoVerticalPrincipal.addGap(15));
    }
}