package co.edu.unbosque.traducia.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad que almacena un token temporal para verificar el correo electrónico de un usuario.
 */
@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {

	/**
	 * Identificador único del token.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Código único enviado en el enlace de verificación.
	 */
	@Column(nullable = false, unique = true)
	private String token;

	/**
	 * Usuario asociado a la solicitud de verificación.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * Fecha y hora límite de vigencia del token.
	 */
	@Column(nullable = false)
	private LocalDateTime fechaExpiracion;

	/**
	 * Indica si el token ya fue utilizado.
	 */
	@Column(nullable = false)
	private boolean usado;

	/**
	 * Constructor por defecto requerido por JPA.
	 */
	public EmailVerificationToken() {
	}

	/**
	 * Crea un token de verificación activo para un usuario.
	 *
	 * @param token código único del enlace
	 * @param user usuario dueño de la cuenta
	 * @param fechaExpiracion fecha y hora de vencimiento
	 */
	public EmailVerificationToken(String token, User user, LocalDateTime fechaExpiracion) {
		this.token = token;
		this.user = user;
		this.fechaExpiracion = fechaExpiracion;
		this.usado = false;
	}

	/**
	 * Comprueba si la fecha actual ya superó la expiración del token.
	 *
	 * @return true si el token está vencido, false en caso contrario
	 */
	public boolean estaExpirado() {
		return LocalDateTime.now().isAfter(this.fechaExpiracion);
	}

	/**
	 * Obtiene el identificador del token.
	 *
	 * @return id del registro
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador del token.
	 *
	 * @param id identificador del registro
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el código del token.
	 *
	 * @return cadena del token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Establece el código del token.
	 *
	 * @param token cadena del token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Obtiene el usuario dueño del token.
	 *
	 * @return usuario asociado
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Establece el usuario dueño del token.
	 *
	 * @param user usuario asociado
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Obtiene la fecha de expiración del token.
	 *
	 * @return fecha y hora de vencimiento
	 */
	public LocalDateTime getFechaExpiracion() {
		return fechaExpiracion;
	}

	/**
	 * Establece la fecha de expiración del token.
	 *
	 * @param fechaExpiracion fecha y hora de vencimiento
	 */
	public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
		this.fechaExpiracion = fechaExpiracion;
	}

	/**
	 * Indica si el token ya fue usado.
	 *
	 * @return true si ya se usó, false si sigue disponible
	 */
	public boolean isUsado() {
		return usado;
	}

	/**
	 * Cambia el estado de uso del token.
	 *
	 * @param usado nuevo estado de uso
	 */
	public void setUsado(boolean usado) {
		this.usado = usado;
	}
}