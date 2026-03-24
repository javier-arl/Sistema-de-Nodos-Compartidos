# DistributedFileSync

Sistema distribuido que simula un administrador de archivos compartido en Java utilizando Sockets bajo un modelo Cliente-Servidor múltiple. Los nodos pueden conectarse entre sí, compartir archivos y mantener sincronizado su contenido.

## Descripción

Este proyecto implementa una simulación de un sistema distribuido donde varias máquinas pueden compartir archivos en una red local.

Cada nodo funciona como Cliente y Servidor al mismo tiempo, permitiendo:

- Conectarse a otros nodos mediante sockets
- Compartir archivos
- Replicar archivos automáticamente
- Notificar cambios a las demás máquinas
- Descargar archivos desde otros nodos
- Mantener sincronizado el almacenamiento

El sistema fue probado localmente utilizando múltiples puertos en la misma computadora para simular diferentes máquinas en red.

Para la configuración de los nodos, el sistema utiliza un archivo de texto llamado:

```
listaNodos.txt
```

Este archivo contiene una lista de puertos de todos los nodos del sistema, por ejemplo:

```
8000
8001
8002
8003
```

De esta forma, cada instancia del programa se inicializa con la lista de nodos y puede conectarse a ellos sin necesidad de ingrear la infromación manualmente.

## Funcionamiento

- Cada nodo lee el archivo `listaNodos.txt`
- Obtiene la lista de puertos disponibles
- Se conecta a los demás nodos
- Cuando un nodo sube un archivo:
  - Se notifica a los demás nodos
  - Los demás nodos reciben el archivo
  - Cada nodo lo guarda en su carpeta local
- Cuando un nodo descarga un archivo:
  - Solo se almacena en su propio directorio

Cada máquina tiene su propia carpeta de almacenamiento y descarga seleccionada por el usuario.

## Características

- Comunicación mediante Sockets TCP
- Arquitectura Cliente-Servidor distribuida
- Replicación automática de archivos
- Sincronización entre nodos
- Configuración mediante archivo `listaNodos.txt`
- Simulación de red distribuida en local
- Selección de carpeta de almacenamiento
- Soporte para múltiples nodos

## Tecnologías usadas

- Java
- Java Sockets
- NetBeans 29
- TCP/IP
- Programación Cliente-Servidor
- Sistemas Distribuidos

## Cómo ejecutar

1. Abrir el archivo en NetBeans
2. Verificar que el archivo `listaNodos.txt` contenga los puertos, se puede modificar para agregar más puertos.
3. Ejecutar múltiples instancias del programa cambiando el número del puerto en el main.
4. Cada instancia usará un puerto diferente
5. Seleccionar la carpeta de almacenamiento
6. Selecconar la carpeta de descarga
7. Subir o descargar archivos

## Objetivo

El objetivo del proyecto es demostrar el funcionamiento de:

- Comunicación por sockets
- Sistemas distribuidos
- Sincronización de archivos
- Replicación de datos
- Arquitectura cliente-servidor múltiple
- Configuración de nodos mediante archivo
