package co.edu.unbosque.traducia.service.strategy;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

@Service
public class RivaTraductorStrategy implements TraductorStrategy {

	@Override
	public String traducir(TraduccionRequestDTO dto) {
		return "Traducciendo desde Riva";
	}

	@Override
	public ProveedorTraduccion getProveedor() {
		return ProveedorTraduccion.RIVA;
	}

}
