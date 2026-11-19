package co.edu.unbosque.traducia.dto;

/**
 * DTO (Data Transfer Object) utilizado para representar la respuesta del
 * proceso de autenticación de un usuario.
 * <p>
 * Contiene la información básica del usuario autenticado y el token JWT
 * generado para gestionar su autenticación y autorización dentro del sistema.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class LoginResponseDTO {

	/**
	 * Información del usuario autenticado.
	 */
	private UserResponseDTO userResponse;

	/**
	 * Token JWT generado durante el proceso de autenticación.
	 */
	private String jwt;

	/**
	 * Obtiene la información del usuario autenticado.
	 *
	 * @return objeto con los datos del usuario.
	 */
	public UserResponseDTO getUserResponse() {
		return userResponse;
	}

	/**
	 * Establece la información del usuario autenticado.
	 *
	 * @param userResponse objeto que contiene los datos del usuario.
	 */
	public void setUserResponse(UserResponseDTO userResponse) {
		this.userResponse = userResponse;
	}

	/**
	 * Obtiene el token JWT del usuario autenticado.
	 *
	 * @return token JWT generado durante la autenticación.
	 */
	public String getJwt() {
		return jwt;
	}

	/**
	 * Establece el token JWT del usuario autenticado.
	 *
	 * @param jwt token JWT que se asignará.
	 */
	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	/**
	 * Construye un objeto LoginResponseDTO con la información del usuario
	 * autenticado y su correspondiente token JWT.
	 *
	 * @param userResponse información del usuario autenticado.
	 * @param jwt          token JWT generado durante el proceso de autenticación.
	 */
	public LoginResponseDTO(UserResponseDTO userResponse, String jwt) {
		this.userResponse = userResponse;
		this.jwt = jwt;
	}

	/**
	 * Construye un objeto LoginResponseDTO sin inicializar sus atributos.
	 */
	public LoginResponseDTO() {
	}
}
