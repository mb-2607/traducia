package co.edu.unbosque.traducia.dto;

public class NvidiaMessageRequestDTO {
	private String role;
	private String content;

	public NvidiaMessageRequestDTO(String role, String content) {
		this.role = role;
		this.content = content;
	}

	public NvidiaMessageRequestDTO() {
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}