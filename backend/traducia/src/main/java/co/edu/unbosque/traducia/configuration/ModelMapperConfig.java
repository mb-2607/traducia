package co.edu.unbosque.traducia.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.edu.unbosque.traducia.dto.AdminCreateUserRequestDTO;
import co.edu.unbosque.traducia.dto.LoginRequestDTO;
import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.User;

/**
 * Configuración encargada de definir las conversiones entre entidades
 * y objetos de transferencia de datos (DTO) mediante ModelMapper.
 * <p>
 * Establece una estrategia de correspondencia estricta entre propiedades
 * y define los mapeos personalizados utilizados para transformar objetos
 * de tipo User y los diferentes DTO de la aplicación.
 * </p>
 *
 * @author
 * @version 1.0
 * @see ModelMapper
 * @see MatchingStrategies
 * @see User
 * @see UserResponseDTO
 * @see LoginRequestDTO
 * @see RegisterRequestDTO
 * @see AdminCreateUserRequestDTO
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Configura y registra el componente ModelMapper como un bean de Spring.
     * <p>
     * Establece la estrategia de correspondencia STRICT y define las
     * conversiones personalizadas entre la entidad User y los DTO
     * utilizados para las operaciones de autenticación, registro
     * y administración de usuarios.
     * </p>
     *
     * @return instancia de ModelMapper configurada con las correspondencias
     *         personalizadas de la aplicación.
     */
    @Bean
    public ModelMapper modelMapper() {

        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.typeMap(User.class, UserResponseDTO.class).addMappings(m -> {
            m.map(User::getUsername, UserResponseDTO::setNombre);
            m.map(User::getEmail, UserResponseDTO::setCorreo);
            m.map(User::getRole, UserResponseDTO::setRol);
            m.map(User::getPlanType, UserResponseDTO::setPlanId);
        });

        mapper.typeMap(LoginRequestDTO.class, User.class).addMappings(m -> {
            m.map(LoginRequestDTO::getNombre, User::setUsername);
            m.map(LoginRequestDTO::getPassword, User::setPassword);
        });

        mapper.typeMap(RegisterRequestDTO.class, User.class).addMappings(m -> {
            m.map(RegisterRequestDTO::getNombre, User::setUsername);
            m.map(RegisterRequestDTO::getCorreo, User::setEmail);
            m.map(RegisterRequestDTO::getPassword, User::setPassword);
        });

        mapper.typeMap(AdminCreateUserRequestDTO.class, User.class).addMappings(m -> {
            m.map(AdminCreateUserRequestDTO::getNombre, User::setUsername);
            m.map(AdminCreateUserRequestDTO::getCorreo, User::setEmail);
            m.map(AdminCreateUserRequestDTO::getPassword, User::setPassword);
            m.map(AdminCreateUserRequestDTO::getRol, User::setRole);
            m.map(AdminCreateUserRequestDTO::getPlanId, User::setPlanType);
        });

        return mapper;
    }
}

