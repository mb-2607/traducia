package co.edu.unbosque.traducia.service;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UserAdminResponseDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	public UserResponseDTO crear(RegisterRequestDTO register) {
		
		register.setCorreo(register.getCorreo().trim().toLowerCase());
		validarUsuario(register);
		register.setPassword(passwordEncoder.encode(register.getPassword()));
		
		User usuario = modelMapper.map(register, User.class);
		usuario.setRole(Role.USER);
		usuario.setPlanType(PlanType.FREE);
		
		usuario = userRepo.save(usuario);
		
		return modelMapper.map(usuario, UserResponseDTO.class);
	}
	
	public UserAdminResponseDTO obtenerPorId(Long id) {
		Optional<User> usuario = userRepo.findById(id);
		if(usuario.isEmpty()) {
			throw new UserNotFoundException();
		}
		
		UserAdminResponseDTO respuesta = modelMapper.map(usuario.get(), UserAdminResponseDTO.class);
		return respuesta;
	}
	
	public void validarUsuario(RegisterRequestDTO register) {
		if(userRepo.existsByUsername(register.getNombre())) {
			throw new UsernameAlreadyExistsException(register.getNombre());
		}
		if(userRepo.existsByEmail(register.getCorreo())) {
			throw new EmailAlreadyExistsException(register.getCorreo());
		}
		
		validarPassword(register.getPassword());
	}
	
	public void validarPassword(String password) {
		if(password == null || password.length() < 8) {
			throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
		}
	}
}
