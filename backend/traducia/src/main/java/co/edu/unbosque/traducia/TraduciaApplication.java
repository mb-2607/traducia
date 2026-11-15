package co.edu.unbosque.traducia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Traducia.
 *
 */
@SpringBootApplication
public class TraduciaApplication {
	
	/**
	 * Punto de entrada de la aplicación. Arranca el contexto de Spring Boot.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(TraduciaApplication.class, args);
	}

}
