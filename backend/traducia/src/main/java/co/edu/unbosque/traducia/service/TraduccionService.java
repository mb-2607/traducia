package co.edu.unbosque.traducia.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.dto.TraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.EstadoTraduccion;
import co.edu.unbosque.traducia.entity.Idioma;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.SolicitudTraduccion;
import co.edu.unbosque.traducia.entity.Traduccion;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.IdiomaEqualsException;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.exception.TraduccionFailedException;
import co.edu.unbosque.traducia.repository.SolicitudTraduccionRepository;
import co.edu.unbosque.traducia.service.strategy.TraductorStrategyRegistry;

@Service
public class TraduccionService {

	private UserService userService;

	private TraductorStrategyRegistry tsr;

	private SolicitudTraduccionRepository soliRepo;

	private IdiomaService idiomaService;

	public TraduccionService(UserService userService, TraductorStrategyRegistry tsr,
			SolicitudTraduccionRepository soliRepo, IdiomaService idiomaService) {
		this.userService = userService;
		this.tsr = tsr;
		this.soliRepo = soliRepo;
		this.idiomaService = idiomaService;
	}

	public SolicitudTraduccionResponseDTO traducir(TraduccionRequestDTO traduccion, String nombreUsuario) {
		User usuario = userService.obtenerEntidadPorNombre(nombreUsuario);

		validarCaracteresEnTexto(traduccion, usuario.getPlanType());

		List<Traduccion> listaTraduccion = new ArrayList<>();

		Idioma origen = idiomaService.obtenerPorCodigo(traduccion.getOrigen());

		Idioma destino = idiomaService.obtenerPorCodigo(traduccion.getDestino());

		if (origen.getCodigo().equals(destino.getCodigo())) {
			throw new IdiomaEqualsException("Los idiomas son iguales");
		}

		SolicitudTraduccion solicitud = new SolicitudTraduccion();
		solicitud.setTextoOriginal(traduccion.getTexto());
		solicitud.setIdiomaDestino(destino);
		solicitud.setIdiomaOrigen(origen);
		solicitud.setUsuario(usuario);
		solicitud.setFecha(LocalDateTime.now());

		usuario.getPlanType().getListaProveedor().forEach((ProveedorTraduccion pt) -> {
			solicitud.addTraduccion(enviarSolicitud(traduccion, pt));
		});
		
		if(solicitud.getListaTraduccion().isEmpty()) {
			throw new TraduccionFailedException("Traduccion fallida en todas las instancias");
		}
		
		return toSolicitudDtoResponse(soliRepo.save(solicitud));
	}

	private void validarCaracteresEnTexto(TraduccionRequestDTO traduccion, PlanType plan) {
		if (traduccion.getTexto().length() > plan.getNumeroCaracterPorSolicitud()) {
			throw new PlanCharacterLimitExceededException();
		}
	}

	private Traduccion enviarSolicitud(TraduccionRequestDTO traduccion, ProveedorTraduccion pt) {
		Traduccion respuesta = new Traduccion();
		respuesta.setProveedorTraduccion(pt);
		try {
			respuesta.setTextoTraducido(tsr.getStrategy(pt).traducir(traduccion));
			respuesta.setEstado(EstadoTraduccion.EXITOSA);			
		} catch (Exception e) {
			respuesta.setEstado(EstadoTraduccion.FALLIDA);
		}
		return respuesta;
	}
	
	private SolicitudTraduccionResponseDTO toSolicitudDtoResponse(SolicitudTraduccion solicitud) {
		
		SolicitudTraduccionResponseDTO respuesta = new SolicitudTraduccionResponseDTO();
		respuesta.setId(solicitud.getId());
		respuesta.setDestino(solicitud.getIdiomaDestino().getCodigo());
		respuesta.setOrigen(solicitud.getIdiomaOrigen().getCodigo());
		List<TraduccionResponseDTO> listaTraduccionDTO = new ArrayList<>();
		solicitud.getListaTraduccion().forEach((Traduccion traduccion)->{
			listaTraduccionDTO.add(toDTOResponse(traduccion));
		});
		respuesta.setListaTraduccion(listaTraduccionDTO);
		respuesta.setFechaCreacion(solicitud.getFecha());
		respuesta.setTextoOriginal(solicitud.getTextoOriginal());
		
		return respuesta;
	}
	
	private TraduccionResponseDTO toDTOResponse(Traduccion traduccion) {
		TraduccionResponseDTO respuesta = new TraduccionResponseDTO();
		respuesta.setEstado(traduccion.getEstado());
		respuesta.setProveedorTraduccion(traduccion.getProveedorTraduccion());
		respuesta.setTextoTraducido(traduccion.getTextoTraducido());
		respuesta.setTipoProveedor(traduccion.getProveedorTraduccion().getTipoProveedor());
		return respuesta;
	}
}
