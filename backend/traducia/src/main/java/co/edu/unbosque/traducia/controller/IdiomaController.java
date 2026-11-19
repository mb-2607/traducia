package co.edu.unbosque.traducia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.service.IdiomaService;

@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
"http://localhost:4200" })
@RestController
@RequestMapping("/idiomas")
public class IdiomaController {

	IdiomaService idiomaServ;

	public IdiomaController(IdiomaService idiomaServ) {
		this.idiomaServ = idiomaServ;
	}
	
	@GetMapping()
	public ResponseEntity<List<IdiomaResponseDTO>> listarIdioma() {
		List<IdiomaResponseDTO> listaIdioma = idiomaServ.listarIdioma();
		return ResponseEntity.ok(listaIdioma);
	}
	
	
}
