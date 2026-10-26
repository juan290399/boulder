package com.boulder.boulder.fotos;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dónde viven los archivos de las fotos.
 *
 * <p>
 * Es una interfaz para poder cambiar de almacén sin tocar nada más: hoy el
 * disco del servidor ({@link AlmacenFotosDisco}); cuando el volumen lo pida
 * —del orden de medio giga por sondaje—, un almacén de objetos compatible con
 * S3 como MinIO, que se puede montar en el mismo campamento.
 * </p>
 */
public interface AlmacenFotos {

    /** Guarda el contenido bajo la clave dada, reemplazando lo que hubiera. */
    void guardar(String clave, byte[] contenido) throws IOException;

    /** Abre el contenido para leerlo. Quien llama cierra el flujo. */
    InputStream abrir(String clave) throws IOException;

    boolean existe(String clave);

    void borrar(String clave) throws IOException;
}
