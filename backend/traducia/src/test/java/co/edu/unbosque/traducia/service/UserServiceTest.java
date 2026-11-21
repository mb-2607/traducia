package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.traducia.dto.AdminChangePasswordRequestDTO;
import co.edu.unbosque.traducia.dto.AdminCreateUserRequestDTO;
import co.edu.unbosque.traducia.dto.RegisterRequestDTO;
import co.edu.unbosque.traducia.dto.UpdateUserRequestDTO;
import co.edu.unbosque.traducia.dto.UserResponseDTO;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.EmailAlreadyExistsException;
import co.edu.unbosque.traducia.exception.InvalidPasswordException;
import co.edu.unbosque.traducia.exception.UserNotFoundException;
import co.edu.unbosque.traducia.exception.UsernameAlreadyExistsException;
import co.edu.unbosque.traducia.repository.UserRepository;

/**
 * Pruebas unitarias para {@link UserService} utilizando JUnit 5, Mockito y
 * AssertJ.
 *
 * <p>Cubre el listado, paginación, consulta por id y por nombre, registro de
 * usuarios, creación administrativa, actualización, cambio de plan, cambio de
 * contraseña, eliminación y validaciones de unicidad y longitud de
 * contraseña.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepo;

	@Mock
	private ModelMapper modelMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailVerificationService emailVerificationService;

	@InjectMocks
	private UserService userService;

	/**
	 * Verifica que {@code listar} retorne todos los usuarios convertidos a
	 * {@link UserResponseDTO}.
	 */
	@Test
	@DisplayName("listar debe retornar la lista de usuarios mapeados a DTO")
	void listarDebeRetornarListaDeUsuariosMapeados() {
		User u1 = new User();
		User u2 = new User();
		UserResponseDTO dto1 = new UserResponseDTO();
		UserResponseDTO dto2 = new UserResponseDTO();

		when(userRepo.findAll()).thenReturn(List.of(u1, u2));
		when(modelMapper.map(u1, UserResponseDTO.class)).thenReturn(dto1);
		when(modelMapper.map(u2, UserResponseDTO.class)).thenReturn(dto2);

		List<UserResponseDTO> resultado = userService.listar();

		assertThat(resultado).containsExactly(dto1, dto2);
		verify(userRepo).findAll();
	}

	/**
	 * Verifica que {@code listar(Pageable)} retorne una página de
	 * {@link UserResponseDTO} preservando el contenido.
	 */
	@Test
	@DisplayName("listar paginado debe retornar Page de UserResponseDTO")
	void listarPaginadoDebeRetornarPaginaMapeada() {
		Pageable pageable = PageRequest.of(0, 5);
		User u1 = new User();
		UserResponseDTO dto1 = new UserResponseDTO();
		Page<User> paginaEntidades = new PageImpl<>(List.of(u1), pageable, 1);

		when(userRepo.findAll(pageable)).thenReturn(paginaEntidades);
		when(modelMapper.map(u1, UserResponseDTO.class)).thenReturn(dto1);

		Page<UserResponseDTO> resultado = userService.listar(pageable);

		assertThat(resultado.getContent()).containsExactly(dto1);
		assertThat(resultado.getTotalElements()).isEqualTo(1);
		verify(userRepo).findAll(pageable);
	}

	/**
	 * Verifica que {@code eliminar} borre el usuario cuando existe y lance
	 * {@link UserNotFoundException} cuando no existe.
	 */
	@Test
	@DisplayName("eliminar debe borrar el usuario si existe y lanzar excepción si no existe")
	void eliminarDebeBorrarSiExisteOLanzarExcepcion() {
		User usuario = new User();
		when(userRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(userRepo.findById(99L)).thenReturn(Optional.empty());

		userService.eliminar(1L);
		verify(userRepo, times(1)).delete(usuario);

		assertThatThrownBy(() -> userService.eliminar(99L))
				.isInstanceOf(UserNotFoundException.class);
		verify(userRepo, times(1)).delete(any(User.class));
	}

	/**
	 * Verifica que {@code crear(RegisterRequestDTO)} normalice el correo, asigne
	 * rol {@link Role#USER}, plan {@link PlanType#FREE}, deshabilite la cuenta,
	 * guarde el usuario y envíe el token de verificación.
	 */
	@Test
	@DisplayName("crear(RegisterRequestDTO) debe registrar usuario con rol USER, plan FREE y enviar token")
	void crearRegistroDebeGuardarUsuarioYEnviarToken() {
		RegisterRequestDTO request = new RegisterRequestDTO("dsantiago", "  DSANTIAGO@EJEMPLO.COM  ", "Password123!");
		User entidadMapeada = new User();
		User entidadGuardada = new User();
		UserResponseDTO responseEsperado = new UserResponseDTO();

		when(userRepo.existsByUsername("dsantiago")).thenReturn(false);
		when(userRepo.existsByEmail("dsantiago@ejemplo.com")).thenReturn(false);
		when(passwordEncoder.encode("Password123!")).thenReturn("encodedPass");
		when(modelMapper.map(request, User.class)).thenReturn(entidadMapeada);
		when(userRepo.save(entidadMapeada)).thenReturn(entidadGuardada);
		when(modelMapper.map(entidadGuardada, UserResponseDTO.class)).thenReturn(responseEsperado);

		UserResponseDTO resultado = userService.crear(request);

		assertThat(resultado).isSameAs(responseEsperado);
		assertThat(request.getCorreo()).isEqualTo("dsantiago@ejemplo.com");
		assertThat(request.getPassword()).isEqualTo("encodedPass");
		assertThat(entidadMapeada.getRole()).isEqualTo(Role.USER);
		assertThat(entidadMapeada.getPlanType()).isEqualTo(PlanType.FREE);
		assertThat(entidadMapeada.isEnabled()).isFalse();
		verify(emailVerificationService).generarYEnviarToken(entidadGuardada);
	}

	/**
	 * Verifica que {@code crear(AdminCreateUserRequestDTO)} guarde el usuario
	 * sin enviar token de verificación por correo.
	 */
	@Test
	@DisplayName("crear(AdminCreateUserRequestDTO) debe guardar usuario sin enviar token")
	void crearAdminDebeGuardarUsuarioSinEnviarToken() {
		AdminCreateUserRequestDTO request = new AdminCreateUserRequestDTO(
				"adminCreado", "ADMIN@EJEMPLO.COM", "Password123!", Role.ADMIN, PlanType.ORIA);
		User entidadMapeada = new User();
		User entidadGuardada = new User();
		UserResponseDTO responseEsperado = new UserResponseDTO();

		when(userRepo.existsByUsername("adminCreado")).thenReturn(false);
		when(userRepo.existsByEmail("admin@ejemplo.com")).thenReturn(false);
		when(passwordEncoder.encode("Password123!")).thenReturn("encodedAdminPass");
		when(modelMapper.map(request, User.class)).thenReturn(entidadMapeada);
		when(userRepo.save(entidadMapeada)).thenReturn(entidadGuardada);
		when(modelMapper.map(entidadGuardada, UserResponseDTO.class)).thenReturn(responseEsperado);

		UserResponseDTO resultado = userService.crear(request);

		assertThat(resultado).isSameAs(responseEsperado);
		assertThat(request.getCorreo()).isEqualTo("admin@ejemplo.com");
		verify(userRepo).save(entidadMapeada);
		verifyNoInteractions(emailVerificationService);
	}

	/**
	 * Verifica las consultas {@code obtenerPorId}, {@code obtenerEntidadPorNombre}
	 * y {@code obtenerPorNombre} tanto en escenarios exitosos como cuando el
	 * usuario no existe.
	 */
	@Test
	@DisplayName("obtenerPorId y obtenerPorNombre deben retornar DTO o lanzar UserNotFoundException")
	void consultasPorIdYNombreDebenFuncionarOLanzarExcepcion() {
		User usuario = new User();
		usuario.setUsername("dsantiago");
		UserResponseDTO dto = new UserResponseDTO();

		when(userRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(userRepo.findById(99L)).thenReturn(Optional.empty());
		when(modelMapper.map(usuario, UserResponseDTO.class)).thenReturn(dto);

		when(userRepo.findByUsername("dsantiago")).thenReturn(Optional.of(usuario));
		when(userRepo.findByUsername("inexistente")).thenReturn(Optional.empty());

		assertThat(userService.obtenerPorId(1L)).isSameAs(dto);
		assertThat(userService.obtenerEntidadPorNombre("dsantiago")).isSameAs(usuario);
		assertThat(userService.obtenerPorNombre("dsantiago")).isSameAs(dto);

		assertThatThrownBy(() -> userService.obtenerPorId(99L))
				.isInstanceOf(UserNotFoundException.class);
		assertThatThrownBy(() -> userService.obtenerEntidadPorNombre("inexistente"))
				.isInstanceOf(UserNotFoundException.class);
		assertThatThrownBy(() -> userService.obtenerPorNombre("inexistente"))
				.isInstanceOf(UserNotFoundException.class);
	}

	/**
	 * Verifica que {@code actualizar} modifique nombre, correo normalizado, plan y
	 * rol del usuario existente, y lance excepciones cuando no existe o hay
	 * duplicidad en otro usuario.
	 */
	@Test
	@DisplayName("actualizar debe modificar datos del usuario existente y validar duplicados")
	void actualizarDebeModificarUsuarioYValidarDuplicados() {
		User usuarioExistente = new User();
		usuarioExistente.setUsername("viejoNombre");
		usuarioExistente.setEmail("viejo@ejemplo.com");
		usuarioExistente.setRole(Role.USER);
		usuarioExistente.setPlanType(PlanType.FREE);

		UpdateUserRequestDTO request = new UpdateUserRequestDTO(
				"nuevoNombre", "  NUEVO@EJEMPLO.COM  ", Role.ADMIN, PlanType.INFINITIA);
		UserResponseDTO dtoEsperado = new UserResponseDTO();

		when(userRepo.findById(1L)).thenReturn(Optional.of(usuarioExistente));

		UpdateUserRequestDTO requestNombreDup = new UpdateUserRequestDTO(
				"nombreOcupado", "nuevo@ejemplo.com", Role.USER, PlanType.FREE);
		when(userRepo.existsByUsername("nombreOcupado")).thenReturn(true);
		assertThatThrownBy(() -> userService.actualizar(1L, requestNombreDup))
				.isInstanceOf(UsernameAlreadyExistsException.class);

		UpdateUserRequestDTO requestCorreoDup = new UpdateUserRequestDTO(
				"otroNombreLibre", "correo@ocupado.com", Role.USER, PlanType.FREE);
		when(userRepo.existsByUsername("otroNombreLibre")).thenReturn(false);
		when(userRepo.existsByEmail("correo@ocupado.com")).thenReturn(true);
		assertThatThrownBy(() -> userService.actualizar(1L, requestCorreoDup))
				.isInstanceOf(EmailAlreadyExistsException.class);

		when(userRepo.existsByUsername("nuevoNombre")).thenReturn(false);
		when(userRepo.existsByEmail("nuevo@ejemplo.com")).thenReturn(false);
		when(modelMapper.map(usuarioExistente, UserResponseDTO.class)).thenReturn(dtoEsperado);

		UserResponseDTO resultado = userService.actualizar(1L, request);

		assertThat(resultado).isSameAs(dtoEsperado);
		assertThat(usuarioExistente.getUsername()).isEqualTo("nuevoNombre");
		assertThat(usuarioExistente.getEmail()).isEqualTo("nuevo@ejemplo.com");
		assertThat(usuarioExistente.getRole()).isEqualTo(Role.ADMIN);
		assertThat(usuarioExistente.getPlanType()).isEqualTo(PlanType.INFINITIA);
		verify(userRepo).save(usuarioExistente);

		when(userRepo.findById(404L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> userService.actualizar(404L, request))
				.isInstanceOf(UserNotFoundException.class);
	}

	/**
	 * Verifica que {@code cambiarPlan} actualice el {@link PlanType} del usuario
	 * existente o lance {@link UserNotFoundException} si no existe.
	 */
	@Test
	@DisplayName("cambiarPlan debe actualizar el plan del usuario o lanzar UserNotFoundException")
	void cambiarPlanDebeActualizarPlanOLanzarExcepcion() {
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);
		UserResponseDTO dtoEsperado = new UserResponseDTO();

		when(userRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(userRepo.save(usuario)).thenReturn(usuario);
		when(modelMapper.map(usuario, UserResponseDTO.class)).thenReturn(dtoEsperado);
		when(userRepo.findById(99L)).thenReturn(Optional.empty());

		UserResponseDTO resultado = userService.cambiarPlan(1L, PlanType.INFINITIA);

		assertThat(resultado).isSameAs(dtoEsperado);
		assertThat(usuario.getPlanType()).isEqualTo(PlanType.INFINITIA);
		verify(userRepo).save(usuario);

		assertThatThrownBy(() -> userService.cambiarPlan(99L, PlanType.ORIA))
				.isInstanceOf(UserNotFoundException.class);
	}

	/**
	 * Verifica que {@code cambiarPassword} valide la contraseña, la codifique,
	 * desactive {@code debeCambiarPassword} y guarde los cambios.
	 */
	@Test
	@DisplayName("cambiarPassword debe codificar contraseña, desactivar bandera y guardar")
	void cambiarPasswordDebeActualizarPasswordYGuardar() {
		User usuario = new User();
		usuario.setDebeCambiarPassword(true);
		AdminChangePasswordRequestDTO request = new AdminChangePasswordRequestDTO();
		request.setNuevaPassword("NuevaClave123!");
		UserResponseDTO dtoEsperado = new UserResponseDTO();

		when(userRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(passwordEncoder.encode("NuevaClave123!")).thenReturn("nuevaEncodedPass");
		when(userRepo.save(usuario)).thenReturn(usuario);
		when(modelMapper.map(usuario, UserResponseDTO.class)).thenReturn(dtoEsperado);

		UserResponseDTO resultado = userService.cambiarPassword(1L, request);

		assertThat(resultado).isSameAs(dtoEsperado);
		assertThat(usuario.getPassword()).isEqualTo("nuevaEncodedPass");
		assertThat(usuario.isDebeCambiarPassword()).isFalse();
		verify(userRepo).save(usuario);
	}

	/**
	 * Verifica las reglas de {@code validarUsuario} y {@code validarPassword}
	 * ante usuario duplicado, correo duplicado o contraseña corta/nula.
	 */
	@Test
	@DisplayName("validarUsuario y validarPassword deben lanzar excepciones cuando correspondan")
	void validacionesDebenLanzarExcepcionesAnteDatosInvalidos() {
		RegisterRequestDTO dtoDuplicadoNombre = new RegisterRequestDTO("repetido", "ok@ejemplo.com", "Password123!");
		when(userRepo.existsByUsername("repetido")).thenReturn(true);

		assertThatThrownBy(() -> userService.validarUsuario(dtoDuplicadoNombre))
				.isInstanceOf(UsernameAlreadyExistsException.class);

		RegisterRequestDTO dtoDuplicadoCorreo = new RegisterRequestDTO("unico", "dup@ejemplo.com", "Password123!");
		when(userRepo.existsByUsername("unico")).thenReturn(false);
		when(userRepo.existsByEmail("dup@ejemplo.com")).thenReturn(true);

		assertThatThrownBy(() -> userService.validarUsuario(dtoDuplicadoCorreo))
				.isInstanceOf(EmailAlreadyExistsException.class);

		assertThatThrownBy(() -> userService.validarPassword("1234567"))
				.isInstanceOf(InvalidPasswordException.class);
		assertThatThrownBy(() -> userService.validarPassword(null))
				.isInstanceOf(InvalidPasswordException.class);
	}
}