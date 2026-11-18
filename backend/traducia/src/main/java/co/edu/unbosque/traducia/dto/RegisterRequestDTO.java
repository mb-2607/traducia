package co.edu.unbosque.traducia.dto;

/**
 * DTO (Data Transfer Object) utilizado para representar los datos enviados por
 * el cliente durante el proceso de registro de un nuevo usuario.
 * <p>
 * Contiene la información necesaria para crear una cuenta en el sistema,
 * incluyendo el nombre, correo electrónico y contraseña del usuario.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class RegisterRequestDTO {

	/**
	 * Nombre del usuario que desea registrarse.
	 */
	private String nombre;

	/**
	 * Dirección de correo electrónico del usuario.
	 */
	private String correo;

	/**
	 * Contraseña proporcionada para crear la cuenta del usuario.
	 */
	private String password;

	/**
	 * Obtiene el nombre del usuario.
	 *
	 * @return nombre del usuario.
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Establece el nombre del usuario.
	 *
	 * @param nombre nombre que se asignará al usuario.
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Obtiene el correo electrónico del usuario.
	 *
	 * @return correo electrónico del usuario.
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * Establece el correo electrónico del usuario.
	 *
	 * @param correo dirección de correo electrónico que se asignará al usuario.
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
	}

	/**
	 * Obtiene la contraseña proporcionada por el usuario.
	 *
	 * @return contraseña del usuario.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece la contraseña del usuario.
	 *
	 * @param password contraseña que se utilizará para crear la cuenta.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Construye un objeto RegisterRequestDTO con los datos necesarios para
	 * registrar un nuevo usuario.
	 *
	 * @param nombre   nombre del usuario que desea registrarse.
	 * @param correo   dirección de correo electrónico del usuario.
	 * @param password contraseña proporcionada para crear la cuenta.
	 */
	public RegisterRequestDTO(String nombre, String correo, String password) {
		this.nombre = nombre;
		this.correo = correo;
		this.password = password;
	}

	/**
	 * Construye un objeto RegisterRequestDTO sin inicializar sus atributos.
	 */
	public RegisterRequestDTO() {
	}
}
