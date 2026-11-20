package co.edu.unbosque.traducia.entity;

public enum AuthenticationType {
	BEARER("Bearer"),
	DEEPL("DeepL-Auth-Key");
	
	private final String esquema;
	
	private AuthenticationType(String esquema) {
		this.esquema = esquema;
	}	
	
	public String crearFormatoLlave(String llave) {
		return esquema + " " + llave;
	}
}
