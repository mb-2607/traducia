package co.edu.unbosque.traducia.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import co.edu.unbosque.traducia.dto.ForgotPasswordRequestDTO;
import co.edu.unbosque.traducia.dto.ResetPasswordRequestDTO;
import co.edu.unbosque.traducia.entity.PasswordResetToken;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.repository.PasswordResetTokenRepository;
import co.edu.unbosque.traducia.repository.UserRepository;

/**
 * Servicio encargado de gestionar el flujo de recuperación y restablecimiento de contraseñas.
 * <p>
 * Genera tokens temporales con vencimiento de 30 minutos, coordina el envío del enlace
 * hacia el frontend mediante {@link EmailService} y valida la vigencia del token y el
 * estado de la cuenta antes de actualizar la contraseña codificada del usuario.
 * </p>
 *
 * @author
 * @version 1.0
 * @see PasswordResetToken
 * @see EmailService
 * @see UserService
 */
@Service
public class PasswordResetService {

	/**
	 * Repositorio de usuarios.
	 */
	private final UserRepository userRepo;

	/**
	 * Repositorio de tokens de recuperación.
	 */
	private final PasswordResetTokenRepository tokenRepo;

	/**
	 * Servicio que valida reglas de usuario.
	 */
	private final UserService userService;

	/**
	 * Servicio que envía correos electrónicos.
	 */
	private final EmailService emailService;

	/**
	 * Codificador de contraseñas.
	 */
	private final PasswordEncoder passwordEncoder;

	/**
	 * URL base del frontend.
	 */
	@Value("${front.url:http://localhost:4200}")
	private String frontUrl;

	/**
	 * Crea el componente con sus dependencias inyectadas por Spring.
	 *
	 * @param userRepo repositorio de usuarios
	 * @param tokenRepo repositorio de tokens de recuperación
	 * @param userService servicio que valida reglas de usuario
	 * @param emailService servicio que envía correos electrónicos
	 * @param passwordEncoder codificador de contraseñas
	 */
	public PasswordResetService(UserRepository userRepo, PasswordResetTokenRepository tokenRepo,
			UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.tokenRepo = tokenRepo;
		this.userService = userService;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Genera un token de recuperación y envía el enlace al correo del usuario.
	 *
	 * Busca la cuenta por correo, valida que esté activa, borra tokens previos
	 * y despacha el correo con vencimiento de 30 minutos.
	 *
	 * @param request petición con el correo del usuario
	 * @throws IllegalArgumentException si el correo llega vacío
	 * @throws UserNotFoundException si no existe un usuario con ese correo
	 * @throws IllegalStateException si la cuenta está deshabilitada o bloqueada
	 */
	@Transactional
	public void solicitarRecuperacion(ForgotPasswordRequestDTO request) {
		if (request == null || request.getCorreo() == null || request.getCorreo().isBlank()) {
			throw new IllegalArgumentException("El correo es obligatorio");
		}

		String correo = request.getCorreo().trim().toLowerCase();
		Optional<User> usuarioOpt = userRepo.findByEmail(correo);
		if (usuarioOpt.isEmpty()) {
			throw new UserNotFoundException();
		}

		User usuario = usuarioOpt.get();
		validarCuentaActiva(usuario);

		tokenRepo.deleteByUser(usuario);

		String token = UUID.randomUUID().toString();
		PasswordResetToken resetToken = new PasswordResetToken(token, usuario, LocalDateTime.now().plusMinutes(30));
		tokenRepo.save(resetToken);

		emailService.enviarCorreoRecuperacion(usuario.getEmail(), usuario.getUsername(), construirEnlace(token));
	}

	/**
	 * Cambia la contraseña del usuario a partir de un token válido.
	 *
	 * Verifica que el token exista, no esté usado ni expirado, valida la nueva
	 * contraseña, la codifica y marca el token como utilizado.
	 *
	 * @param request petición con el token y la nueva contraseña
	 * @throws InvalidTokenException si el token no existe, ya fue usado o expiró
	 * @throws IllegalStateException si la cuenta está deshabilitada o bloqueada
	 * @throws InvalidPasswordException si la contraseña no cumple la longitud mínima
	 */
	@Transactional
	public void restablecerPassword(ResetPasswordRequestDTO request) {
		if (request == null || request.getToken() == null || request.getToken().isBlank()) {
			throw new InvalidTokenException("El token es obligatorio");
		}

		Optional<PasswordResetToken> tokenOpt = tokenRepo.findByToken(request.getToken().trim());
		if (tokenOpt.isEmpty()) {
			throw new InvalidTokenException("Token inválido");
		}

		PasswordResetToken resetToken = tokenOpt.get();
		if (resetToken.isUsado() || resetToken.estaExpirado()) {
			throw new InvalidTokenException("El token ya fue usado o expiró");
		}

		User usuario = resetToken.getUser();
		validarCuentaActiva(usuario);

		userService.validarPassword(request.getNuevaPassword());
		usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
		usuario.setDebeCambiarPassword(false);
		userRepo.save(usuario);

		resetToken.setUsado(true);
		tokenRepo.save(resetToken);
	}

	/**
	 * Verifica que el usuario esté habilitado y sin bloqueo.
	 *
	 * @param usuario usuario a validar
	 * @throws IllegalStateException si la cuenta no está activa
	 */
	private void validarCuentaActiva(User usuario) {
		if (!usuario.isEnabled() || !usuario.isAccountNonLocked()) {
			throw new IllegalStateException("La cuenta no está habilitada o está bloqueada");
		}
	}

	/**
	 * Arma la URL del frontend con el token de recuperación.
	 *
	 * @param token token generado para el usuario
	 * @return enlace completo para restablecer la contraseña
	 */
	private String construirEnlace(String token) {
		String base = frontUrl.endsWith("/") ? frontUrl.substring(0, frontUrl.length() - 1) : frontUrl;
		return UriComponentsBuilder.fromUriString(base)
				.path("/restablecer-contrasena")
				.queryParam("token", token)
				.toUriString();
	}
}