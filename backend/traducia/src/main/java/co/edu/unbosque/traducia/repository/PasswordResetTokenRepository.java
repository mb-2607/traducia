package co.edu.unbosque.traducia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.PasswordResetToken;
import co.edu.unbosque.traducia.entity.User;

/**
 * Repositorio JPA para consultar y gestionar tokens de recuperación de contraseña.
 */

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
	/**
	 * Busca un token a partir de su valor único.
	 *
	 * @param token código recibido desde el enlace
	 * @return token encontrado si existe
	 */
	Optional<PasswordResetToken> findByToken(String token);

	/**
	 * Elimina los tokens anteriores asociados a un usuario.
	 *
	 * @param user usuario cuyos tokens previos se van a borrar
	 */
	void deleteByUser(User user);

}
