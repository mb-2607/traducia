package co.edu.unbosque.traducia.dto;

/**
 * DTO utilizado para solicitar el reenvío del correo de verificación de cuenta.
 */
public class ResendVerificationRequestDTO {

	/**
	 * Dirección de correo electrónico asociada a la cuenta pendiente de verificación.
	 */
	private String correo;

	/**
	 * Constructor por defecto.
	 */
	public ResendVerificationRequestDTO() {
	}

	/**
	 * Construye la solicitud con el correo indicado.
	 *
	 * @param correo dirección de correo electrónico
	 */
	public ResendVerificationRequestDTO(String correo) {
		this.correo = correo;
	}

	/**
	 * Obtiene el correo electrónico.
	 *
	 * @return dirección de correo electrónico
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * Establece el correo electrónico.
	 *
	 * @param correo dirección de correo electrónico
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
	}
}