package co.edu.unbosque.traducia.service;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.dto.TraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;

@Service
public class TraduccionService {
	
	private UserService userService;
	
	public TraduccionService(UserService userService) {
		this.userService = userService;
	}



	public TraduccionResponseDTO traducir(TraduccionRequestDTO traduccion, String nombreUsuario) {
		UserResponseDTO usuario = userService.obtenerPorNombre(nombreUsuario);
		validarCaracteresEnTexto(traduccion, usuario.getPlanId());
		
		return null;
	}
	
	public void validarCaracteresEnTexto(TraduccionRequestDTO traduccion, PlanType plan) {
		if(traduccion.getTexto().length() > plan.getNumeroCaracterPorSolicitud()) {
			throw new PlanCharacterLimitExceededException();
		}
	}
}
