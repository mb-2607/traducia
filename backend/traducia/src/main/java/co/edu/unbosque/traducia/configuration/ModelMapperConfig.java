package co.edu.unbosque.traducia.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
			m.map(src -> src.getRole().name(), UserResponseDTO::setRol);
			m.map(src -> src.getPlanType().name(), UserResponseDTO::setPlanId);
		});
		
		mapper.typeMap(UserResponseDTO.class, User.class).addMappings(m -> {
		    m.map(UserResponseDTO::getNombre, User::setUsername);
		    m.map(UserResponseDTO::getCorreo, User::setEmail);
		    m.map(UserResponseDTO::getRol, User::setRole);
		    m.map(UserResponseDTO::getPlanId, User::setPlanType);
		});

		return mapper;
	}
}
