package co.edu.unbosque.traducia.entity;

/**
 * Proveedores de traducción disponibles.
 *
 * Cada constante indica el modelo o servicio que usa y si es tradicional o de
 * IA.
 */
public enum ProveedorTraduccion {
	RIVA("Riva-Translate:4b", TipoProveedor.IA),
	GEMMA("Gemma4:31b", TipoProveedor.IA),
	DEEPL("DeepL", TipoProveedor.TRADICIONAL),
	NEMOTRON("Nemotron3.5-lightning", TipoProveedor.IA);

	/**
	 * Nombre del modelo o servicio que usa el proveedor.
	 */
	private final String modelo;
	/**
	 * Tipo de proveedor (tradicional o de inteligencia artificial).
	 */
	private final TipoProveedor tipoProveedor;

	/**
	 * Crea la constante con sus atributos.
	 *
	 * @param modelo        nombre del modelo o servicio que usa el proveedor
	 * @param tipoProveedor tipo de proveedor (tradicional o de inteligencia
	 *                      artificial)
	 */
	private ProveedorTraduccion(String modelo, TipoProveedor tipoProveedor) {
		this.modelo = modelo;
		this.tipoProveedor = tipoProveedor;
	}

	/**
	 * Obtiene el nombre del modelo o servicio que usa el proveedor.
	 *
	 * @return nombre del modelo o servicio que usa el proveedor
	 */
	public String getModelo() {
		return modelo;
	}

	/**
	 * Obtiene el tipo de proveedor (tradicional o de inteligencia artificial).
	 *
	 * @return tipo de proveedor (tradicional o de inteligencia artificial)
	 */
	public TipoProveedor getTipoProveedor() {
		return tipoProveedor;
	}

}
