package interfaz;
import codigo.Servidor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

public class Ventana extends JFrame implements Observer {
    private PanelPrincipal panel; //Panel Principal de la Ventana
    private Evento evento; //Evento que tendra la ventana
    private Servidor servidor; //Servidor de la Ventana
    private File descargas;

    public Ventana(int puerto, File directorio, File descargas, ArrayList<String[]> nodos) throws IOException {
        super("Ventana 1");
        //Ajustamos el tamanio de la pantalla 
        Toolkit miPantalla = Toolkit.getDefaultToolkit();
        Dimension tamanoPantalla = miPantalla.getScreenSize();
        //Creacion del Servidor
        this.servidor = new Servidor(puerto, directorio); //Se crea un Servidor
        Thread hiloServidor = new Thread(servidor); //Se crea un Hilo para el Servidor
        servidor.addObserver(this); //La Ventana estara observando al Servidor cuando le llegue una solicitud
        hiloServidor.start(); //Iniciar el Hilo

        this.descargas = descargas; //Directorio donde se Guardaran las Descargas
        //Creacion de la Ventana y sus componentes
        int alturaPantalla = tamanoPantalla.height;
        int anchoPantalla = tamanoPantalla.width;
        anchoPantalla = (int) (anchoPantalla * .6); //Un 60% del ancho de la Pantalla
        alturaPantalla = (int) (alturaPantalla * .7); //Un 70% de alto de la Pantalla
        this.setSize(anchoPantalla, alturaPantalla); //Ajustar tamanio de la Ventana
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//El programa finaliza cuando sale de la ventana
        this.setLocationRelativeTo(null);//Posicionar al centro de la Pantalla

        //Creacion del Panel y Evento
        this.panel = new PanelPrincipal(); //Crear el Panel Principal de la Ventana
        this.evento = new Evento(this, nodos); //Crear un Evento que tiene la Venta y la Lista de Nodos
        this.panel.activarActionListener(evento); //Activamos los Action Listener de cada componente de la Ventana
        add(panel); //Se agrega el Panel Principal
    }

    public PanelPrincipal getPanel() {
        return panel;
    }

    public void setPanel(PanelPrincipal panel) {
        this.panel = panel;
    }

    public Servidor getServidor() {
        return servidor;
    }

    public File getDescargas() {
        return descargas;
    }

    //Notificacion del Servidor
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            String mensaje = (String) arg;
            if (mensaje.equals("Actualizar Nodo")) {
                this.evento.actualizarNodo();
            } else if (mensaje.substring(0, 1).equals("1")) { //Actualizar Tabla
                int indiceTam = mensaje.lastIndexOf("_");
                int indiceFecha = mensaje.lastIndexOf("_", indiceTam - 1);
                String nombre = mensaje.substring(2, indiceFecha);
                String fechaHora = mensaje.substring(nombre.length() + 3, indiceTam);
                String strTam = mensaje.substring(indiceTam + 1, mensaje.length());
                Object[] row = new Object[3];
                DecimalFormat formato = new DecimalFormat("###,###.##");
                row[0] = nombre;
                row[1] = fechaHora;
                row[2] = formato.format(Double.valueOf(strTam) / 1000) + " KB";
                DefaultTableModel dtm = (DefaultTableModel) this.panel.getTabla().getModel();
                dtm.addRow(row);

            } else { //Eliminar Fila -> Archivo de la tabla
                int indice = getRow(mensaje);
                DefaultTableModel dtm = (DefaultTableModel) this.panel.getTabla().getModel();
                dtm.removeRow(indice);
            }
        }
    }

    /**
     * Metodo que Obtiene el Numero de la Fila dentro de la Tabla quien contenga
     * el 'nombre'
     *
     * @param nombre Nombre del Archivo que se desea buscar dentro de la Tabla
     * @return i Numero de Fila que contiene el 'nombre' del Archivo a buscar
     */
    private int getRow(String nombre) {
        int nRow = this.panel.getTabla().getRowCount();
        DefaultTableModel dtm = (DefaultTableModel) this.panel.getTabla().getModel();
        for (int i = 0; i < nRow; i++) {
            if (dtm.getValueAt(i, 0).equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

}
