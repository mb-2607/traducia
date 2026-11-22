package co.edu.unbosque.traducia.service;

import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.traducia.dto.AuditoriaLogDTO;
import co.edu.unbosque.traducia.entity.AuditoriaLog;
import co.edu.unbosque.traducia.repository.AuditoriaRepository;

/**
 * Servicio para consultar y crear los registros de la auditoria
 */
@Service
public class AuditoriaService {

	private final AuditoriaRepository auditRepo;

	private final ModelMapper modelMapper;

	/**
	 * Construye el servicio de auditoría e inyecta sus dependencias.
	 *
	 * @param auditRepo   repositorio encargado de la persistencia de auditorías
	 * @param modelMapper componente utilizado para convertir entidades en DTO
	 */
	public AuditoriaService(AuditoriaRepository auditRepo, ModelMapper modelMapper) {
		this.auditRepo = auditRepo;
		this.modelMapper = modelMapper;
	}

	/**
	 * Almacena un registro de auditoría en la base de datos. Si ocurre una
	 * excepción durante la persistencia, esta se captura para evitar que el fallo
	 * del registro interfiera con el flujo principal.
	 *
	 * @param log registro de auditoría que se desea almacenar
	 */
	@Transactional
	public void registrar(AuditoriaLog log) {
		try {
			auditRepo.save(log);
		} catch (Exception e) {

		}
	}

	/**
	 * Consulta los registros de auditoría de forma paginada y convierte cada
	 * entidad obtenida en su correspondiente DTO.
	 *
	 * @param pageable parámetros que definen la página y el tamaño de los
	 *                 resultados solicitados
	 * @return página de registros de auditoría convertidos a
	 *         {@link AuditoriaLogDTO}
	 */
	@Transactional(readOnly = true)
	public Page<AuditoriaLogDTO> listar(Pageable pageable) {
		Page<AuditoriaLog> audiPage = auditRepo.findAll(pageable);
		return audiPage.map((AuditoriaLog al) -> {
			return modelMapper.map(al, AuditoriaLogDTO.class);
		});
	}
}
