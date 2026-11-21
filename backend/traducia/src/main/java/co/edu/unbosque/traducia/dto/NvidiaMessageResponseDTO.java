package co.edu.unbosque.traducia.dto;

public class NvidiaMessageResponseDTO {
	private String content;

	public NvidiaMessageResponseDTO(String content) {
		this.content = content;
	}

	public NvidiaMessageResponseDTO() {
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}