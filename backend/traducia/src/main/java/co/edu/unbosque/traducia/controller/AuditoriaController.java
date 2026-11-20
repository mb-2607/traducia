package co.edu.unbosque.traducia.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.AuditoriaLogDTO;
import co.edu.unbosque.traducia.service.AuditoriaService;

/**
 * Controlador REST encargado de exponer los servicios de consulta de los
 * registros de auditoría.
 * <p>
 * Permite obtener los registros almacenados mediante respuestas paginadas. Las
 * solicitudes se gestionan bajo la ruta base {@code /auditoria}.
 * </p>
 *
 * @see AuditoriaService
 * @see AuditoriaLogDTO
 */
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}", "http://localhost:4200" })
@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

	private AuditoriaService auditServ;

	/**
	 * Construye el controlador de auditoría e inyecta el servicio correspondiente.
	 *
	 * @param auditServ servicio encargado de consultar y registrar auditorías
	 */
	public AuditoriaController(AuditoriaService auditServ) {
		this.auditServ = auditServ;
	}

	@GetMapping(path = "/obtener")
	/**
	 * Obtiene los registros de auditoría mediante paginación.
	 *
	 * @param pageable parámetros de paginación, como el número de página y la
	 *                 cantidad de registros por página
	 * @return respuesta HTTP 200 (OK) con una página de registros representados
	 *         mediante {@link AuditoriaLogDTO}
	 */
	public ResponseEntity<Page<AuditoriaLogDTO>> listar(Pageable pageable) {
		return ResponseEntity.ok(auditServ.listar(pageable));
	}

}
