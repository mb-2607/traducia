package co.edu.unbosque.traducia.configuration;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import co.edu.unbosque.traducia.entity.AuditoriaLog;
import co.edu.unbosque.traducia.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

	private final AuditoriaService auditServ;

	public AuditoriaInterceptor(AuditoriaService auditServ) {
		this.auditServ = auditServ;
	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
		req.setAttribute("inicio", System.currentTimeMillis());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
		if ("OPTIONS".equals(req.getMethod())) {			
			return;
		}

		long inicio = (long) req.getAttribute("inicio");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usuario = (auth == null || auth instanceof AnonymousAuthenticationToken) ? "anonimo" : auth.getName();
		
		AuditoriaLog log = new AuditoriaLog();
		log.setUsuario(usuario);
		log.setMetodo(req.getMethod());
        log.setRuta(req.getRequestURI());
        log.setStatus(res.getStatus());
        log.setIp(req.getRemoteAddr());
        log.setDuracionMs(System.currentTimeMillis() - inicio);
        log.setFecha(LocalDateTime.now());
        
        auditServ.registrar(log);
	}

}
