package co.edu.unbosque.traducia.dto;

import java.util.ArrayList;
import java.util.List;

public class DeepLRequestDTO {
	private List<String> text;
	private String target_lang;

	public DeepLRequestDTO(List<String> text, String target_lang) {
		this.text = text;
		this.target_lang = target_lang;
	}
	
	

	public DeepLRequestDTO(String target_lang) {
		this.target_lang = target_lang;
		text = new ArrayList<>();
	}



	public DeepLRequestDTO() {
		text = new ArrayList<>();
	}

	public List<String> getText() {
		return text;
	}

	public void setText(List<String> text) {
		this.text = text;
	}

	public String getTarget_lang() {
		return target_lang;
	}

	public void setTarget_lang(String target_lang) {
		this.target_lang = target_lang;
	}

}
