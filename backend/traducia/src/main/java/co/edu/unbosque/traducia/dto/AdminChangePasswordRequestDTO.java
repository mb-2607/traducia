package co.edu.unbosque.traducia.dto;

public class AdminChangePasswordRequestDTO {
	private String nuevaPassword;

	public AdminChangePasswordRequestDTO() {
	}

	public AdminChangePasswordRequestDTO(String nuevaPassword) {
		this.nuevaPassword = nuevaPassword;
	}

	public String getNuevaPassword() {
		return nuevaPassword;
	}

	public void setNuevaPassword(String nuevaPassword) {
		this.nuevaPassword = nuevaPassword;
	}
}

