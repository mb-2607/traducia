package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) utilizado para representar los datos enviados por
 * el cliente durante la actualización de la información de un usuario.
 * <p>
 * Contiene los atributos que pueden modificarse, incluyendo el nombre, correo
 * electrónico, rol y plan asociado al usuario.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class UpdateUserRequestDTO {

	/**
	 * Nombre que se asignará al usuario.
	 */
	@NotBlank(message = "El nombre de usuario es obligatorio")
	private String nombre;

	/**
	 * Dirección de correo electrónico que se asignará al usuario.
	 */
	@NotBlank(message = "El correo electrónico es obligatorio")
	@Email(message = "El formato del correo electrónico no es válido")
	private String correo;

	/**
	 * Rol que se asignará al usuario dentro del sistema.
	 */
	@NotNull(message = "El rol es obligatorio")
	private Role rol;

	/**
	 * Tipo de plan que se asociará al usuario.
	 */
	@NotNull(message = "El tipo de plan es obligatorio")
	private PlanType planId;

	/**
	 * Construye un objeto UpdateUserRequestDTO sin inicializar sus atributos.
	 */
	public UpdateUserRequestDTO() {
	}

	/**
	 * Construye un objeto UpdateUserRequestDTO con los datos que se desean
	 * establecer durante la actualización de un usuario.
	 *
	 * @param nombre nombre que se asignará al usuario.
	 * @param correo dirección de correo electrónico que se asignará al usuario.
	 * @param rol    rol que se asignará al usuario dentro del sistema.
	 * @param planId tipo de plan que se asociará al usuario.
	 */
	public UpdateUserRequestDTO(String nombre, String correo, Role rol, PlanType planId) {
		this.nombre = nombre;
		this.correo = correo;
		this.rol = rol;
		this.planId = planId;
	}

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
	 * Obtiene el rol asignado al usuario.
	 *
	 * @return rol del usuario.
	 */
	public Role getRol() {
		return rol;
	}

	/**
	 * Establece el rol del usuario.
	 *
	 * @param rol rol que se asignará al usuario.
	 */
	public void setRol(Role rol) {
		this.rol = rol;
	}

	/**
	 * Obtiene el tipo de plan asociado al usuario.
	 *
	 * @return tipo de plan del usuario.
	 */
	public PlanType getPlanId() {
		return planId;
	}

	/**
	 * Establece el tipo de plan asociado al usuario.
	 *
	 * @param planId tipo de plan que se asociará al usuario.
	 */
	public void setPlanId(PlanType planId) {
		this.planId = planId;
	}
}