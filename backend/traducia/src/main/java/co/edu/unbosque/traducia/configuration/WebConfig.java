package co.edu.unbosque.traducia.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AuditoriaInterceptor auditInter;

	public WebConfig(AuditoriaInterceptor auditInter) {
		this.auditInter = auditInter;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(auditInter).addPathPatterns("/**").excludePathPatterns("/idiomas");
	}
}
