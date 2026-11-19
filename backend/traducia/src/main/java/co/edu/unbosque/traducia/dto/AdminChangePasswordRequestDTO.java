package co.edu.unbosque.traducia.dto;

/**
 * Objeto de transferencia de datos (DTO) utilizado para las solicitudes de cambio
 * de contraseña realizadas por un administrador sobre la cuenta de un usuario.
 * <p>
 * Contiene únicamente la nueva contraseña que será asignada al usuario objetivo.
 *
 * @author
 * @version 1.0
 */


public class AdminChangePasswordRequestDTO {
	
	/**
	 * Nueva contraseña que el administrador desea establecer para el usuario.
	 */
	
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

