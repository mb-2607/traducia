package co.edu.unbosque.traducia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.EmailVerificationToken;
import co.edu.unbosque.traducia.entity.User;

/**
 * Repositorio JPA para gestionar los tokens de verificación de correo electrónico.
 */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	/**
	 * Busca un token de verificación por su código único.
	 *
	 * @param token código del token
	 * @return token encontrado si existe
	 */
	Optional<EmailVerificationToken> findByToken(String token);

	/**
	 * Elimina todos los tokens de verificación asociados a un usuario.
	 *
	 * @param user usuario dueño de los tokens
	 */
	void deleteByUser(User user);
}