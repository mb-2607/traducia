package co.edu.unbosque.traducia.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
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
 * La idea es que implemente UserDetails para integrarse con Spring Security. Es algo que queda pendiente
 * 
 * Esta entidad se almacena en la tabla "user" en la base de datos.
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
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) long id;
	
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
	
	public User(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}
	
	public User(String username, String password, Role role) {
		this();
		this.username = username;
		this.password = password;
		this.role = role;
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public PlanType getPlanType() {
		return planType;
	}

	public void setPlanType(PlanType planType) {
		this.planType = planType;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}
	
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isDebeCambiarPassword() {
		return debeCambiarPassword;
	}

	public void setDebeCambiarPassword(boolean debeCambiarPassword) {
		this.debeCambiarPassword = debeCambiarPassword;
	}

	public long getId() {
		return id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
