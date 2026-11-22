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

/**
 * Servicio con la lógica de negocio de las traducciones.
 *
 * Valida el texto contra el plan del usuario, consulta a cada proveedor que el
 * plan permite mediante el patrón Strategy y persiste la solicitud con sus
 * resultados.
 */
@Service
public class TraduccionService {

	/**
	 * Servicio que consulta usuarios.
	 */
	private UserService userService;

	/**
	 * Registro que resuelve la estrategia de traducción según el proveedor.
	 */
	private TraductorStrategyRegistry tsr;

	/**
	 * Repositorio de solicitudes de traducción.
	 */
	private SolicitudTraduccionRepository soliRepo;

	/**
	 * Servicio que consulta idiomas.
	 */
	private IdiomaService idiomaService;
	
	/**
	 * Proovedor de reemplazo en caso en que falle alguno.
	 */
	private ProveedorTraduccion reemplazo = ProveedorTraduccion.GEMMA;
	
	/**
	 * Crea el componente con sus dependencias inyectadas por Spring.
	 *
	 * @param userService   servicio que consulta usuarios
	 * @param tsr           registro que resuelve la estrategia de traducción según
	 *                      el proveedor
	 * @param soliRepo      repositorio de solicitudes de traducción
	 * @param idiomaService servicio que consulta idiomas
	 */
	public TraduccionService(UserService userService, TraductorStrategyRegistry tsr,
			SolicitudTraduccionRepository soliRepo, IdiomaService idiomaService) {
		this.userService = userService;
		this.tsr = tsr;
		this.soliRepo = soliRepo;
		this.idiomaService = idiomaService;
	}

	/**
	 * Traduce el texto con todos los proveedores que permite el plan del usuario.
	 *
	 * Valida el límite de caracteres, busca los idiomas, crea la solicitud,
	 * consulta a cada proveedor y guarda el resultado.
	 *
	 * @param traduccion    datos de la traducción: idiomas de origen y destino y
	 *                      texto
	 * @param nombreUsuario nombre del usuario autenticado
	 * @return la solicitud guardada con la traducción de cada proveedor
	 * @throws PlanCharacterLimitExceededException si el texto supera el límite del
	 *                                             plan
	 * @throws IdiomaNotFoundException             si algún código de idioma no
	 *                                             existe
	 * @throws IdiomaEqualsException               si el idioma de origen y el de
	 *                                             destino son iguales
	 * @throws TraduccionFailedException           si la solicitud queda sin
	 *                                             traducciones
	 */
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
			if(solicitud.getListaTraduccion().getLast().getEstado()==EstadoTraduccion.FALLIDA) {
				solicitud.addTraduccion(enviarSolicitudReemplazo(traduccion));
			}
		});

		if (solicitud.getListaTraduccion().stream()
				.noneMatch(traduccionResultado -> traduccionResultado.getEstado() == EstadoTraduccion.EXITOSA)) {
			throw new TraduccionFailedException("No se pudo traducir el texto con ninguno de los proveedores.");
		}

		return toSolicitudDtoResponse(soliRepo.save(solicitud));
	}

	/**
	 * Verifica que el texto no supere el límite de caracteres del plan.
	 *
	 * @param traduccion petición con el texto a validar
	 * @param plan       plan del usuario
	 * @throws PlanCharacterLimitExceededException si el texto es más largo que el
	 *                                             límite
	 */
	private void validarCaracteresEnTexto(TraduccionRequestDTO traduccion, PlanType plan) {
		if (traduccion.getTexto().length() > plan.getNumeroCaracterPorSolicitud()) {
			throw new PlanCharacterLimitExceededException();
		}
	}

	/**
	 * Envía el texto a un proveedor y registra el resultado.
	 *
	 * Si el proveedor funciona, la traducción queda {@code EXITOSA}; si lanza
	 * cualquier excepción, queda {@code FALLIDA} sin texto.
	 *
	 * @param traduccion petición con el texto a traducir
	 * @param pt         proveedor al que se consulta
	 * @return la traducción con su estado
	 */
	private Traduccion enviarSolicitud(TraduccionRequestDTO traduccion, ProveedorTraduccion pt) {
		Traduccion respuesta = new Traduccion();
		respuesta.setProveedorTraduccion(pt);
		try {
			respuesta.setTextoTraducido(tsr.getStrategy(pt).traducir(traduccion));
			respuesta.setEstado(EstadoTraduccion.EXITOSA);
		} catch (Exception e) {
			respuesta.setEstado(EstadoTraduccion.FALLIDA);
			e.printStackTrace();
		}
		return respuesta;
	}

	/**
	 * Convierte una solicitud en su DTO de respuesta.
	 *
	 * @param solicitud solicitud guardada
	 * @return DTO con los códigos de idioma y las traducciones
	 */
	private SolicitudTraduccionResponseDTO toSolicitudDtoResponse(SolicitudTraduccion solicitud) {

		SolicitudTraduccionResponseDTO respuesta = new SolicitudTraduccionResponseDTO();
		respuesta.setId(solicitud.getId());
		respuesta.setDestino(solicitud.getIdiomaDestino().getCodigo());
		respuesta.setOrigen(solicitud.getIdiomaOrigen().getCodigo());
		List<TraduccionResponseDTO> listaTraduccionDTO = new ArrayList<>();
		solicitud.getListaTraduccion().forEach((Traduccion traduccion) -> {
			listaTraduccionDTO.add(toDTOResponse(traduccion));
		});
		respuesta.setListaTraduccion(listaTraduccionDTO);
		respuesta.setFechaCreacion(solicitud.getFecha());
		respuesta.setTextoOriginal(solicitud.getTextoOriginal());

		return respuesta;
	}

	/**
	 * Convierte una traducción en su DTO de respuesta.
	 *
	 * @param traduccion traducción a convertir
	 * @return DTO con estado, texto, proveedor y tipo de proveedor
	 */
	private TraduccionResponseDTO toDTOResponse(Traduccion traduccion) {
		TraduccionResponseDTO respuesta = new TraduccionResponseDTO();
		respuesta.setEstado(traduccion.getEstado());
		respuesta.setProveedorTraduccion(traduccion.getProveedorTraduccion());
		respuesta.setTextoTraducido(traduccion.getTextoTraducido());
		respuesta.setTipoProveedor(traduccion.getProveedorTraduccion().getTipoProveedor());
		return respuesta;
	}
	
	/**
	 * Envía el texto al proveedor de reemplazo y registra el resultado.
	 *
	 * Si el proveedor funciona, la traducción queda {@code EXITOSA}; si lanza cualquier
	 * excepción, queda {@code FALLIDA} sin texto.
	 *
	 * @param traduccion petición con el texto a traducir
	 * @return la traducción con su estado
	 */
	private Traduccion enviarSolicitudReemplazo(TraduccionRequestDTO traduccion) {
		return enviarSolicitud(traduccion, reemplazo);
	}
}