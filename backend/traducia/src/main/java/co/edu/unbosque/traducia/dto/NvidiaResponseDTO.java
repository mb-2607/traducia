package co.edu.unbosque.traducia.dto;

import java.util.List;

public class NvidiaResponseDTO {
	private List<NvidiaChoiceResponseDTO> choices;

	public NvidiaResponseDTO() {
	}

	public NvidiaResponseDTO(List<NvidiaChoiceResponseDTO> choices) {
		this.choices = choices;
	}

	public List<NvidiaChoiceResponseDTO> getChoices() {
		return choices;
	}

	public void setChoices(List<NvidiaChoiceResponseDTO> choices) {
		this.choices = choices;
	}

	public String obtenerContenido() {
		return choices.get(0).getMessage().getContent();
	}
}
