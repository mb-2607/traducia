package co.edu.unbosque.traducia.dto;

public class UserResponseDTO {
	private String nombre;
	private String correo;
	private String planId;
	private String rol;

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

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public UserResponseDTO(String nombre, String correo, String planId, String rol) {
		this.nombre = nombre;
		this.correo = correo;
		this.planId = planId;
		this.rol = rol;
	}

	public UserResponseDTO() {

	}

}
