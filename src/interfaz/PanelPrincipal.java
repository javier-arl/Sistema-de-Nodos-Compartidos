package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PanelPrincipal extends JPanel {

    private JButton btnSubir, btnDescargar, btnEliminar;
    private JScrollPane spTabla;
    private JTable tabla;

    public PanelPrincipal() {
        this.setLayout(new GridBagLayout());
        inicializarComponentes();
        agregarComponentes();
        agregarEstilo();
        ComponentListener cl = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                int anchoTabla = tabla.getWidth();
                tabla.getColumnModel().getColumn(0).setPreferredWidth((int) (anchoTabla * .6));
                tabla.getColumnModel().getColumn(1).setPreferredWidth((int) (anchoTabla * .2));
                tabla.getColumnModel().getColumn(2).setPreferredWidth((int) (anchoTabla * .2));
            }
        };
        spTabla.addComponentListener(cl);
    }

    //Crea e inicializa todos los componentes de la interfaz
    private void inicializarComponentes() {
        //Iniciar Botones
        this.btnSubir = new JButton("Subir");
        this.btnDescargar = new JButton("Descargar");
        this.btnEliminar = new JButton("Eliminar");
        //Iniciar Tabla
        String[] cabecera = {"Nombre", "Fecha", "Tamanio"};
        DefaultTableModel dtm = new DefaultTableModel(null, cabecera) {
            //Hacemos que el contenido de una Celda no sea Editable
            @Override
            public boolean isCellEditable(int filas, int columnas) {
                return false;
            }
        };
        tabla = new JTable(dtm);
        tabla.getTableHeader().setReorderingAllowed(false); //Evitar que se puedan mover las columnas
        tabla.setRowHeight(25); //Altura de cada renglon
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); //Auto redimensionar las columnas
        spTabla = new JScrollPane(tabla); //Agregar al ScrollPanel la Tabla
        spTabla.setPreferredSize(new Dimension(0, 0));

    }

    //Agrega en ser necesario en paneles los componentes y los posiciona en el Panel Principal
    private void agregarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        //Agregar Boton Subir
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.gridx = 0; //Columna
        gbc.gridy = 0; //Fila
        gbc.gridwidth = 1; //1 casillas a lo ancho
        gbc.gridheight = 1; //1 casilla a lo alto
        this.add(btnSubir, gbc);
        //Agregar Botn Descargar
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0; //Columna
        gbc.gridy = 1; //Fila
        gbc.gridwidth = 1; //1 casillas a lo ancho
        gbc.gridheight = 1; //1 casilla a lo alto
        this.add(btnDescargar, gbc);
        //Agregar Boton eliminar
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0; //Columna
        gbc.gridy = 2; //Fila
        gbc.gridwidth = 1; //1 casillas a lo ancho
        gbc.gridheight = 1; //1 casilla a lo alto
        this.add(btnEliminar, gbc);
        //Agregar Tabla
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 1; //Columna
        gbc.gridy = 0; //Fila
        gbc.gridwidth = 1; //2 casillas a lo ancho
        gbc.gridheight = 3; //1 casilla a lo alto
        this.add(spTabla, gbc);

    }

    //Cambio el color y letra
    private void agregarEstilo() {
        DefaultTableCellRenderer Alinear = new DefaultTableCellRenderer();
        Font fuenteBoton = new Font("Yu Gothic UI Light", 1, 18);
        Color colorPanel = new Color(222, 225, 238  );
        this.setBackground(colorPanel);
        
        this.btnSubir.setFont(fuenteBoton);
        this.btnDescargar.setFont(fuenteBoton);
        this.btnEliminar.setFont(fuenteBoton);
        
        //Personalizar Tabla
        Alinear.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(1).setCellRenderer(Alinear);
        tabla.getColumnModel().getColumn(2).setCellRenderer(Alinear);
        tabla.getTableHeader().setFont(new Font("Consolas", 1, 14));
        tabla.setFont(new Font("Arial", 0, 15));
    }

    //Meto que agrega un ActionListener a los componentes del Panel
    public void activarActionListener(ActionListener evento) {
        this.btnSubir.addActionListener(evento);
        this.btnDescargar.addActionListener(evento);
        this.btnEliminar.addActionListener(evento);
    }

    public JButton getBtnSubir() {
        return btnSubir;
    }

    public JButton getBtnDescargar() {
        return btnDescargar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JTable getTabla() {
        return tabla;
    }

}
