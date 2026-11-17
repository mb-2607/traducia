package co.edu.unbosque.traducia.dto;

public class UserAdminResponseDTO {
	private String nombre;
	private String correo;
	private String rol;
	private String planId;

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

	public UserAdminResponseDTO(String nombre, String correo, String rol, String planId) {
		this.nombre = nombre;
		this.correo = correo;
		this.rol = rol;
		this.planId = planId;
	}

	public UserAdminResponseDTO() {

	}

}
