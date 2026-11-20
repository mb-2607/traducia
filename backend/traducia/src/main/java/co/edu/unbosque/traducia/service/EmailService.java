package co.edu.unbosque.traducia.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de enviar correos electrónicos del sistema.
 */
@Service


public class EmailService {
	/**
	 * Componente de Spring que envía correos por SMTP.
	 */
	private JavaMailSender mailSender;

	/**
	 * Correo remitente configurado en las propiedades.
	 */
	@Value("${spring.mail.username")
	private String remitente;

	/**
	 * Crea el componente con sus dependencias inyectadas por Spring.
	 *
	 * @param mailSender componente que envía los correos
	 */
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Envía el correo con el enlace para restablecer la contraseña.
	 *
	 * @param destinatario correo del usuario
	 * @param nombreUsuario nombre de usuario para el saludo
	 * @param enlace URL del frontend con el token de recuperación
	 */
	public void enviarCorreoRecuperacion(String destinatario, String nombreUsuario, String enlace) {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setFrom(remitente);
		mensaje.setTo(destinatario);
		mensaje.setSubject("Recuperación de contraseña - Traducia");
		mensaje.setText("Hola " + nombreUsuario + ",\n\n"
				+ "Ingresa al siguiente enlace para restablecer tu contraseña (válido por 30 minutos):\n\n"
				+ enlace + "\n\n"
				+ "Si no solicitaste este cambio, ignora este mensaje.");
		mailSender.send(mensaje);
	}

}
