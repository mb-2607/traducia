package co.edu.unbosque.traducia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.dto.AdminCreateUserRequestDTO;
import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UpdateUserRequestDTO;
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
	
	public List<UserResponseDTO> listar() {
		List<User> usuarios = userRepo.findAll();
		List<UserResponseDTO> respuesta = new ArrayList<>();

		usuarios.forEach((usuario) -> {
			UserResponseDTO dto = modelMapper.map(usuario, UserResponseDTO.class);
			respuesta.add(dto);
		});

		return respuesta;
	}
	
	public Page<UserResponseDTO> listar(Pageable pageable) {
		Page<User> usuarios = userRepo.findAll(pageable);

		return usuarios.map((usuario) -> modelMapper.map(usuario, UserResponseDTO.class));
	}
	
	public void eliminar(Long id) {
		Optional<User> usuario = userRepo.findById(id);
		if (usuario.isEmpty()) {
			throw new UserNotFoundException();
		}

		userRepo.delete(usuario.get());
	}
	
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
	
	public UserResponseDTO crear(AdminCreateUserRequestDTO register) {
		register.setCorreo(register.getCorreo().trim().toLowerCase());
		validarUsuario(register);
		register.setPassword(passwordEncoder.encode(register.getPassword()));
		
		User usuario = modelMapper.map(register, User.class);
		
		usuario = userRepo.save(usuario);
		
		return modelMapper.map(usuario, UserResponseDTO.class);
	}
	
	public UserResponseDTO obtenerPorId(Long id) {
		Optional<User> usuario = userRepo.findById(id);
		if(usuario.isEmpty()) {
			throw new UserNotFoundException();
		}
		
		UserResponseDTO respuesta = modelMapper.map(usuario.get(), UserResponseDTO.class);
		return respuesta;
	}
	
	public UserResponseDTO actualizar(Long id, UpdateUserRequestDTO register) {
		register.setCorreo(register.getCorreo().trim().toLowerCase());
		
		Optional<User> usuario = userRepo.findById(id);
		if(usuario.isEmpty()) {
			throw new UserNotFoundException();
		}
		
		validarUsuario(register, usuario.get());
		
		usuario.get().setUsername(register.getNombre());
		usuario.get().setEmail(register.getCorreo());
		usuario.get().setPlanType(PlanType.valueOf(register.getPlanId()));
		usuario.get().setRole(Role.valueOf(register.getRol()));
		
		userRepo.save(usuario.get());
		
		return modelMapper.map(usuario, UserResponseDTO.class);
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
	
	public void validarUsuario(UpdateUserRequestDTO register, User user) {
		if(userRepo.existsByUsername(register.getNombre()) && !user.getUsername().equals(register.getNombre())) {
			throw new UsernameAlreadyExistsException(register.getNombre());
		}
		if(userRepo.existsByEmail(register.getCorreo()) && !user.getEmail().equals(register.getCorreo())) {
			throw new EmailAlreadyExistsException(register.getCorreo());
		}
	}
	
	public void validarPassword(String password) {
		if(password == null || password.length() < 8) {
			throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
		}
	}
}
