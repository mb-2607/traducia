package co.edu.unbosque.traducia.dto;

/**
 * Objeto de transferencia de datos (DTO) utilizado para solicitar el envío de un
 * correo de recuperación de contraseña.
 *
 * @author
 * @version 1.0
 */

public class ForgotPasswordRequestDTO {
	/**
	 * Correo electrónico registrado del usuario que solicita recuperar su contraseña.
	 */
	private String correo;

	/**
	 * Constructor por defecto requerido para la deserialización del objeto.
	 */
	public ForgotPasswordRequestDTO() {
	}

	/**
	 * Crea una nueva instancia del DTO con el correo especificado.
	 *
	 * @param correo correo electrónico del usuario.
	 */
	public ForgotPasswordRequestDTO(String correo) {
		this.correo = correo;
	}

	/**
	 * Obtiene el correo electrónico enviado en la solicitud.
	 *
	 * @return correo electrónico del usuario.
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * Establece el correo electrónico del usuario.
	 *
	 * @param correo correo electrónico del usuario.
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
	}

}
