package co.edu.unbosque.traducia.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
"http://localhost:4200" })
@RestController
@RequestMapping("/traductor")
public class AuditoriaController {
	
	
	
	public AuditoriaController() {
		// TODO Auto-generated constructor stub
	}

}
