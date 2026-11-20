package co.edu.unbosque.traducia.dto;

import java.util.List;

public class DeepLResponseDTO {
	private List<DeepLTraduccion> translations;

	public DeepLResponseDTO() {
	}

	public List<DeepLTraduccion> getTranslations() {
		return translations;
	}

	public void setTranslations(List<DeepLTraduccion> translations) {
		this.translations = translations;
	}

	public static class DeepLTraduccion {
		private String text;

		public DeepLTraduccion() {
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
}