package co.edu.unbosque.traducia.dto;

public class AdminCreateUserRequestDTO extends RegisterRequestDTO{
	
	private String rol;
	private String planId;

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getPlanId() {
		return planId;
	}
	
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	
	public AdminCreateUserRequestDTO(String nombre, String correo, String password, String rol, String planId) {
		super(nombre, correo, password);
		this.rol = rol;
		this.planId = planId;
	}

	public AdminCreateUserRequestDTO() {
	
	}

}
