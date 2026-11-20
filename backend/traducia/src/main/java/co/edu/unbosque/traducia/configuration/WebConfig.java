package co.edu.unbosque.traducia.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AuditoriaInterceptor auditInter;

	/**
	 * Construye la configuración web e inyecta el interceptor de auditoría.
	 *
	 * @param auditInter interceptor que recopila los datos de las solicitudes
	 */
	public WebConfig(AuditoriaInterceptor auditInter) {
		this.auditInter = auditInter;
	}

	@Override
	/**
	 * Registra el interceptor de auditoría para las rutas de la aplicación y
	 * excluye las rutas que no deben generar registros.
	 *
	 * @param registry registro donde se configuran los interceptores MVC
	 */
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(auditInter).addPathPatterns("/**").excludePathPatterns("/idiomas", "/auditoria/**");
	}
}
