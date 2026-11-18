package co.edu.unbosque.traducia.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.repository.UserRepository;

/**
 * Servicio encargado de proporcionar los detalles de los usuarios a Spring Security.
 * <p>
 * Implementa {@link UserDetailsService} para consultar los usuarios almacenados
 * en la base de datos mediante su nombre de usuario. Si el usuario no existe,
 * lanza una excepción {@link UsernameNotFoundException}.
 *
 * @author
 * @version 1.0
 * @see UserDetailsService
 * @see UserRepository
 * @see UserDetails
 * @see UsernameNotFoundException
 */
@Service
public class TraduciaUserDetailsService implements UserDetailsService {

    /**
     * Repositorio encargado de realizar las consultas de usuarios en la base de datos.
     */
    private final UserRepository userRepository;

    /**
     * Construye el servicio e inyecta el repositorio de usuarios.
     *
     * @param userRepository repositorio utilizado para consultar los usuarios.
     */
    public TraduciaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Busca un usuario en la base de datos a partir de su nombre de usuario.
     * <p>
     * Si encuentra el usuario, devuelve la entidad correspondiente como
     * {@link UserDetails}. En caso contrario, lanza una excepción indicando
     * que no se encontró el usuario.
     *
     * @param username nombre de usuario que se desea buscar.
     * @return detalles del usuario encontrado, utilizados por Spring Security
     *         durante la autenticación.
     * @throws UsernameNotFoundException si no existe un usuario con el nombre
     *         de usuario proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username));
    }
}