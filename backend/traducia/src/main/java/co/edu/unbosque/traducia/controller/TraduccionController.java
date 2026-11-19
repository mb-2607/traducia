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

@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
"http://localhost:4200" })
@RestController
@RequestMapping("/traductor")
public class TraduccionController {

	private final TraduccionService traduccionServ;

	public TraduccionController(TraduccionService traduccionServ) {
		this.traduccionServ = traduccionServ;
	}
	
	@PostMapping(path = "/traducir")
    public ResponseEntity<SolicitudTraduccionResponseDTO> traducir(@RequestBody TraduccionRequestDTO request,
                                           Authentication authentication) {
        String nombre = authentication.getName();
        SolicitudTraduccionResponseDTO respuesta = traduccionServ.traducir(request, nombre);
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }
	
	@ExceptionHandler(PlanCharacterLimitExceededException.class)
	public ResponseEntity<String> manejarPlanCharacterLimitExceededException() {
		return ResponseEntity.badRequest().body("Cantidad de caracteres excedidad frente al plan actual");
	}
	

}
