package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;

public class AdminCreateUserRequestDTO extends RegisterRequestDTO{
	
	private Role rol;
	private PlanType planId;

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
	
	public AdminCreateUserRequestDTO(String nombre, String correo, String password, Role rol, PlanType planId) {
		super(nombre, correo, password);
		this.rol = rol;
		this.planId = planId;
	}

	public AdminCreateUserRequestDTO() {
	
	}

}
