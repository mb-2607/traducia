package co.edu.unbosque.traducia.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.traducia.entity.Idioma;
import co.edu.unbosque.traducia.entity.Role;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.repository.IdiomaRepository;
import co.edu.unbosque.traducia.repository.UserRepository;

/**
 * Configuración encargada de inicializar usuarios predeterminados
 * en la base de datos durante el arranque de la aplicación.
 * <p>
 * Comprueba la existencia de las cuentas de administrador y usuario normal.
 * Si alguna no se encuentra registrada, la crea utilizando las credenciales
 * definidas en las propiedades de configuración y almacena sus contraseñas
 * codificadas mediante Spring Security.
 * </p>
 *
 * @author
 * @version 1.0
 * @see CommandLineRunner
 * @see UserRepository
 * @see PasswordEncoder
 * @see User
 * @see Role
 */
@Configuration
public class LoadDatabase {
	
	/**
     * Registrador utilizado para generar mensajes informativos
     * durante el proceso de inicialización de la base de datos.
     */
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
	
	/**
     * Define el proceso de inicialización de usuarios predeterminados
     * que se ejecuta durante el arranque de la aplicación.
     * <p>
     * Comprueba si existen las cuentas de administrador y usuario normal.
     * Si alguna no existe, crea el usuario correspondiente, codifica
     * su contraseña y lo almacena mediante el repositorio.
     * Las contraseñas se obtienen de las propiedades de configuración
     * de la aplicación.
     * </p>
     *
     * @param userRepo repositorio encargado de consultar y almacenar usuarios.
     * @param passwordEncoder componente encargado de codificar las contraseñas.
     * @param adminPassword contraseña configurada para la cuenta de administrador.
     * @param userPassword contraseña configurada para la cuenta de usuario normal.
     * @return componente CommandLineRunner encargado de ejecutar la inicialización
     *         de los usuarios al iniciar la aplicación.
     */
	@Bean
	CommandLineRunner initDatabase(UserRepository userRepo, PasswordEncoder passwordEncoder, @Value("${app.admin.password}") String adminPassword,
	        @Value("${app.user.password}") String userPassword) {

		return args -> {
			Optional<User> found = userRepo.findByUsername("admin");
			if (found.isPresent()) {
				log.info("El administrador ya existe, omitiendo la creación del administrador...");
			} else {
				User adminUser = new User("admin", passwordEncoder.encode(adminPassword), "traducia687@gmail.com",
						Role.ADMIN);
				userRepo.save(adminUser);
				log.info("Precargando usuario administrador");
			}
			Optional<User> found2 = userRepo.findByUsername("normaluser");
			if (found2.isPresent()) {
				log.info("El usuario normal ya existe, omitiendo la creación del usuario normal...");
			} else {
				User normalUser = new User("normaluser", passwordEncoder.encode(userPassword),
						"manuelbarros2007@gmail.com", Role.USER);
				userRepo.save(normalUser);
				log.info("Precargando usuario normal");
			}
		};
	}
	
	@Bean
	CommandLineRunner initIdiomas(IdiomaRepository idiomaRepo) {
		return args -> {
			Map<String, String> idiomas = new LinkedHashMap<>();
			idiomas.put("es", "Español");
			idiomas.put("en", "Inglés");
			idiomas.put("fr", "Francés");
			idiomas.put("de", "Alemán");
			idiomas.put("pt", "Portugués");

			idiomas.forEach((codigo, nombre) -> {
				if (idiomaRepo.findByCodigo(codigo).isPresent()) {
					log.info("El idioma {} ya existe, omitiendo...", codigo);
				} else {
					idiomaRepo.save(new Idioma(codigo, nombre));
					log.info("Precargando idioma {}", codigo);
				}
			});
		};
	}
}
