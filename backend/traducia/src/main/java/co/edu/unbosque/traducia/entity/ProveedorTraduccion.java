package co.edu.unbosque.traducia.entity;

public enum ProveedorTraduccion {
	GEMINI("Gemini", TipoProveedor.IA), OPENROUTER("Openrouter", TipoProveedor.IA),
	DEEPL("DeepL", TipoProveedor.TRADICIONAL);

	private final String modelo;
	private final TipoProveedor tipoProveedor;

	private ProveedorTraduccion(String modelo, TipoProveedor tipoProveedor) {
		this.modelo = modelo;
		this.tipoProveedor = tipoProveedor;
	}

	public String getModelo() {
		return modelo;
	}

	public TipoProveedor getTipoProveedor() {
		return tipoProveedor;
	}

}
