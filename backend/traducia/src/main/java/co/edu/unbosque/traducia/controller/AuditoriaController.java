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

@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}", "http://localhost:4200" })
@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

	private AuditoriaService auditServ;

	public AuditoriaController(AuditoriaService auditServ) {
		this.auditServ = auditServ;
	}

	@GetMapping(path = "/obtener")
	public ResponseEntity<Page<AuditoriaLogDTO>> listar(Pageable pageable) {
		return ResponseEntity.ok(auditServ.listar(pageable));
	}

}
