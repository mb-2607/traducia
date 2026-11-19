package co.edu.unbosque.traducia.entity;

/**
 * Estado del resultado de una traducción.
 *
 * {@code EXITOSA}: el proveedor devolvió un texto. {@code FALLIDA}: el proveedor lanzó un error.
 */
public enum EstadoTraduccion {
	EXITOSA, FALLIDA;
}

