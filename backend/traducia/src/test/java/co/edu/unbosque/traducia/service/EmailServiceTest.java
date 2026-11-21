package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Pruebas unitarias para {@link EmailService} utilizando JUnit 5, Mockito y
 * AssertJ.
 *
 * <p>Valida el envío de correos de recuperación de contraseña y de
 * verificación de cuenta, comprobando remitente, destinatario, asunto y
 * cuerpo del mensaje enviado a través de {@link JavaMailSender}.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private EmailService emailService;

	private static final String REMITENTE_PRUEBA = "soporte@traducia.com";

	/**
	 * Asigna el remitente simulado al campo anotado con {@code @Value} antes de
	 * cada prueba unitaria.
	 */
	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(emailService, "remitente", REMITENTE_PRUEBA);
	}

	/**
	 * Verifica que {@code enviarCorreoRecuperacion} construya y envíe un
	 * {@link SimpleMailMessage} con el remitente, destinatario, asunto,
	 * vigencia de 30 minutos y enlace de recuperación esperados.
	 */
	@Test
	@DisplayName("enviarCorreoRecuperacion debe enviar mensaje con asunto y enlace de recuperación")
	void enviarCorreoRecuperacionDebeEnviarMensajeCorrecto() {
		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		emailService.enviarCorreoRecuperacion(
				"usuario@ejemplo.com",
				"dsantiago",
				"http://localhost:4200/recuperar?token=abc123");

		verify(mailSender, times(1)).send(captor.capture());
		SimpleMailMessage enviado = captor.getValue();

		assertThat(enviado.getFrom()).isEqualTo(REMITENTE_PRUEBA);
		assertThat(enviado.getTo()).containsExactly("usuario@ejemplo.com");
		assertThat(enviado.getSubject()).isEqualTo("Recuperación de contraseña - Traducia");
		assertThat(enviado.getText())
				.contains("Hola dsantiago")
				.contains("30 minutos")
				.contains("http://localhost:4200/recuperar?token=abc123");
	}

	/**
	 * Verifica que {@code enviarCorreoVerificacion} construya y envíe un
	 * {@link SimpleMailMessage} con el remitente, destinatario, asunto,
	 * vigencia de 12 horas y enlace de verificación esperados.
	 */
	@Test
	@DisplayName("enviarCorreoVerificacion debe enviar mensaje con asunto y enlace de verificación")
	void enviarCorreoVerificacionDebeEnviarMensajeCorrecto() {
		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		emailService.enviarCorreoVerificacion(
				"nuevo@ejemplo.com",
				"mbarros",
				"http://localhost:4200/verificar?token=xyz789");

		verify(mailSender, times(1)).send(captor.capture());
		SimpleMailMessage enviado = captor.getValue();

		assertThat(enviado.getFrom()).isEqualTo(REMITENTE_PRUEBA);
		assertThat(enviado.getTo()).containsExactly("nuevo@ejemplo.com");
		assertThat(enviado.getSubject()).isEqualTo("Verifica tu correo - Traducia");
		assertThat(enviado.getText())
				.contains("Hola mbarros")
				.contains("12 horas")
				.contains("http://localhost:4200/verificar?token=xyz789");
	}
}