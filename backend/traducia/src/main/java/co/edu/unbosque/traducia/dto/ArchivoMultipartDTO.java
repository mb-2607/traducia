package co.edu.unbosque.traducia.dto;

import java.util.Objects;

/**
 * Archivo binario de una petición multipart saliente.
 *
 * @param campo nombre del campo esperado por el proveedor
 * @param nombre nombre seguro del archivo, incluida su extensión
 * @param contenido bytes del archivo; se copian para evitar modificaciones externas
 */
public record ArchivoMultipartDTO(String campo, String nombre, byte[] contenido) {

    /** Crea una copia defensiva del contenido obligatorio. */
    public ArchivoMultipartDTO {
        Objects.requireNonNull(campo, "El campo del archivo es obligatorio");
        Objects.requireNonNull(nombre, "El nombre del archivo es obligatorio");
        Objects.requireNonNull(contenido, "El contenido es obligatorio");
        contenido = contenido.clone();
    }

    /** @return copia independiente de los bytes del archivo. */
    @Override
    public byte[] contenido() {
        return contenido.clone();
    }
}
