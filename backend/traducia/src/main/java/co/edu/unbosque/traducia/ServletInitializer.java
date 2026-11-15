package co.edu.unbosque.traducia;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador para desplegar la aplicación Traducia como WAR en un contenedor de servlets externo.
 * 
 * 
 */
public class ServletInitializer extends SpringBootServletInitializer {
	
	/**
     * Configura la aplicación indicando la clase principal de arranque.
     *
     * @param application constructor de la aplicación
     * @return el constructor configurado con {@link TraduciaApplication}
     */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TraduciaApplication.class);
	}

}
