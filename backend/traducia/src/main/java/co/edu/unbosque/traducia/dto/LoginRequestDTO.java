package co.edu.unbosque.traducia.dto;

/**
 * DTO (Data Transfer Object) utilizado para representar los datos enviados por
 * el cliente durante el proceso de autenticación de un usuario.
 * <p>
 * Contiene el nombre de usuario y la contraseña necesarios para validar las
 * credenciales de acceso al sistema.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class LoginRequestDTO {

	/**
	 * Nombre del usuario que solicita autenticarse.
	 */
	private String nombre;

	/**
	 * Contraseña proporcionada para validar las credenciales del usuario.
	 */
	private String password;

	/**
	 * Obtiene el nombre del usuario.
	 *
	 * @return nombre del usuario que solicita autenticarse.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Establece el nombre del usuario.
	 *
	 * @param nombre nombre del usuario que se utilizará para la autenticación.
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Obtiene la contraseña proporcionada por el usuario.
	 *
	 * @return contraseña utilizada durante la autenticación.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece la contraseña del usuario.
	 *
	 * @param password contraseña que se utilizará para validar las credenciales.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Construye un objeto LoginRequestDTO con las credenciales del usuario.
	 *
	 * @param nombre   nombre del usuario que solicita autenticarse.
	 * @param password contraseña proporcionada para la autenticación.
	 */
	public LoginRequestDTO(String nombre, String password) {
		this.nombre = nombre;
		this.password = password;
	}

	/**
	 * Construye un objeto LoginRequestDTO sin inicializar sus atributos.
	 */
	public LoginRequestDTO() {
	}
}
