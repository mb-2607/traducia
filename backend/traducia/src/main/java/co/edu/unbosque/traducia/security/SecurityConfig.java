package co.edu.unbosque.traducia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de seguridad de la aplicación mediante Spring
 * Security.
 * <p>
 * Define las reglas de autorización para los diferentes endpoints, configura el
 * mecanismo de autenticación basado en usuarios y contraseñas, establece el uso
 * de sesiones sin estado y registra el filtro encargado de validar los tokens
 * JWT de las solicitudes HTTP. También configura el proveedor de autenticación
 * y el mecanismo de codificación de contraseñas mediante BCrypt.
 * </p>
 *
 * @author
 * @version 1.0
 * @see JwtAuthenticationFilter
 * @see UserDetailsService
 * @see SecurityFilterChain
 * @see AuthenticationProvider
 * @see AuthenticationManager
 * @see PasswordEncoder
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Filtro encargado de interceptar las solicitudes HTTP y validar los tokens JWT
	 * para establecer la autenticación de los usuarios en el contexto de seguridad.
	 */
	private final JwtAuthenticationFilter jwtAuthFilter;

	/**
	 * Servicio encargado de recuperar los detalles de los usuarios, incluyendo sus
	 * credenciales y autoridades, durante la autenticación.
	 */
	private final UserDetailsService userDetailsService;

	/**
	 * Construye la configuración de seguridad e inicializa las dependencias
	 * necesarias para gestionar la autenticación.
	 *
	 * @param jwtAuthFilter      filtro encargado de validar los tokens JWT.
	 * @param userDetailsService servicio encargado de recuperar los detalles de los
	 *                           usuarios.
	 */
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Define la cadena de filtros de seguridad y las reglas de acceso para los
	 * diferentes endpoints de la aplicación.
	 * <p>
	 * Habilita CORS, desactiva CSRF, establece los permisos de acceso según las
	 * rutas y roles de usuario, configura una política de sesiones sin estado y
	 * registra el filtro JWT antes del filtro de autenticación por usuario y
	 * contraseña.
	 * </p>
	 *
	 * @param http objeto utilizado para configurar la seguridad HTTP.
	 * @return cadena de filtros de seguridad configurada para la aplicación.
	 * @throws Exception si ocurre un error durante la configuración de seguridad
	 *                   HTTP.
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/user/getall", "/user/count", "/user/exists/**", "/user/getbyid/**")
						.hasAnyRole("USER", "ADMIN").requestMatchers("/user/**").hasAnyRole("ADMIN")
						.requestMatchers("/traductor/**").hasAnyRole("USER", "ADMIN").anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Configura el proveedor de autenticación basado en acceso a datos.
	 * <p>
	 * Utiliza UserDetailsService para recuperar los usuarios y PasswordEncoder para
	 * comprobar sus contraseñas codificadas.
	 * </p>
	 *
	 * @return proveedor de autenticación configurado con los servicios de
	 *         recuperación de usuarios y codificación de contraseñas.
	 */
	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	/**
	 * Obtiene el administrador de autenticación configurado por Spring Security.
	 *
	 * @param config configuración de autenticación de Spring Security.
	 * @return administrador encargado de gestionar los procesos de autenticación.
	 * @throws Exception si ocurre un error al obtener el administrador de
	 *                   autenticación.
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Define el codificador de contraseñas utilizado por la aplicación.
	 * <p>
	 * Utiliza BCrypt para generar hashes de contraseñas y comprobar posteriormente
	 * las credenciales proporcionadas por los usuarios.
	 * </p>
	 *
	 * @return instancia de BCryptPasswordEncoder utilizada para codificar y
	 *         verificar contraseñas.
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
