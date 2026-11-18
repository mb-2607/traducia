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

@Configuration
public class ModelMapperConfig {

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
