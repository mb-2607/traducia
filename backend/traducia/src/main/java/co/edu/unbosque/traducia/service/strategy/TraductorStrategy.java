package co.edu.unbosque.traducia.service.strategy;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

public interface TraductorStrategy {
	String traducir(TraduccionRequestDTO dto);
	ProveedorTraduccion getProveedor();
}
