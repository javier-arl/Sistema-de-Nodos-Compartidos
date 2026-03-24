package codigo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

//Observable ya que le mandara una Notificacion a la Ventana cuando tenga que 
//Actualizar la Lista de Nodos y la Tabla de Archivos
public class Servidor extends Observable implements Runnable {

	private ServerSocket server; //Instancia que nos permite aceptar conexiones remotas
	private Socket sc; //Socket que establece una comunicacion entre este nodo y quien haya realizado la peticion de conectarse
	private File directorio; //Es la Carpeta en donde se almacenan los Archivos Compartidos
	private ArrayList<String[]> listaArchivos; // <Nombre> <Indice Nodo Original> <Indice Nodo Copia>

	public Servidor(int puerto, File directorio) throws IOException {
		this.server = new ServerSocket(puerto);
		this.directorio = directorio;
		this.listaArchivos = new ArrayList();
	}

	@Override
	public void run() {
		String nombre, msjActualizarTabla, msjActualizarLista, nodo1, nodo2;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		DataInputStream in;
		DataOutputStream out;
		BufferedOutputStream bufOut;
		File archivo;
		int op, tam, num;
		int indice1, indice2;
		boolean salir = false;
		byte[] receivedData;
		byte[] byteArray;
		while (!salir) {
			try {
				sc = server.accept();//Se queda aqui hasta que se conecte un Cliente
				in = new DataInputStream(sc.getInputStream());
				op = in.readInt(); //Leer Op del Cliente
				switch (op) {
					// 1 a 1 -> Esta Op la realiza solo este nodo
					case 1 -> { //Almacenar el Archivo en esta Maquina
						tam = in.readInt(); //Leer el Tam del Archivo
						receivedData = new byte[1024]; //Buffered para Leer los Bytes del Socket
						nombre = in.readUTF(); //Leer el nombre del Archivo
						nombre = nombre.substring(nombre.indexOf('/') + 1, nombre.length()); //Obtener el nombre
						archivo = new File(directorio, nombre);//Crear un Archivo con el "Nombre" y dentro de "directorio"
						bos = new BufferedOutputStream(new FileOutputStream(archivo)); //Preparar Archivo para Escribir Bytes
						bis = new BufferedInputStream(sc.getInputStream()); //Tener  un Bufer para leer Bytes desde el Socket
						//Mientras se leen los Bytes del Socket, estos Bytes leidos se escriben en el Archivo Creado
						while ((num = bis.read(receivedData)) != -1) {
							bos.write(receivedData, 0, num);
						}
						bos.close();
						in.close();
					}
					// 1 a 1 -> Esta Op la realiza solo este nodo
					case 2 -> { //Descargar Archivo
						nombre = in.readUTF(); //Leer el Nombre del Archivo
						archivo = new File(directorio, nombre); //Crear un Archivo en el Directorio y nombre que se leyo
						bis = new BufferedInputStream(new FileInputStream(archivo)); //Preparar Buffered para Leer los Bytes del Archivo
						out = new DataOutputStream(sc.getOutputStream()); //Flujo de Salida de Datos
						out.writeInt((int) archivo.length()); //Enviar Longitud del Archivo
						bufOut = new BufferedOutputStream(sc.getOutputStream()); //Buffered para Enviar los Bytes que se leen del Archivo
						byteArray = new byte[8192];
						//Vamos a Enviar al mismo tiempo que leemos el Archivo
						while ((num = bis.read(byteArray)) != -1) {
							bufOut.write(byteArray, 0, num);
						}
						bis.close();
						bufOut.close();
					}
					// 1 a 1 -> Esta Op la realiza solo este nodo
					case 3 -> { //Eliminar el Archivo del Disco
						nombre = in.readUTF(); //Leer el nombre del Archivo a Eliminar
						archivo = new File(directorio, nombre); //Creamos un Archivo con el Nombre y en el Directorio
						archivo.delete(); //Eliminamos el archivo
					}
					// 1 a N -> Esta Op la realizan todos los Nodos
					case 4 -> { //Actualizar Archivos
						msjActualizarTabla = in.readUTF();
						//<OP_Nombre_Fecha_Tam>
						this.setChanged();
						this.notifyObservers(msjActualizarTabla);
						this.clearChanged();
						msjActualizarLista = in.readUTF(); //Leer el mensaje que Actualiza la Lista de Archivos
						//Extraer del mensaje el <nombre> <nodoO> <nodoC>
						indice1 = msjActualizarLista.lastIndexOf("_");
						indice2 = msjActualizarLista.lastIndexOf("_", indice1 - 1);
						nombre = msjActualizarLista.substring(0, indice2);
						nodo1 = msjActualizarLista.substring(indice2 + 1, indice1);
						nodo2 = msjActualizarLista.substring(indice1 + 1, msjActualizarLista.length());
						this.listaArchivos.add(new String[]{nombre, nodo1, nodo2});
					}
					// 1 a N -> Esta Op la realizan todos los Nodos
					case 5 -> { //Eliminar Archivo de la Lista
						nombre = in.readUTF(); //Obtener el nombre del Archivo
						indice1 = this.getPosArchivo(nombre); //Obtener la Posicion del Archivo en la Lista
						this.listaArchivos.remove(indice1); //Eliminar Archivo de la Lista de acuerdo a su posicion
						//Actualizar la Tabla de Archivos
						this.setChanged();
						this.notifyObservers(nombre); //Eliminacion a traves del Nombre del Archivo
						this.clearChanged();
					}
					// 1 a N -> Esta Op la realizan todos los Nodos
					case 6 -> {//Actualizar Nodo
						this.setChanged();
						this.notifyObservers("Actualizar Nodo");
						this.clearChanged();
					}
					case 7 -> {
						salir = true;
					}
					default -> {
//                    out.writeUTF("Solo numeros de 1-6");
					}
				}
				sc.close();
			} catch (IOException ex) {
				Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Metodo que devuelve la posicion de la Lista de Archivos quien contenga el Nombre
	 *
	 * @param nombre
	 * @return
	 */
	public int getPosArchivo(String nombre) {
		int i = 0;
		for (String[] archivo : this.listaArchivos) {
			if (archivo[0].equals(nombre)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public ArrayList<String[]> getListaArchivos() {
		return listaArchivos;
	}

	public File getDirectorio() {
		return directorio;
	}

}
