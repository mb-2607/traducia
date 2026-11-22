package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
 * Pruebas unitarias para {@link PasswordResetService} utilizando JUnit 5,
 * Mockito y AssertJ.
 *
 * <p>Valida la generación y envío de tokens de recuperación de contraseña, así
 * como la verificación del token, validación de la nueva contraseña y
 * actualización del usuario aislando repositorios y servicios externos.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private PasswordResetTokenRepository tokenRepo;

	@Mock
	private UserService userService;

	@Mock
	private EmailService emailService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private PasswordResetService passwordResetService;

	/**
	 * Inicializa la propiedad {@code frontUrl} antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(passwordResetService, "frontUrl", "http://localhost:4200/");
	}

	/**
	 * Verifica que {@code solicitarRecuperacion} elimine tokens previos, guarde un
	 * nuevo token vigente y envíe el correo con el enlace de recuperación.
	 */
	@Test
	@DisplayName("solicitarRecuperacion debe generar token y enviar correo para cuenta activa")
	void solicitarRecuperacionDebeGenerarTokenYEnviarCorreo() {
		ForgotPasswordRequestDTO request = mock(ForgotPasswordRequestDTO.class);
		when(request.getCorreo()).thenReturn("  Usuario@Ejemplo.com  ");

		User usuario = mock(User.class);
		when(usuario.isEnabled()).thenReturn(true);
		when(usuario.isAccountNonLocked()).thenReturn(true);
		when(usuario.getEmail()).thenReturn("usuario@ejemplo.com");
		when(usuario.getUsername()).thenReturn("david");

		when(userRepo.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));

		passwordResetService.solicitarRecuperacion(request);

		ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
		ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);

		verify(tokenRepo).deleteByUser(usuario);
		verify(tokenRepo).save(tokenCaptor.capture());
		verify(emailService).enviarCorreoRecuperacion(eq("usuario@ejemplo.com"), eq("david"), enlaceCaptor.capture());

		PasswordResetToken tokenGuardado = tokenCaptor.getValue();
		assertThat(tokenGuardado.getUser()).isSameAs(usuario);
		assertThat(tokenGuardado.isUsado()).isFalse();
		assertThat(tokenGuardado.estaExpirado()).isFalse();
		assertThat(enlaceCaptor.getValue()).startsWith("http://localhost:4200/restablecer-contrasena?token=");
	}

	/**
	 * Verifica que {@code solicitarRecuperacion} lance
	 * {@link IllegalArgumentException} cuando el correo está vacío o en blanco.
	 */
	@Test
	@DisplayName("solicitarRecuperacion debe lanzar IllegalArgumentException si el correo está vacío")
	void solicitarRecuperacionDebeFallarCuandoCorreoEsVacio() {
		ForgotPasswordRequestDTO request = mock(ForgotPasswordRequestDTO.class);
		when(request.getCorreo()).thenReturn("   ");

		assertThatThrownBy(() -> passwordResetService.solicitarRecuperacion(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El correo es obligatorio");

		verifyNoInteractions(userRepo, tokenRepo, emailService);
	}

	/**
	 * Verifica que {@code solicitarRecuperacion} lance
	 * {@link UserNotFoundException} cuando no existe un usuario con ese correo.
	 */
	@Test
	@DisplayName("solicitarRecuperacion debe lanzar UserNotFoundException si el usuario no existe")
	void solicitarRecuperacionDebeFallarCuandoUsuarioNoExiste() {
		ForgotPasswordRequestDTO request = mock(ForgotPasswordRequestDTO.class);
		when(request.getCorreo()).thenReturn("noexiste@ejemplo.com");
		when(userRepo.findByEmail("noexiste@ejemplo.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> passwordResetService.solicitarRecuperacion(request))
				.isInstanceOf(UserNotFoundException.class);
	}

	/**
	 * Verifica que {@code solicitarRecuperacion} lance
	 * {@link IllegalStateException} cuando la cuenta está deshabilitada o bloqueada.
	 */
	@Test
	@DisplayName("solicitarRecuperacion debe lanzar IllegalStateException si la cuenta está inactiva")
	void solicitarRecuperacionDebeFallarCuandoCuentaEstaInactiva() {
		ForgotPasswordRequestDTO request = mock(ForgotPasswordRequestDTO.class);
		when(request.getCorreo()).thenReturn("inactivo@ejemplo.com");

		User usuario = mock(User.class);
		when(usuario.isEnabled()).thenReturn(false);
		when(userRepo.findByEmail("inactivo@ejemplo.com")).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> passwordResetService.solicitarRecuperacion(request))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La cuenta no está habilitada o está bloqueada");

		verifyNoInteractions(tokenRepo, emailService);
	}

	/**
	 * Verifica que {@code restablecerPassword} valide y codifique la nueva
	 * contraseña, actualice al usuario y marque el token como usado.
	 */
	@Test
	@DisplayName("restablecerPassword debe actualizar la contraseña y marcar el token como usado")
	void restablecerPasswordDebeActualizarPasswordCuandoTokenEsValido() {
		ResetPasswordRequestDTO request = mock(ResetPasswordRequestDTO.class);
		when(request.getToken()).thenReturn("  token-valido  ");
		when(request.getNuevaPassword()).thenReturn("NuevaClave123!");

		User usuario = mock(User.class);
		when(usuario.isEnabled()).thenReturn(true);
		when(usuario.isAccountNonLocked()).thenReturn(true);

		PasswordResetToken resetToken = new PasswordResetToken(
				"token-valido", usuario, LocalDateTime.now().plusMinutes(30));

		when(tokenRepo.findByToken("token-valido")).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.encode("NuevaClave123!")).thenReturn("clave-codificada");

		passwordResetService.restablecerPassword(request);

		verify(userService).validarPassword("NuevaClave123!");
		verify(usuario).setPassword("clave-codificada");
		verify(usuario).setDebeCambiarPassword(false);
		verify(userRepo).save(usuario);
		verify(tokenRepo).save(resetToken);
		assertThat(resetToken.isUsado()).isTrue();
	}

	/**
	 * Verifica que {@code restablecerPassword} lance {@link InvalidTokenException}
	 * cuando el token es nulo o está en blanco.
	 */
	@Test
	@DisplayName("restablecerPassword debe lanzar InvalidTokenException si el token está vacío")
	void restablecerPasswordDebeFallarCuandoTokenEsVacio() {
		ResetPasswordRequestDTO request = mock(ResetPasswordRequestDTO.class);
		when(request.getToken()).thenReturn(" ");

		assertThatThrownBy(() -> passwordResetService.restablecerPassword(request))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("El token es obligatorio");

		verifyNoInteractions(tokenRepo, userRepo, userService, passwordEncoder);
	}

	/**
	 * Verifica que {@code restablecerPassword} lance {@link InvalidTokenException}
	 * cuando el token no existe en el repositorio.
	 */
	@Test
	@DisplayName("restablecerPassword debe lanzar InvalidTokenException si el token no existe")
	void restablecerPasswordDebeFallarCuandoTokenNoExiste() {
		ResetPasswordRequestDTO request = mock(ResetPasswordRequestDTO.class);
		when(request.getToken()).thenReturn("inexistente");
		when(tokenRepo.findByToken("inexistente")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> passwordResetService.restablecerPassword(request))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("Token inválido");
	}

	/**
	 * Verifica que {@code restablecerPassword} lance {@link InvalidTokenException}
	 * cuando el token ya expiró o fue utilizado.
	 */
	@Test
	@DisplayName("restablecerPassword debe lanzar InvalidTokenException si el token expiró")
	void restablecerPasswordDebeFallarCuandoTokenExpiro() {
		ResetPasswordRequestDTO request = mock(ResetPasswordRequestDTO.class);
		when(request.getToken()).thenReturn("token-expirado");

		User usuario = mock(User.class);
		PasswordResetToken tokenExpirado = new PasswordResetToken(
				"token-expirado", usuario, LocalDateTime.now().minusMinutes(5));
		when(tokenRepo.findByToken("token-expirado")).thenReturn(Optional.of(tokenExpirado));

		assertThatThrownBy(() -> passwordResetService.restablecerPassword(request))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("El token ya fue usado o expiró");
	}

	/**
	 * Verifica que {@code restablecerPassword} propague
	 * {@link InvalidPasswordException} y no guarde cambios si la nueva contraseña
	 * incumple las reglas de validación.
	 */
	@Test
	@DisplayName("restablecerPassword debe lanzar InvalidPasswordException si la contraseña no es válida")
	void restablecerPasswordDebeFallarCuandoPasswordEsInvalida() {
		ResetPasswordRequestDTO request = mock(ResetPasswordRequestDTO.class);
		when(request.getToken()).thenReturn("token-valido");
		when(request.getNuevaPassword()).thenReturn("123");

		User usuario = mock(User.class);
		when(usuario.isEnabled()).thenReturn(true);
		when(usuario.isAccountNonLocked()).thenReturn(true);

		PasswordResetToken resetToken = new PasswordResetToken(
				"token-valido", usuario, LocalDateTime.now().plusMinutes(30));

		when(tokenRepo.findByToken("token-valido")).thenReturn(Optional.of(resetToken));
		doThrow(new InvalidPasswordException("La contraseña es muy corta"))
				.when(userService).validarPassword("123");

		assertThatThrownBy(() -> passwordResetService.restablecerPassword(request))
				.isInstanceOf(InvalidPasswordException.class)
				.hasMessage("La contraseña es muy corta");

		verify(userRepo, never()).save(usuario);
		verify(tokenRepo, never()).save(resetToken);
	}
}