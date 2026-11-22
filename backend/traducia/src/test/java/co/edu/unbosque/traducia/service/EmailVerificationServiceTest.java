package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.traducia.dto.ResendVerificationRequestDTO;
import co.edu.unbosque.traducia.entity.EmailVerificationToken;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.InvalidTokenException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.repository.EmailVerificationTokenRepository;
import co.edu.unbosque.traducia.repository.UserRepository;

/**
 * Pruebas unitarias para {@link EmailVerificationService} utilizando JUnit 5,
 * Mockito y AssertJ.
 *
 * <p>Valida la generación y envío de tokens de verificación, la activación de
 * cuentas y el reenvío de enlaces aislando los repositorios y el servicio de
 * correo electrónico.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private EmailVerificationTokenRepository tokenRepo;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private EmailVerificationService emailVerificationService;

	/**
	 * Inicializa las propiedades {@code @Value} del servicio antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(emailVerificationService, "frontUrl", "http://localhost:4200/");
		ReflectionTestUtils.setField(emailVerificationService, "verifyEmailPath", "/verify-email");
	}

	/**
	 * Verifica que {@code generarYEnviarToken} elimine tokens previos, guarde uno
	 * nuevo y envíe el correo con el enlace construido correctamente.
	 */
	@Test
	@DisplayName("generarYEnviarToken debe eliminar tokens previos, guardar uno nuevo y enviar correo")
	void generarYEnviarTokenDebeGuardarTokenYEnviarCorreo() {
		User usuario = mock(User.class);
		when(usuario.getEmail()).thenReturn("usuario@ejemplo.com");
		when(usuario.getUsername()).thenReturn("david");

		emailVerificationService.generarYEnviarToken(usuario);

		ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
		ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);

		verify(tokenRepo).deleteByUser(usuario);
		verify(tokenRepo).save(tokenCaptor.capture());
		verify(emailService).enviarCorreoVerificacion(eq("usuario@ejemplo.com"), eq("david"), enlaceCaptor.capture());

		EmailVerificationToken tokenGuardado = tokenCaptor.getValue();
		assertThat(tokenGuardado.getUser()).isSameAs(usuario);
		assertThat(tokenGuardado.isUsado()).isFalse();
		assertThat(tokenGuardado.estaExpirado()).isFalse();
		assertThat(enlaceCaptor.getValue()).startsWith("http://localhost:4200/verify-email?token=");
	}

	/**
	 * Verifica que {@code verificarCorreo} habilite la cuenta y marque el token
	 * como usado cuando es válido.
	 */
	@Test
	@DisplayName("verificarCorreo debe activar la cuenta y marcar el token como usado")
	void verificarCorreoDebeActivarCuentaCuandoTokenEsValido() {
		User usuario = mock(User.class);
		when(usuario.isAccountNonLocked()).thenReturn(true);

		EmailVerificationToken verificationToken = new EmailVerificationToken(
				"token-valido", usuario, LocalDateTime.now().plusHours(12));
		when(tokenRepo.findByToken("token-valido")).thenReturn(Optional.of(verificationToken));

		emailVerificationService.verificarCorreo("  token-valido  ");

		verify(usuario).setEnabled(true);
		verify(userRepo).save(usuario);
		verify(tokenRepo).save(verificationToken);
		assertThat(verificationToken.isUsado()).isTrue();
	}

	/**
	 * Verifica que {@code verificarCorreo} lance {@link InvalidTokenException}
	 * cuando el token es nulo o está en blanco.
	 */
	@Test
	@DisplayName("verificarCorreo debe lanzar InvalidTokenException si el token está vacío")
	void verificarCorreoDebeFallarCuandoTokenEsBlanco() {
		assertThatThrownBy(() -> emailVerificationService.verificarCorreo("   "))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("El token es obligatorio");

		verifyNoInteractions(tokenRepo, userRepo);
	}

	/**
	 * Verifica que {@code verificarCorreo} lance {@link InvalidTokenException}
	 * cuando el token no existe en el repositorio.
	 */
	@Test
	@DisplayName("verificarCorreo debe lanzar InvalidTokenException si el token no existe")
	void verificarCorreoDebeFallarCuandoTokenNoExiste() {
		when(tokenRepo.findByToken("inexistente")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> emailVerificationService.verificarCorreo("inexistente"))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("Token inválido");
	}

	/**
	 * Verifica que {@code verificarCorreo} lance {@link InvalidTokenException}
	 * cuando el token ya expiró o fue usado.
	 */
	@Test
	@DisplayName("verificarCorreo debe lanzar InvalidTokenException si el token expiró")
	void verificarCorreoDebeFallarCuandoTokenExpiro() {
		User usuario = mock(User.class);
		EmailVerificationToken tokenExpirado = new EmailVerificationToken(
				"token-expirado", usuario, LocalDateTime.now().minusMinutes(5));
		when(tokenRepo.findByToken("token-expirado")).thenReturn(Optional.of(tokenExpirado));

		assertThatThrownBy(() -> emailVerificationService.verificarCorreo("token-expirado"))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("El token ya fue usado o expiró");
	}

	/**
	 * Verifica que {@code verificarCorreo} lance {@link IllegalStateException}
	 * cuando la cuenta del usuario está bloqueada.
	 */
	@Test
	@DisplayName("verificarCorreo debe lanzar IllegalStateException si la cuenta está bloqueada")
	void verificarCorreoDebeFallarCuandoCuentaEstaBloqueada() {
		User usuario = mock(User.class);
		when(usuario.isAccountNonLocked()).thenReturn(false);

		EmailVerificationToken verificationToken = new EmailVerificationToken(
				"token-bloqueado", usuario, LocalDateTime.now().plusHours(12));
		when(tokenRepo.findByToken("token-bloqueado")).thenReturn(Optional.of(verificationToken));

		assertThatThrownBy(() -> emailVerificationService.verificarCorreo("token-bloqueado"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La cuenta está bloqueada");
	}

	/**
	 * Verifica que {@code reenviarVerificacion} genere un nuevo token y envíe el
	 * correo cuando la cuenta existe y aún no está verificada.
	 */
	@Test
	@DisplayName("reenviarVerificacion debe generar nuevo token y enviar correo para cuenta inactiva")
	void reenviarVerificacionDebeEnviarNuevoEnlaceCuandoCuentaEsValida() {
		ResendVerificationRequestDTO request = mock(ResendVerificationRequestDTO.class);
		when(request.getCorreo()).thenReturn("  Usuario@Ejemplo.com  ");

		User usuario = mock(User.class);
		when(usuario.isEnabled()).thenReturn(false);
		when(usuario.isAccountNonLocked()).thenReturn(true);
		when(usuario.getEmail()).thenReturn("usuario@ejemplo.com");
		when(usuario.getUsername()).thenReturn("david");

		when(userRepo.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));

		emailVerificationService.reenviarVerificacion(request);

		verify(tokenRepo).deleteByUser(usuario);
		verify(tokenRepo).save(any(EmailVerificationToken.class));
		verify(emailService).enviarCorreoVerificacion(eq("usuario@ejemplo.com"), eq("david"), any(String.class));
	}

	/**
	 * Verifica que {@code reenviarVerificacion} lance {@link IllegalArgumentException}
	 * cuando el correo es nulo o vacío.
	 */
	@Test
	@DisplayName("reenviarVerificacion debe lanzar IllegalArgumentException si el correo está vacío")
	void reenviarVerificacionDebeFallarCuandoCorreoEsVacio() {
		ResendVerificationRequestDTO request = mock(ResendVerificationRequestDTO.class);
		when(request.getCorreo()).thenReturn(" ");

		assertThatThrownBy(() -> emailVerificationService.reenviarVerificacion(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El correo es obligatorio");

		verifyNoInteractions(userRepo, tokenRepo, emailService);
	}

	/**
	 * Verifica que {@code reenviarVerificacion} lance {@link UserNotFoundException}
	 * cuando no existe un usuario registrado con ese correo.
	 */
	@Test
	@DisplayName("reenviarVerificacion debe lanzar UserNotFoundException si el correo no existe")
	void reenviarVerificacionDebeFallarCuandoUsuarioNoExiste() {
		ResendVerificationRequestDTO request = mock(ResendVerificationRequestDTO.class);
		when(request.getCorreo()).thenReturn("noexiste@ejemplo.com");
		when(userRepo.findByEmail("noexiste@ejemplo.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> emailVerificationService.reenviarVerificacion(request))
				.isInstanceOf(UserNotFoundException.class);
	}

	/**
	 * Verifica que {@code reenviarVerificacion} lance {@link IllegalStateException}
	 * cuando la cuenta ya fue verificada previamente.
	 */
	@Test
	@DisplayName("reenviarVerificacion debe lanzar IllegalStateException si la cuenta ya está verificada")
	void reenviarVerificacionDebeFallarCuandoCuentaYaEstaVerificada() {
		ResendVerificationRequestDTO request = mock(ResendVerificationRequestDTO.class);
		when(request.getCorreo()).thenReturn("activo@ejemplo.com");

		User usuario = mock(User.class);
		when(usuario.isEnabled()).thenReturn(true);
		when(userRepo.findByEmail("activo@ejemplo.com")).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> emailVerificationService.reenviarVerificacion(request))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La cuenta ya se encuentra verificada");
	}
}
