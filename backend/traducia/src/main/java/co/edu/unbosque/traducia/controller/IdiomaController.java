package co.edu.unbosque.traducia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.service.IdiomaService;

/**
 * Controlador REST encargado de gestionar las solicitudes relacionadas
 * con los idiomas disponibles en la aplicación.
 * <p>
 * Expone endpoints para consultar la lista de idiomas registrados.
 * Permite el acceso desde los orígenes configurados mediante CORS.
 */
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
"http://localhost:4200" })
@RestController
@RequestMapping("/idiomas")
public class IdiomaController {
	
	/**
     * Servicio encargado de gestionar las operaciones relacionadas
     * con los idiomas.
     */
	private final IdiomaService idiomaServ;
	
	/**
     * Construye una instancia del controlador de idiomas.
     *
     * @param idiomaServ servicio utilizado para realizar las operaciones
     *                   relacionadas con los idiomas
     */
	public IdiomaController(IdiomaService idiomaServ) {
		this.idiomaServ = idiomaServ;
	}
	
	/**
     * Obtiene la lista de idiomas registrados en el sistema.
     *
     * @return respuesta HTTP 200 (OK) que contiene una lista de objetos
     *         {@link IdiomaResponseDTO} con la información de los idiomas
     */
	@GetMapping()
	public ResponseEntity<List<IdiomaResponseDTO>> listarIdioma() {
		List<IdiomaResponseDTO> listaIdioma = idiomaServ.listarIdioma();
		return ResponseEntity.ok(listaIdioma);
	}
	
	
}
