package co.edu.unbosque.traducia.dto;

/**
 * Objeto de transferencia de datos (DTO) utilizado para completar el
 * restablecimiento de contraseña mediante un token de recuperación válido.
 *
 * @author
 * @version 1.0
 */

public class ResetPasswordRequestDTO {
	/**
	 * Token único recibido por el usuario en el enlace de recuperación.
	 */
	private String token;

	/**
	 * Nueva contraseña que el usuario desea establecer en su cuenta.
	 */
	private String nuevaPassword;

	/**
	 * Constructor por defecto requerido para la deserialización del objeto.
	 */
	public ResetPasswordRequestDTO() {
	}

	/**
	 * Crea una nueva instancia del DTO con el token y la nueva contraseña.
	 *
	 * @param token token de recuperación de contraseña.
	 * @param nuevaPassword nueva contraseña a establecer.
	 */
	public ResetPasswordRequestDTO(String token, String nuevaPassword) {
		this.token = token;
		this.nuevaPassword = nuevaPassword;
	}

	/**
	 * Obtiene el token de recuperación enviado en la solicitud.
	 *
	 * @return token de recuperación.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Establece el token de recuperación.
	 *
	 * @param token token de recuperación.
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Obtiene la nueva contraseña enviada en la solicitud.
	 *
	 * @return nueva contraseña del usuario.
	 */
	public String getNuevaPassword() {
		return nuevaPassword;
	}

	/**
	 * Establece la nueva contraseña que se asignará al usuario.
	 *
	 * @param nuevaPassword nueva contraseña del usuario.
	 */
	public void setNuevaPassword(String nuevaPassword) {
		this.nuevaPassword = nuevaPassword;
	}

}
