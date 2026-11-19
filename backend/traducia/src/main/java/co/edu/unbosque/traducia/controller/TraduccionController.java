package co.edu.unbosque.traducia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.service.TraduccionService;

/**
 * Controlador REST del módulo de traducción.
 *
 * Expone los endpoints bajo la ruta <code>/traductor</code>. Requiere un usuario
 * autenticado y delega la lógica en {@link TraduccionService}.
 */
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
"http://localhost:4200" })
@RestController
@RequestMapping("/traductor")
public class TraduccionController {

	/**
	 * Servicio que ejecuta la lógica de traducción.
	 */
	private final TraduccionService traduccionServ;

	/**
	 * Crea el componente con sus dependencias inyectadas por Spring.
	 *
	 * @param traduccionServ servicio que ejecuta la lógica de traducción
	 */
	public TraduccionController(TraduccionService traduccionServ) {
		this.traduccionServ = traduccionServ;
	}
	
	/**
	 * Traduce un texto con los proveedores del plan del usuario autenticado.
	 *
	 * @param request datos de la traducción: idiomas de origen y destino y texto
	 * @param authentication autenticación de la que se toma el nombre del usuario
	 * @return la solicitud creada con la traducción de cada proveedor y estado HTTP 201
	 */
	@PostMapping(path = "/traducir")
    public ResponseEntity<SolicitudTraduccionResponseDTO> traducir(@RequestBody TraduccionRequestDTO request,
                                           Authentication authentication) {
        String nombre = authentication.getName();
        SolicitudTraduccionResponseDTO respuesta = traduccionServ.traducir(request, nombre);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }
	
	/**
	 * Maneja {@link PlanCharacterLimitExceededException}.
	 *
	 * @return respuesta HTTP 400 con un mensaje que indica que se excedió el límite del plan
	 */
	@ExceptionHandler(PlanCharacterLimitExceededException.class)
	public ResponseEntity<String> manejarPlanCharacterLimitExceededException() {
		return ResponseEntity.badRequest().body("Cantidad de caracteres excedidad frente al plan actual");
	}
	

}
