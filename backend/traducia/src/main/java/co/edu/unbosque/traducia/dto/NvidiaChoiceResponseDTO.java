package co.edu.unbosque.traducia.dto;

public class NvidiaChoiceResponseDTO {
	private NvidiaMessageResponseDTO message;

	public NvidiaChoiceResponseDTO() {
	}

	public NvidiaMessageResponseDTO getMessage() {
		return message;
	}

	public void setMessage(NvidiaMessageResponseDTO message) {
		this.message = message;
	}

	public NvidiaChoiceResponseDTO(NvidiaMessageResponseDTO message) {
		this.message = message;
	}
}
