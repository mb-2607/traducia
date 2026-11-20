package co.edu.unbosque.traducia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Objeto de transferencia de datos (DTO) utilizado para las solicitudes de cambio
 * de contraseña realizadas por un administrador sobre la cuenta de un usuario.
 * <p>
 * Contiene únicamente la nueva contraseña que será asignada al usuario objetivo.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class AdminChangePasswordRequestDTO {

	/**
	 * Nueva contraseña que el administrador desea establecer para el usuario.
	 */
	@NotBlank(message = "La nueva contraseña es obligatoria")
	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	private String nuevaPassword;

	/**
	 * Constructor por defecto requerido para la deserialización del objeto.
	 */
	public AdminChangePasswordRequestDTO() {
	}

	/**
	 * Crea una nueva instancia del DTO con la nueva contraseña especificada.
	 *
	 * @param nuevaPassword nueva contraseña que se asignará al usuario.
	 */
	public AdminChangePasswordRequestDTO(String nuevaPassword) {
		this.nuevaPassword = nuevaPassword;
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

