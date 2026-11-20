package co.edu.unbosque.traducia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Objeto de transferencia de datos (DTO) utilizado para las solicitudes de cambio
 * de contraseña realizadas por un usuario autenticado sobre su propia cuenta.
 * <p>
 * Contiene la contraseña actual del usuario para validar su identidad y la nueva
 * contraseña que desea establecer.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class ChangePasswordRequestDTO {

	/**
	 * Contraseña actual del usuario requerida para autorizar el cambio.
	 */
	@NotBlank(message = "La contraseña actual es obligatoria")
	private String passwordActual;

	/**
	 * Nueva contraseña que el usuario desea establecer en su cuenta.
	 */
	@NotBlank(message = "La nueva contraseña es obligatoria")
	@Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
	private String nuevaPassword;

	/**
	 * Constructor por defecto requerido para la deserialización del objeto.
	 */
	public ChangePasswordRequestDTO() {
	}

	/**
	 * Crea una nueva instancia del DTO con la contraseña actual y la nueva contraseña especificadas.
	 *
	 * @param passwordActual contraseña actual del usuario.
	 * @param nuevaPassword nueva contraseña que se asignará al usuario.
	 */
	public ChangePasswordRequestDTO(String passwordActual, String nuevaPassword) {
		this.passwordActual = passwordActual;
		this.nuevaPassword = nuevaPassword;
	}

	/**
	 * Obtiene la contraseña actual enviada en la solicitud.
	 *
	 * @return contraseña actual del usuario.
	 */
	public String getPasswordActual() {
		return passwordActual;
	}

	/**
	 * Establece la contraseña actual del usuario.
	 *
	 * @param passwordActual contraseña actual del usuario.
	 */
	public void setPasswordActual(String passwordActual) {
		this.passwordActual = passwordActual;
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
	 * Establece la nueva contraseña que se desea asignar al usuario.
	 *
	 * @param nuevaPassword nueva contraseña del usuario.
	 */
	public void setNuevaPassword(String nuevaPassword) {
		this.nuevaPassword = nuevaPassword;
	}
}
