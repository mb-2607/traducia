package co.edu.unbosque.traducia.service.strategy;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

@Service
public class DeeplTraductorStrategy implements TraductorStrategy{

	@Override
	public String traducir(TraduccionRequestDTO dto) {
		return "Tradujendo desde aquí DeepL";
	}

	@Override
	public ProveedorTraduccion getProveedor() {
		return ProveedorTraduccion.DEEPL;
	}

	

}
