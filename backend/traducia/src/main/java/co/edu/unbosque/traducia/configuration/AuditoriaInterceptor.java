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

	/**
	 * Construye el interceptor de auditoría e inyecta el servicio encargado de
	 * almacenar los registros generados.
	 *
	 * @param auditServ servicio utilizado para registrar la información de
	 *                  auditoría
	 */
	public AuditoriaInterceptor(AuditoriaService auditServ) {
		this.auditServ = auditServ;
	}

	@Override
	/**
	 * Se ejecuta antes de que la solicitud sea procesada por el controlador.
	 * Almacena el instante de inicio para calcular posteriormente la duración total
	 * de la solicitud.
	 *
	 * @param req     solicitud HTTP recibida
	 * @param res     respuesta HTTP asociada a la solicitud
	 * @param handler controlador o recurso que procesará la solicitud
	 * @return {@code true} para permitir que continúe el procesamiento
	 */
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
		req.setAttribute("inicio", System.currentTimeMillis());
		return true;
	}

	@Override
	/**
	 * Se ejecuta cuando termina el procesamiento de la solicitud. Obtiene los datos
	 * de contexto de la petición y construye un registro de auditoría que
	 * posteriormente envía al servicio para almacenarlo. Las solicitudes OPTIONS no
	 * generan registros.
	 *
	 * @param req     solicitud HTTP procesada
	 * @param res     respuesta HTTP generada
	 * @param handler controlador o recurso que atendió la solicitud
	 * @param ex      excepción producida durante el procesamiento, si ocurrió
	 */
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
