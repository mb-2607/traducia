package co.edu.unbosque.traducia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.User;

/**
 * Repositorio encargado de gestionar las operaciones de persistencia de los usuarios.
 * <p>
 * Extiende {@link JpaRepository} para heredar operaciones CRUD, consultas,
 * eliminación, paginación y ordenamiento sobre la entidad {@link User}.
 * También define métodos de consulta personalizados que Spring Data JPA
 * implementa automáticamente a partir de sus nombres.
 *
 * @author
 * @version 1.0
 * @see User
 * @see JpaRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario a partir de su nombre de usuario.
     *
     * @param username nombre de usuario que se desea consultar.
     * @return un {@link Optional} que contiene el usuario encontrado o está vacío
     *         si no existe un usuario con ese nombre.
     */
    public Optional<User> findByUsername(String username);

    /**
     * Elimina un usuario de la base de datos a partir de su nombre de usuario.
     *
     * @param username nombre de usuario del registro que se desea eliminar.
     */
    public void deleteByUsername(String username);

    /**
     * Comprueba si existe un usuario con el nombre de usuario indicado.
     *
     * @param userName nombre de usuario que se desea verificar.
     * @return {@code true} si existe un usuario con ese nombre; {@code false} en caso contrario.
     */
    public boolean existsByUsername(String userName);

    /**
     * Comprueba si existe un usuario registrado con el correo electrónico indicado.
     *
     * @param email dirección de correo electrónico que se desea verificar.
     * @return {@code true} si existe un usuario con ese correo; {@code false} en caso contrario.
     */
    public boolean existsByEmail(String email);
    
    /**
	 * Busca un usuario registrado a partir de su dirección de correo electrónico.
	 *
	 * @param email correo electrónico del usuario que se desea consultar.
	 * @return un {@link Optional} que contiene el {@link User} encontrado si existe, o vacío en caso contrario.
	 */
	
    Optional<User> findByEmail(String email);

}