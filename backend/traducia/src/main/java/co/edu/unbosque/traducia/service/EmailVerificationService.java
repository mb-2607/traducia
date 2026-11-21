package co.edu.unbosque.traducia.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import co.edu.unbosque.traducia.dto.ResendVerificationRequestDTO;
import co.edu.unbosque.traducia.entity.EmailVerificationToken;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.repository.EmailVerificationTokenRepository;
import co.edu.unbosque.traducia.repository.UserRepository;

/**
 * Servicio encargado de gestionar el flujo de verificación de correo electrónico.
 * <p>
 * Genera tokens temporales con vigencia de 12 horas, coordina el envío del enlace
 * al frontend mediante {@link EmailService} y activa la cuenta del usuario al
 * validar el token.
 * </p>
 */
@Service
public class EmailVerificationService {

	/**
	 * Repositorio de usuarios.
	 */
	private final UserRepository userRepo;

	/**
	 * Repositorio de tokens de verificación.
	 */
	private final EmailVerificationTokenRepository tokenRepo;

	/**
	 * Servicio encargado del envío de correos electrónicos.
	 */
	private final EmailService emailService;

	/**
	 * URL base del frontend.
	 */
	@Value("${front.url:http://localhost:4200}")
	private String frontUrl;

	/**
	 * Ruta del frontend encargada de procesar la verificación del correo.
	 */
	@Value("${front.verify-email-path:/verify-email}")
	private String verifyEmailPath;

	/**
	 * Crea el servicio con sus dependencias inyectadas por Spring.
	 *
	 * @param userRepo repositorio de usuarios
	 * @param tokenRepo repositorio de tokens de verificación
	 * @param emailService servicio de envío de correos
	 */
	public EmailVerificationService(UserRepository userRepo, EmailVerificationTokenRepository tokenRepo,
			EmailService emailService) {
		this.userRepo = userRepo;
		this.tokenRepo = tokenRepo;
		this.emailService = emailService;
	}

	/**
	 * Genera un nuevo token de verificación con vigencia de 12 horas y envía el correo al usuario.
	 *
	 * @param usuario usuario recién registrado o que solicita reenvío
	 */
	@Transactional
	public void generarYEnviarToken(User usuario) {
		tokenRepo.deleteByUser(usuario);

		String token = UUID.randomUUID().toString();
		EmailVerificationToken verificationToken = new EmailVerificationToken(token, usuario,
				LocalDateTime.now().plusHours(12));
		tokenRepo.save(verificationToken);

		emailService.enviarCorreoVerificacion(usuario.getEmail(), usuario.getUsername(), construirEnlace(token));
	}

	/**
	 * Valida el token recibido y habilita la cuenta del usuario asociado.
	 *
	 * @param token código único recibido desde el enlace del correo
	 * @throws InvalidTokenException si el token es vacío, inexistente, usado o vencido
	 * @throws IllegalStateException si la cuenta está bloqueada
	 */
	@Transactional
	public void verificarCorreo(String token) {
		if (token == null || token.isBlank()) {
			throw new InvalidTokenException("El token es obligatorio");
		}

		Optional<EmailVerificationToken> tokenOpt = tokenRepo.findByToken(token.trim());
		if (tokenOpt.isEmpty()) {
			throw new InvalidTokenException("Token inválido");
		}

		EmailVerificationToken verificationToken = tokenOpt.get();
		if (verificationToken.isUsado() || verificationToken.estaExpirado()) {
			throw new InvalidTokenException("El token ya fue usado o expiró");
		}

		User usuario = verificationToken.getUser();
		if (!usuario.isAccountNonLocked()) {
			throw new IllegalStateException("La cuenta está bloqueada");
		}

		usuario.setEnabled(true);
		userRepo.save(usuario);

		verificationToken.setUsado(true);
		tokenRepo.save(verificationToken);
	}

	/**
	 * Reenvía el enlace de verificación a un usuario cuya cuenta aún no ha sido activada.
	 *
	 * @param request petición con el correo electrónico del usuario
	 * @throws IllegalArgumentException si el correo llega vacío
	 * @throws UserNotFoundException si no existe una cuenta con ese correo
	 * @throws IllegalStateException si la cuenta ya fue verificada o está bloqueada
	 */
	@Transactional
	public void reenviarVerificacion(ResendVerificationRequestDTO request) {
		if (request == null || request.getCorreo() == null || request.getCorreo().isBlank()) {
			throw new IllegalArgumentException("El correo es obligatorio");
		}

		String correo = request.getCorreo().trim().toLowerCase();
		Optional<User> usuarioOpt = userRepo.findByEmail(correo);
		if (usuarioOpt.isEmpty()) {
			throw new UserNotFoundException();
		}

		User usuario = usuarioOpt.get();
		if (usuario.isEnabled()) {
			throw new IllegalStateException("La cuenta ya se encuentra verificada");
		}
		if (!usuario.isAccountNonLocked()) {
			throw new IllegalStateException("La cuenta está bloqueada");
		}

		generarYEnviarToken(usuario);
	}

	/**
	 * Construye la URL del frontend con el token de verificación.
	 *
	 * @param token token generado para el usuario
	 * @return enlace completo hacia la vista de verificación
	 */
	private String construirEnlace(String token) {
		String base = frontUrl.endsWith("/") ? frontUrl.substring(0, frontUrl.length() - 1) : frontUrl;
		String ruta = verifyEmailPath.startsWith("/") ? verifyEmailPath : "/" + verifyEmailPath;
		return UriComponentsBuilder.fromUriString(base)
				.path(ruta)
				.queryParam("token", token)
				.toUriString();
	}
}