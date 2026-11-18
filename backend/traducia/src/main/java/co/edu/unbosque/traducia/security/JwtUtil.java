package co.edu.unbosque.traducia.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.unbosque.traducia.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Componente encargado de gestionar la generación, extracción
 * y validación de tokens JSON Web Token (JWT).
 * <p>
 * Permite crear tokens firmados con el algoritmo HS256, incorporar
 * información adicional del usuario mediante claims y recuperar
 * los datos contenidos en los tokens.
 * También verifica la identidad del usuario y la vigencia
 * del token durante los procesos de autenticación.
 * </p>
 *
 * @author
 * @version 1.0
 * @see UserDetails
 * @see User
 * @see JwtAuthenticationFilter
 * @see Claims
 */
@Component
public class JwtUtil {

    /**
     * Tiempo de validez de los tokens JWT, establecido en 24 horas
     * y expresado en milisegundos.
     */
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    /**
     * Clave secreta utilizada para firmar y verificar los tokens JWT.
     * Su valor se obtiene de las propiedades de configuración
     * de la aplicación.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Obtiene la clave criptográfica utilizada para firmar y verificar
     * los tokens JWT mediante el algoritmo HMAC-SHA256.
     *
     * @return clave criptográfica derivada de la clave secreta configurada.
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario almacenado en el claim subject
     * del token JWT.
     *
     * @param token token JWT del cual se obtiene la información.
     * @return nombre de usuario asociado al token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración almacenada en el token JWT.
     *
     * @param token token JWT del cual se obtiene la información.
     * @return fecha y hora en la que expira el token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el rol del usuario almacenado en el claim personalizado "role".
     *
     * @param token token JWT del cual se obtiene la información.
     * @return nombre del rol asociado al usuario.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrae el tipo de plan del usuario almacenado en el claim personalizado
     * "plantype".
     *
     * @param token token JWT del cual se obtiene la información.
     * @return nombre del tipo de plan asociado al usuario.
     */
    public String extractPlanType(String token) {
        return extractClaim(token, claims -> claims.get("plantype", String.class));
    }

    /**
     * Extrae un dato específico del conjunto de claims de un token JWT
     * mediante una función de resolución.
     *
     * @param <T> tipo de dato que se desea obtener del token.
     * @param token token JWT del cual se extraerán los claims.
     * @param claimsResolver función encargada de obtener el dato deseado
     *                       a partir de los claims.
     * @return valor obtenido mediante la función de resolución.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims contenidos en un token JWT.
     * <p>
     * Verifica la firma del token utilizando la clave secreta
     * configurada y obtiene su cuerpo de información.
     * </p>
     *
     * @param token token JWT del cual se obtendrán los claims.
     * @return conjunto de claims contenidos en el token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Comprueba si un token JWT ha superado su fecha de expiración.
     *
     * @param token token JWT que se desea comprobar.
     * @return {@code true} si el token ha expirado; {@code false} en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un token JWT para un usuario autenticado.
     * <p>
     * Incorpora las autoridades del usuario y, cuando corresponde
     * a una instancia de la entidad User, también incluye su rol
     * y tipo de plan como claims personalizados.
     * </p>
     *
     * @param userDetails información del usuario para el cual se generará el token.
     * @return token JWT firmado con la clave secreta configurada.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("authorities", userDetails.getAuthorities());

        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("role", user.getRole().name());
            claims.put("plantype", user.getPlanType().name());
        }

        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Construye y firma un token JWT con los claims proporcionados
     * y el identificador del usuario como subject.
     * <p>
     * Establece las fechas de emisión y expiración del token
     * y utiliza el algoritmo HS256 para generar su firma.
     * </p>
     *
     * @param claims información adicional que se incluirá en el token.
     * @param subject identificador del usuario asociado al token.
     * @return token JWT generado y firmado.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida un token JWT comprobando que el nombre de usuario
     * coincida con el usuario proporcionado y que el token
     * no haya expirado.
     *
     * @param token token JWT que se desea validar.
     * @param userDetails información del usuario contra la cual se comprobará el token.
     * @return {@code true} si el usuario coincide y el token continúa vigente;
     *         {@code false} en caso contrario.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
