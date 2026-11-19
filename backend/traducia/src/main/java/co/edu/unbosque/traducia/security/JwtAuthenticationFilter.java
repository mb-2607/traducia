package co.edu.unbosque.traducia.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de seguridad encargado de autenticar las solicitudes HTTP
 * mediante tokens JSON Web Token (JWT).
 * <p>
 * Intercepta cada solicitud, obtiene el token enviado en la cabecera
 * Authorization, extrae el nombre de usuario y valida su autenticidad.
 * Si el token es válido y no existe una autenticación previa, establece
 * la información del usuario en el contexto de seguridad de Spring.
 * </p>
 *
 * @author
 * @version 1.0
 * @see OncePerRequestFilter
 * @see JwtUtil
 * @see UserDetailsService
 * @see SecurityContextHolder
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Utilidad encargada de extraer información y validar los tokens JWT.
     */
    private final JwtUtil jwtUtil;

    /**
     * Servicio encargado de recuperar los detalles y autoridades
     * de los usuarios registrados en la aplicación.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Construye el filtro de autenticación JWT e inicializa
     * las dependencias necesarias para validar los usuarios.
     *
     * @param jwtUtil utilidad encargada de gestionar los tokens JWT.
     * @param userDetailsService servicio encargado de recuperar los detalles de los usuarios.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepta las solicitudes HTTP para comprobar la presencia
     * y validez de un token JWT.
     * <p>
     * Extrae el token de la cabecera Authorization cuando utiliza
     * el esquema Bearer. Posteriormente, obtiene el nombre de usuario,
     * recupera sus detalles y valida el token.
     * Si la validación es satisfactoria y no existe una autenticación
     * previa, establece una nueva autenticación en el contexto
     * de seguridad de Spring.
     * Finalmente, permite que la solicitud continúe por la cadena
     * de filtros.
     * </p>
     *
     * @param request solicitud HTTP que contiene la información de autenticación.
     * @param response respuesta HTTP asociada a la solicitud.
     * @param filterChain cadena de filtros por la que debe continuar la solicitud.
     * @throws ServletException si ocurre un error durante el procesamiento del filtro.
     * @throws IOException si ocurre un error de entrada o salida durante el procesamiento.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error extracting username from token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
