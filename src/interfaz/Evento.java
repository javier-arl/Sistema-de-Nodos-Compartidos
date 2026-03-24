package interfaz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Evento implements ActionListener {

    private PanelPrincipal panel; //Panel Principal de la Ventana
    private JFileChooser seleccionar; //File Chooser para elegir el Archivo a subir
    private File archivo; //Archivo seleccionado con el File Chooser
    private JTable tabla; //Tabla que muestra los Archivos en la Interfaz
    private DefaultTableModel dtm; //Realizar las Operaciones tanto para Descargar como Eliminar
    private ArrayList<String[]> nodos; //Lista de Nodos <IP> <Puerto>
    private int apuntadorNodo; //Apunta a la posicion del Nodo dentro de la lista, quien es seleccionado para enviar el Archivo
    private Ventana ventana; //Ventana el cual tendra este Evento
    private DataOutputStream out; //Flujo de Salida de Datos para enviar a traves del Socket
    private DataInputStream in; //Flujo de Entrada de Datos para leer a traves del socket
    private BufferedOutputStream bufOut; //Flujo de Salida de Bytes tanto para escribir en un Archivo como Enviarlos a traves del Socket
    private BufferedInputStream bis; //Flujo de Entrada de Bytes tanto para leer bytes de un Archivo como para leerlos a traves del Socket
    private Socket sc; //Socket que tiene la conexion con el Nodo al cual se realiza la peticion, ya sea subir, descargar o eliminar
    private byte[] byteArray, receivedData; //Arreglos de Bytes que se envia en el Socket y recibirlos tambien
    private int num; //Numero de Bytes leidos dentro del Archivo

    public Evento(Ventana ventana, ArrayList<String[]> nodos) {
        this.ventana = ventana;
        this.panel = ventana.getPanel();
        this.nodos = nodos;
        this.seleccionar = new JFileChooser();
        this.tabla = panel.getTabla();
        this.dtm = (DefaultTableModel) this.tabla.getModel();
        this.apuntadorNodo = 0;
    }
    /**
     * msjActualizarTabla : OP_NombreArchivo_Fecha_Tam msjActualizarLista :
     * NombreArchivo_IndiceNodoOrigen_IndiceNodoCopia
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        long startTime, endTime;
        if (e.getSource() == this.panel.getBtnSubir()) { //Subir un Archivo al Almacenamiento de Archivos compartidos
            String msjActualizarTabla, msjActualizarLista = "", nodo[];
            double tam;
            int indice, indice2;
            if (seleccionar.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                archivo = seleccionar.getSelectedFile();
                if (archivo.canExecute()) { //Validar que se pueda ejecutar el Archivo
                    DateTimeFormatter dtf4 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                    startTime = System.nanoTime();
                    msjActualizarTabla = "1_"; //Concatenar OP
                    msjActualizarTabla += archivo.getName().concat("_"); //Concatenar el Nombre
                    msjActualizarTabla += dtf4.format(LocalDateTime.now()).concat("_"); //Concatenar la Fecha
                    msjActualizarTabla += String.valueOf(archivo.length()); //Concatenar el Tam
                    indice = this.apuntadorNodo; //Obtener al Nodo a quien lo vamos a mandar
                    indice2 = indice;
                    for (int i = 0; i < 2; i++) { //Se manda el Original y la Copia, se hace 2 veces el mismo procedimiento
                        try {
                            bis = new BufferedInputStream(new FileInputStream(archivo)); //Preparar el Archivo para leer sus Bytes
                            sc = new Socket(this.nodos.get(indice)[0], Integer.valueOf(this.nodos.get(indice)[1]));//Se raliza una Conexion al Servidor
                            out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
                            out.writeInt(1); //Le enviamos la Op al Servidor
                            out.writeInt((int) archivo.length()); //Enviar Longitud del Archivo
                            out.writeUTF(archivo.getName()); //Enviar el Nombre del Archivo
                            bufOut = new BufferedOutputStream(sc.getOutputStream());
                            byteArray = new byte[8192];
                            //Proceso de Envio de Bytes del Archivo al Nodo:
                            while ((num = bis.read(byteArray)) != -1) { //Leer Bytes del Archivo
                                bufOut.write(byteArray, 0, num); //Enviar los Bytes leidos al Socket
                            }
                            bis.close();
                            bufOut.close();
                            indice = this.getNextNodo(); //Siguiente nodo, es decir quien tendra la Copia
                        } catch (IOException ex) {
                            System.out.println("No se Logro Conectar con el Nodo");
                        } catch (IllegalArgumentException ex){
                            JOptionPane.showMessageDialog(null, "Se escribio un Argumento ilegal en la Lista de Nodos", "Error Lista Nodos", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    //Armar el Mensaje para Actualizar la Lista
                    msjActualizarLista = archivo.getName().concat("_");
                    msjActualizarLista += String.valueOf(indice2).concat("_");
                    msjActualizarLista += String.valueOf(indice);
                    this.actualizarArchivo(msjActualizarTabla, msjActualizarLista);

                    this.enviarActualizarNodo();
                    endTime = System.nanoTime();
                    System.out.println("Archivo Enviado: " + (endTime - startTime));
                    JOptionPane.showMessageDialog(null, "Archivo subido correctamente");
                }
            }
        } else if (e.getSource() == this.panel.getBtnDescargar()) { //Descargar un Archivo
            if (this.panel.getTabla().getSelectedRow() != -1) {
                //Obtener el nombre del Archivo Seleccionado
                String nombre = (String) this.dtm.getValueAt(this.tabla.getSelectedRow(), 0);
                int numError = 0, tam;
                int indice = this.getPosNodoArchivoOrigen(nombre); //Obtener el Indice del Archivo dentro de la Lista de Archivos
                //Obtener el indice del Nodo quien tiene el Archivo Original dentro de la Lista de Nodos
                int nodoO = Integer.valueOf(this.ventana.getServidor().getListaArchivos().get(indice)[1]);
                startTime = System.nanoTime();
                while (numError < 2) {
                    try {
                        sc = new Socket(this.nodos.get(nodoO)[0], Integer.valueOf(this.nodos.get(nodoO)[1]));//Se raliza una Conexion al Servidor
                        out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
                        in = new DataInputStream(sc.getInputStream());
                        out.writeInt(2); //Le enviamos la Op al Servidor
                        out.writeUTF(nombre);
                        tam = in.readInt();
                        receivedData = new byte[1024];
                        archivo = new File(this.ventana.getDescargas(), nombre);
                        bufOut = new BufferedOutputStream(new FileOutputStream(archivo)); //Preparar Archivo para Escribir Bytes
                        bis = new BufferedInputStream(sc.getInputStream()); //Tener  un Bufer para leer Bytes desde el Socket
                        while ((num = bis.read(receivedData)) != -1) {
                            bufOut.write(receivedData, 0, num);
                        }
                        bufOut.close();
                        in.close();
                        break;
                    } catch (IOException ex) {
                        //Si no se Realiza la conexion con el Nodo quien posee el Archivo Original
                        // Se realiza una conexion con el Nodo quien tenga el Archivo Copia
                        numError++;
                        nodoO = this.getPosNodoArchivoCopia(indice);
//                        System.out.println("No se Logro Conectar con el Nodo");
                    }
                }
                endTime = System.nanoTime();
                System.out.println("Archivo Descargado: " + (endTime - startTime));
                if(numError < 2){
                    JOptionPane.showMessageDialog(null, "Archivo Descargado y almacenado en: "+this.ventana.getDescargas().getAbsolutePath());    
                }else{
                    JOptionPane.showMessageDialog(null, "Archivo no disponible", "Error Descarga", JOptionPane.ERROR_MESSAGE);
                }
                
            } else {
                System.out.println("Seleccione un renglon primero");
            }

        } else if (e.getSource() == this.panel.getBtnEliminar()) { //Eliminar Archivo
            if (this.panel.getTabla().getSelectedRow() != -1) {
                startTime = System.nanoTime();
                //Obtener el nombre del Archivo Seleccionado
                String nombre = (String) this.dtm.getValueAt(this.tabla.getSelectedRow(), 0);
                int indice = this.getPosNodoArchivoOrigen(nombre);//Obtener el Indice del Archivo dentro de la Lista de Archivos
                //Obtener el indice del Nodo quien tiene el Archivo Original dentro de la Lista de Nodos
                int nodoO = Integer.valueOf(this.ventana.getServidor().getListaArchivos().get(indice)[1]);
                int numError = 0;
                for (int i = 0; i < 2; i++) {
                    try {
                        sc = new Socket(this.nodos.get(nodoO)[0], Integer.valueOf(this.nodos.get(nodoO)[1]));//Se raliza una Conexion al Servidor
                        out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
                        out.writeInt(3); //Le enviamos la Op de Eliminar al Servidor
                        out.writeUTF(nombre);
                        out.close();
                        //Se tiene que eliminar tanto el Archivo Original como la Copia.
                        //Asi que tambien Obtenemos la Posicion del Nodo quien tiene la Copia dentro de la Lista de Nodos
                        nodoO = this.getPosNodoArchivoCopia(indice);
                    } catch (IOException ex) {
                        nodoO = this.getPosNodoArchivoCopia(indice);
                        numError++;
                        System.out.println("No se Logro Conectar con el Nodo");
                    }
                }
                //Una vez eliminado el Archivo Original y la Copia, se debe actualizar la Tabla de todos los nodos
                this.actualizarArchivoEliminado(nombre);
                endTime = System.nanoTime();
                System.out.println("Archivo Eliminado: " + (endTime - startTime));
                if(numError < 2){
                    JOptionPane.showMessageDialog(null, "Archivo Eliminado");    
                }else{
                    JOptionPane.showMessageDialog(null, "Archivo no disponible", "Error Eliminar", JOptionPane.ERROR_MESSAGE);
                }
                
            } else {
                System.out.println("Seleccione un renglon primero");
            }
        }

    }
    /**
     * Metodo que actualiza el siguiente Nodo a quien se le enviara el Archivo
     */
    public void actualizarNodo() {
        this.apuntadorNodo++;
        if (this.apuntadorNodo == this.nodos.size()) {
            this.apuntadorNodo = 0;
        }
    }

    /**
     *
     * @param indice
     */
    public void actualizarNodo(int indice) {
        this.nodos.get(indice)[0] = "0"; // El nodo se encuentra Fuera de Servicio
    }

    /**
     * Metodo que obtiene el Nodo quien tendra la Replica del Archivo
     *
     * @return
     */
    public int getNextNodo() {
        if (this.apuntadorNodo == this.nodos.size() - 1) {
            return 0;
        }
        return this.apuntadorNodo + 1;
    }

    /**
     * Metodo que envia a los otros Nodos los mensajes para Actualizar tanto la
     * tabla como la lista de archivos
     *
     * @param msjActualizarTabla Formado por: OP_Nombre_Fecha_Tam
     * @param msjActualizarLista Formado por: Nombre_Nodo1_Nodo2
     */
    public void actualizarArchivo(String msjActualizarTabla, String msjActualizarLista) {
        for (String[] nodo : nodos) {
            try {
                bis = new BufferedInputStream(new FileInputStream(archivo));
                sc = new Socket(nodo[0], Integer.valueOf(nodo[1]));//Se raliza una Conexion al Servidor
                out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
                out.writeInt(4); //Le enviamos la Op al Servidor
                out.writeUTF(msjActualizarTabla);
                out.writeUTF(msjActualizarLista);
            } catch (IOException ex) {
                System.out.println("No se Logro Conectar con el Nodo");
            }
        }
    }

    //Obtener la Posicion del Nodo (dentro de la lista de Nodos) quien tenga el Archivo Original
    /**
     * Metodo que Obtiene el indice del Archivo dentro de la Lista de Archivos,
     * que a su vez contiene el indice tanto del Nodo quien contiene el archivo
     * Original como el Nodo que contiene la replica del archivo. <br>
     * Recordando que la informacion de un archivo se guarda como: Nombre NodoO
     * NodoR Siendo O de Original y R de replica.
     *
     * @param nombre
     * @return
     */
    public int getPosNodoArchivoOrigen(String nombre) {
        int i = 0;
        for (String[] archivo : this.ventana.getServidor().getListaArchivos()) {
            if (archivo[0].equals(nombre)) {
//                return Integer.valueOf(archivo[1]);
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Metodo que obtiene mediante la posicion de un Archivo dentro de la Lista
     * de Archivos, el indice del Nodo quien tiene la Replica del Archivo
     *
     * @param indice
     * @return
     */
    public int getPosNodoArchivoCopia(int indice) {
        return Integer.valueOf(this.ventana.getServidor().getListaArchivos().get(indice)[2]);
    }

    /**
     * Metodo que actualiza el Nodo proximo a ser elegido para enviar el Archivo
     * Original
     */
    public void enviarActualizarNodo() {
        for (String[] nodo : nodos) {
            try {
                bis = new BufferedInputStream(new FileInputStream(archivo));
                sc = new Socket(nodo[0], Integer.valueOf(nodo[1]));//Se raliza una Conexion al Servidor
                out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
                out.writeInt(6); //Le enviamos la Op 6 para actualizar el Nodo
                out.close();
            } catch (IOException ex) {
                System.out.println("No se Logro Conectar con el Nodo");
            }
        }
    }

    /**
     * Metodo que actualiza el Archivo una vez que fue eliminado, para esto le
     * informa a los demas Nodos que deberan eliminar la informacion almacenado
     * hasta el momento del archivo almacenado con este nombre.
     *
     * @param nombre
     */
    public void actualizarArchivoEliminado(String nombre) {
        for (String[] nodo : nodos) {
            try {
                bis = new BufferedInputStream(new FileInputStream(archivo));
                sc = new Socket(nodo[0], Integer.valueOf(nodo[1]));//Se raliza una Conexion al Servidor
                out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
                out.writeInt(5); //Le enviamos la Op al Servidor
                out.writeUTF(nombre);
                out.close();
            } catch (IOException ex) {
                System.out.println("No se Logro Conectar con el Nodo");
            }
        }
    }

}
