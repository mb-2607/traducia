package co.edu.unbosque.traducia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizado para solicitar el reenvío del correo de verificación de cuenta.
 *
 * @author
 * @version 1.0
 */
public class ResendVerificationRequestDTO {

	/**
	 * Dirección de correo electrónico asociada a la cuenta pendiente de verificación.
	 */
	@NotBlank(message = "El correo electrónico es obligatorio")
	@Email(message = "El formato del correo electrónico no es válido")
	private String correo;

	/**
	 * Constructor por defecto.
	 */
	public ResendVerificationRequestDTO() {
	}

	/**
	 * Construye la solicitud con el correo indicado.
	 *
	 * @param correo dirección de correo electrónico.
	 */
	public ResendVerificationRequestDTO(String correo) {
		this.correo = correo;
	}

	/**
	 * Obtiene el correo electrónico.
	 *
	 * @return dirección de correo electrónico.
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * Establece el correo electrónico.
	 *
	 * @param correo dirección de correo electrónico.
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
	}
}