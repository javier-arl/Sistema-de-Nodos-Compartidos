package codigo;

import interfaz.Ventana;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JFileChooser;

public class Main {

	public static ArrayList<String[]> getListaNodos(String nombreArchivo) {
		ArrayList<String[]> nodos = new ArrayList();
		String token, IP, puerto;
		File archivo = new File(nombreArchivo);
		if (archivo.exists()) {
			try (Scanner entrada = new Scanner(archivo)) {
				while (entrada.hasNextLine()) { //Mientras existe una linea para leer
					IP = entrada.next();
					puerto = entrada.next();
					nodos.add(new String[]{IP, puerto});
				}
				entrada.close();
			} catch (FileNotFoundException ex) {

			}
		} else {
			System.out.println("El archivo no existe");
		}
		return nodos;
	}

	public static void main(String[] args) {
		final int puerto = 8003;
		File archivoNodos = new File("listaNodos.txt");
		JFileChooser seleccionar = new JFileChooser();
		final File directorio, descargas;
		final ArrayList<String[]> nodos;
		if (!archivoNodos.exists()) {
			System.out.println("Se necesita el Archivo donde contenga la IP y Puerto de cada Maquina");
		} else {
			nodos = getListaNodos("listaNodos.txt"); //Obtener la Lista de Nodos
			seleccionar.setCurrentDirectory(new File("."));
			seleccionar.setDialogTitle("Seleccionar el Directorio donde se almacenaran los archivos");
			seleccionar.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //Solo seleccionar Directorios
			seleccionar.setAcceptAllFileFilterUsed(false);
			if (seleccionar.showDialog(null, "Almacenamiento") == JFileChooser.APPROVE_OPTION) {
				directorio = seleccionar.getSelectedFile();
			} else {
				directorio = new File(".");
			}
			seleccionar.setCurrentDirectory(new File("."));
			seleccionar.setDialogTitle("Seleccionar el Directorio donde se almacenaran los archivos descargados");
			seleccionar.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //Solo seleccionar Directorios
			seleccionar.setAcceptAllFileFilterUsed(false);
			if (seleccionar.showDialog(null, "Descargas") == JFileChooser.APPROVE_OPTION) {
				descargas = seleccionar.getSelectedFile();
			} else {
				descargas = new File(".");
			}
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						new Ventana(puerto, directorio, descargas, nodos).setVisible(true);
					} catch (IOException ex) {
						//Error al momento de crear un Servidor
						//No tiene caso la instancia de una ventana si no puede recibir Solicitudes de otras Maquinas
					}
				}
			});
		}

//        ArrayList<String[]> nodos = new ArrayList();
//        File directorio = new File("Directorio3");
//        File descargas = new File("Descargas3");
//        String IP = "127.0.0.1";
//        int puerto = 8003;
//        nodos.add(new String[]{"127.0.0.1", "8001"});
//        nodos.add(new String[]{"127.0.0.1", "8002"});
//        nodos.add(new String[]{"127.0.0.1", "8003"});
//
	}
}
