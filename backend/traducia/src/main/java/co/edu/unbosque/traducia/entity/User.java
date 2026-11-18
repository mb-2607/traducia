package co.edu.unbosque.traducia.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Clase que representa un usuario en el sistema.
 * 
 * <p>Implementa {@link UserDetails} para integrarse con Spring Security.
 * Se almacena en la tabla "user" de la base de datos.</p>
 * 
 *
 */
@Entity
@Table(name = "user")
public class User implements UserDetails{
	
	/**
	 * Número de versión para la serialización.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Identificador único del usuario.
	 * Se genera automáticamente con estrategia de identidad.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * Nombre de usuario que es único en el sistema.
	 */
	@Column(unique = true)
	private String username;
	
	/**
	 * Correo con el que se creó el usuario. Es único en el sistema.
	 */
	@Column(unique = true)
	private String email;
	
	/**
	 * Contraseña del usuario, es hasheada al entrar al sistema.
	 */
	private String password;
	
	/**
	 * Rol del usuario en el sistema, puede ser USER o ADMIN.
	 */
	@Enumerated(EnumType.STRING)
	private Role role;
	
	/**
	 * Tipo de plan para el usuario en el sistema, actualmente hay tres:
	 * - FREE
	 * - ORIA
	 * - INFINITIA
	 */
	@Enumerated(EnumType.STRING)
	private PlanType planType;
	
	/**
	 * Representa si está disponible el usuario, o necesita validar su correo.
	 */
	private boolean enabled;
	
	/**
	 * Representa si la cuenta no está bloqueada.
	 */
	private boolean accountNonLocked = true;
	
	/**
	 * Representa el cambio obligatorio de contraseña que el admin tiene que hacer al ingresar por primera vez.
	 */
	private boolean debeCambiarPassword;
	
	/**
	 * Constructor por defecto
	 * Inicializa un usuario con los valores predeterminados:
	 * - Cuenta no bloqueada
	 * - No tiene que cambiar contraseña al entrar
	 * - Rol USER
	 * - Plan gratuito
	 */
	public User() {
		this.accountNonLocked = true;
		this.debeCambiarPassword = false;
		this.enabled = true;
		this.role = Role.USER;
		this.planType = PlanType.FREE;
	}
	
	/**
	 * Crea un usuario con el nombre de usuario y la contraseña indicados,
	 * usando los valores por defecto (rol USER, plan FREE).
	 *
	 * @param username nombre de usuario único
	 * @param password contraseña del usuario estando ya hasheada
	 */
	public User(String username, String password, String email) {
		this();
		this.username = username;
		this.password = password;
		this.email = email;
	}
	
	/**
	 * Crea un usuario con el rol indicado.
	 *
	 * @param username nombre de usuario único
	 * @param password contraseña del usuario (ya hasheada)
	 * @param role     rol asignado al usuario
	 */
	public User(String username, String password, String email ,Role role) {
		this();
		this.username = username;
		this.password = password;
		this.role = role;
		this.email = email;
	}
	
	/**
	 * Obtiene las autoridades (roles) asignadas al usuario.
	 * Implementación del método de la interfaz UserDetails.
	 * 
	 * @return Colección de autoridades del usuario con el prefijo "ROLE_"
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}
	
	/**
	 * Obtiene el correo nombre del usuario.
	 *
	 * @return nombre del usuario
	 */
	@Override
	public String getUsername() {
		return username;
	}
	
	/**
	 * Establece el nombre de usuario.
	 *
	 * @param username nuevo nombre de usuario, debe ser único
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Obtiene el correo electrónico del usuario.
	 *
	 * @return correo electrónico del usuario
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Establece el correo electrónico del usuario.
	 *
	 * @param email nuevo correo electrónico
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Obtiene el hash de la contraseña del usuario.
	 *
	 * @return contraseña ya hasheada
	 */
	@Override
	public String getPassword() {
		return password;
	}
	
	/**
	 * Establece la contraseña del usuario.
	 *
	 * @param password nueva contraseña, debe estar ya hasheada
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Obtiene el rol del usuario.
	 *
	 * @return rol del usuario
	 */
	public Role getRole() {
		return role;
	}
	
	/**
	 * Establece el rol del usuario.
	 *
	 * @param role nuevo rol del usuario
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	
	/**
	 * Obtiene el tipo de plan del usuario, si el usuario es ADMIN, devuelve null.
	 *
	 * @return tipo de plan del usuario
	 */
	public PlanType getPlanType() {
		return planType;
	}
	
	/**
	 * Establece el tipo de plan del usuario.
	 *
	 * @param planType nuevo tipo de plan
	 */
	public void setPlanType(PlanType planType) {
		this.planType = planType;
	}
	
	/**
	 * Verifica si la cuenta del usuario está habilitada, o si le falta validar el correo.
	 * Implementación del método de la interfaz UserDetails.
	 * 
	 * @return true si la cuenta está habilitada, false en caso contrario
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Establece si la cuenta del usuario está habilitada.
	 *
	 * @param enabled true para habilitar la cuenta, false para deshabilitarla
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Verifica si la cuenta del usuario no está bloqueada por determinadas circunstancias.
	 * Es una implementación del método de la interfaz UserDetails.
	 * 
	 * @return true si la cuenta no está bloqueada, false en caso contrario
	 */
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}
	
	/**
	 * Establece si la cuenta del usuario está bloqueada o no.
	 *
	 * @param accountNonLocked true si la cuenta no está bloqueada, false para bloquearla
	 */
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}
	
	/**
	 * Verifica si el usuario o administrador debe cambiar la contraseña
	 * Esto es útil sobretodo cuando una cuenta de Admin se crea por primera véz
	 * 
	 * @return true si la cuenta debe cambiar la contraseña, false en caso contrario
	 */
	public boolean isDebeCambiarPassword() {
		return debeCambiarPassword;
	}
	
	/**
	 * Establece si el usuario debe cambiar la contraseña al ingresar.
	 *
	 * @param debeCambiarPassword true si debe cambiarla, false en caso contrario
	 */
	public void setDebeCambiarPassword(boolean debeCambiarPassword) {
		this.debeCambiarPassword = debeCambiarPassword;
	}
	
	/**
	 * Obtiene el identificador único del usuario.
	 *
	 * @return identificador del usuario, o null si aún no ha sido guardado
	 */
	public Long getId() {
		return id;
	}
	
	
}
