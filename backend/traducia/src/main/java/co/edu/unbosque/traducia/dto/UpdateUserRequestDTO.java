package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;

public class UpdateUserRequestDTO {

	private String nombre;
	private String correo;
	private Role rol;
	private PlanType planId;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public Role getRol() {
		return rol;
	}

	public void setRol(Role rol) {
		this.rol = rol;
	}

	public PlanType getPlanId() {
		return planId;
	}

	public void setPlanId(PlanType planId) {
		this.planId = planId;
	}

	public UpdateUserRequestDTO(String nombre, String correo, Role rol, PlanType planId) {
		this.nombre = nombre;
		this.correo = correo;
		this.rol = rol;
		this.planId = planId;
	}

	public UpdateUserRequestDTO() {

	}

}
