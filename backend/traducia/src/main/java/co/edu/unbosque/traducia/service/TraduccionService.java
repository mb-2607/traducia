package co.edu.unbosque.traducia.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.dto.TraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.service.strategy.TraductorStrategyRegistry;

@Service
public class TraduccionService {
	
	private UserService userService;
	
	private TraductorStrategyRegistry tsr;

	public TraduccionService(UserService userService, TraductorStrategyRegistry tsr) {
		this.userService = userService;
		this.tsr = tsr;
	}

	public SolicitudTraduccionResponseDTO traducir(TraduccionRequestDTO traduccion, String nombreUsuario) {
		UserResponseDTO usuario = userService.obtenerPorNombre(nombreUsuario);
		validarCaracteresEnTexto(traduccion, usuario.getPlanId());
		
		List<TraduccionResponseDTO> listaTraduccion = new ArrayList<>();
		
		usuario.getPlanId().getListaProveedor().forEach((ProveedorTraduccion pt ) -> {
			listaTraduccion.add(enviarSolicitud(traduccion, pt));
		});
		
		SolicitudTraduccionResponseDTO respuesta = new SolicitudTraduccionResponseDTO();
		respuesta.setListaTraduccion(listaTraduccion);
		respuesta.setTextoOriginal(traduccion.getTexto());
		respuesta.setId(25);
		respuesta.setFechaCreacion(LocalDate.now());
		
		return respuesta;
	}
	
	private void validarCaracteresEnTexto(TraduccionRequestDTO traduccion, PlanType plan) {
		if(traduccion.getTexto().length() > plan.getNumeroCaracterPorSolicitud()) {
			throw new PlanCharacterLimitExceededException();
		}
	}
	
	private TraduccionResponseDTO enviarSolicitud(TraduccionRequestDTO traduccion, ProveedorTraduccion pt) {
		TraduccionResponseDTO respuesta = new TraduccionResponseDTO();
		respuesta.setTextoTraducido(tsr.getStrategy(pt).traducir(traduccion));
		respuesta.setOrigen(traduccion.getOrigen());
		respuesta.setDestino(traduccion.getDestino());
		respuesta.setProveedorTraduccion(pt);
		respuesta.setTipoProveedor(pt.getTipoProveedor());
		return respuesta;
	}
}
