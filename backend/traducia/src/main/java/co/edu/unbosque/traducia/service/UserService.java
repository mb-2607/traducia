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
import org.springframework.transaction.annotation.Transactional;

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
 * Servicio encargado de gestionar las operaciones relacionadas con los usuarios.
 * <p>
 * Proporciona funcionalidades para registrar, consultar, actualizar, eliminar,
 * cambiar el plan y actualizar la contraseña de los usuarios, además de validar
 * la información suministrada durante su creación y actualización. Utiliza
 * {@link UserRepository} para acceder a la base de datos, {@link ModelMapper}
 * para convertir entidades en DTOs, {@link PasswordEncoder} para codificar las
 * contraseñas y {@link EmailVerificationService} para gestionar el envío de
 * enlaces de verificación de correo.
 * </p>
 *
 * @version 1.0
 * @see User
 * @see UserRepository
 * @see UserResponseDTO
 * @see RegisterRequestDTO
 * @see AdminCreateUserRequestDTO
 * @see AdminChangePasswordRequestDTO
 * @see UpdateUserRequestDTO
 * @see EmailVerificationService
 */
@Service
public class UserService {

    /**
     * Repositorio encargado de realizar las operaciones de persistencia de los usuarios.
     */
    @Autowired
    UserRepository userRepo;

    /**
     * Componente utilizado para convertir entidades y objetos DTO.
     */
    @Autowired
    ModelMapper modelMapper;

    /**
     * Componente encargado de codificar las contraseñas antes de almacenarlas.
     */
    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Servicio encargado de generar y enviar tokens de verificación de correo.
     */
    @Autowired
    EmailVerificationService emailVerificationService;

    /**
     * Obtiene todos los usuarios registrados en la base de datos.
     * <p>
     * Convierte cada entidad {@link User} en un objeto {@link UserResponseDTO}
     * antes de devolver la lista.
     * </p>
     *
     * @return lista de usuarios representados mediante DTOs.
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> listar() {
        List<User> usuarios = userRepo.findAll();
        List<UserResponseDTO> respuesta = new ArrayList<>();
        usuarios.forEach((usuario) -> {
            UserResponseDTO dto = modelMapper.map(usuario, UserResponseDTO.class);
            respuesta.add(dto);
        });
        return respuesta;
    }

    /**
     * Obtiene los usuarios registrados mediante una consulta paginada.
     * <p>
     * Convierte los elementos de la página de entidades {@link User} en objetos
     * {@link UserResponseDTO}, conservando la información de paginación.
     * </p>
     *
     * @param pageable configuración de paginación y ordenamiento de los resultados.
     * @return página de usuarios representados mediante DTOs.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listar(Pageable pageable) {
        Page<User> usuarios = userRepo.findAll(pageable);
        return usuarios.map((usuario) -> modelMapper.map(usuario, UserResponseDTO.class));
    }

    /**
     * Elimina un usuario de la base de datos a partir de su identificador.
     *
     * @param id identificador del usuario que se desea eliminar.
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado.
     */
    @Transactional
    public void eliminar(Long id) {
        Optional<User> usuario = userRepo.findById(id);
        if (usuario.isEmpty()) {
            throw new UserNotFoundException();
        }
        userRepo.delete(usuario.get());
    }

    /**
     * Registra un nuevo usuario mediante los datos de una solicitud de registro.
     * <p>
     * Normaliza el correo electrónico, valida que el nombre de usuario y el correo
     * no estén registrados, verifica la longitud de la contraseña y la codifica.
     * Asigna automáticamente el rol {@link Role#USER}, el plan {@link PlanType#FREE},
     * deshabilita la cuenta hasta verificar el correo y envía el token de activación.
     * </p>
     *
     * @param register objeto con los datos necesarios para registrar al usuario.
     * @return información del usuario creado, representada mediante un DTO.
     * @throws UsernameAlreadyExistsException si el nombre de usuario ya está registrado.
     * @throws EmailAlreadyExistsException si el correo electrónico ya está registrado.
     * @throws InvalidPasswordException si la contraseña no cumple la longitud mínima requerida.
     */
    @Transactional
    public UserResponseDTO crear(RegisterRequestDTO register) {
        register.setCorreo(register.getCorreo().trim().toLowerCase());
        validarUsuario(register);
        register.setPassword(passwordEncoder.encode(register.getPassword()));
        User usuario = modelMapper.map(register, User.class);
        usuario.setRole(Role.USER);
        usuario.setPlanType(PlanType.FREE);
        usuario.setEnabled(false);
        usuario = userRepo.save(usuario);
        emailVerificationService.generarYEnviarToken(usuario);
        return modelMapper.map(usuario, UserResponseDTO.class);
    }

    /**
     * Registra un nuevo usuario mediante una solicitud de creación administrativa.
     * <p>
     * Normaliza el correo electrónico, valida que el nombre de usuario y el correo
     * no estén registrados, verifica la contraseña y la codifica antes de guardar
     * el usuario. Los demás atributos se obtienen del DTO recibido.
     * </p>
     *
     * @param register objeto con los datos del usuario que se desea crear.
     * @return información del usuario creado, representada mediante un DTO.
     * @throws UsernameAlreadyExistsException si el nombre de usuario ya está registrado.
     * @throws EmailAlreadyExistsException si el correo electrónico ya está registrado.
     * @throws InvalidPasswordException si la contraseña no cumple la longitud mínima requerida.
     */
    @Transactional
    public UserResponseDTO crear(AdminCreateUserRequestDTO register) {
        register.setCorreo(register.getCorreo().trim().toLowerCase());
        validarUsuario(register);
        register.setPassword(passwordEncoder.encode(register.getPassword()));
        User usuario = modelMapper.map(register, User.class);
        usuario = userRepo.save(usuario);
        return modelMapper.map(usuario, UserResponseDTO.class);
    }

    /**
     * Obtiene la información de un usuario a partir de su identificador.
     *
     * @param id identificador del usuario que se desea consultar.
     * @return información del usuario encontrado, representada mediante un DTO.
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO obtenerPorId(Long id) {
        Optional<User> usuario = userRepo.findById(id);
        if (usuario.isEmpty()) {
            throw new UserNotFoundException();
        }
        return modelMapper.map(usuario.get(), UserResponseDTO.class);
    }

    /**
     * Obtiene la entidad usuario a partir de su nombre.
     *
     * @param nombre nombre del usuario que se desea consultar.
     * @return entidad del usuario.
     * @throws UserNotFoundException si no existe un usuario con el nombre indicado.
     */
    @Transactional(readOnly = true)
    public User obtenerEntidadPorNombre(String nombre) {
        Optional<User> usuario = userRepo.findByUsername(nombre);
        if (usuario.isEmpty()) {
            throw new UserNotFoundException();
        }
        return usuario.get();
    }

    /**
     * Obtiene la información de un usuario a partir de su nombre.
     *
     * @param nombre nombre del usuario que se desea consultar.
     * @return información del usuario encontrado, representada mediante un DTO.
     * @throws UserNotFoundException si no existe un usuario con el nombre indicado.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO obtenerPorNombre(String nombre) {
        Optional<User> usuario = userRepo.findByUsername(nombre);
        if (usuario.isEmpty()) {
            throw new UserNotFoundException();
        }
        return modelMapper.map(usuario.get(), UserResponseDTO.class);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * <p>
     * Normaliza el correo electrónico y verifica que el usuario exista.
     * Posteriormente, valida que el nuevo nombre de usuario y correo no estén
     * registrados por otros usuarios, actualiza sus atributos y guarda los cambios.
     * </p>
     *
     * @param id identificador del usuario que se desea actualizar.
     * @param register objeto con los nuevos datos del usuario.
     * @return información actualizada del usuario, representada mediante un DTO.
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado.
     * @throws UsernameAlreadyExistsException si el nombre de usuario pertenece a otro usuario.
     * @throws EmailAlreadyExistsException si el correo electrónico pertenece a otro usuario.
     */
    @Transactional
    public UserResponseDTO actualizar(Long id, UpdateUserRequestDTO register) {
        register.setCorreo(register.getCorreo().trim().toLowerCase());
        Optional<User> usuario = userRepo.findById(id);
        if (usuario.isEmpty()) {
            throw new UserNotFoundException();
        }
        validarUsuario(register, usuario.get());
        usuario.get().setUsername(register.getNombre());
        usuario.get().setEmail(register.getCorreo());
        usuario.get().setPlanType(register.getPlanId());
        usuario.get().setRole(register.getRol());
        userRepo.save(usuario.get());
        return modelMapper.map(usuario.get(), UserResponseDTO.class);
    }

    /**
     * Actualiza el plan de suscripción asignado a un usuario existente.
     *
     * @param id identificador del usuario al que se le cambiará el plan.
     * @param planType nuevo tipo de plan que se asignará al usuario.
     * @return información actualizada del usuario, representada mediante un DTO.
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado.
     */
    @Transactional
    public UserResponseDTO cambiarPlan(Long id, PlanType planType) {
        Optional<User> usuarioOpt = userRepo.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new UserNotFoundException();
        }
        User usuario = usuarioOpt.get();
        usuario.setPlanType(planType);
        userRepo.save(usuario);
        return modelMapper.map(usuario, UserResponseDTO.class);
    }

    /**
     * Cambia la contraseña de un usuario y desactiva la bandera de cambio obligatorio.
     * <p>
     * Verifica la existencia del usuario, valida la longitud mínima de la nueva
     * contraseña, la codifica con {@link PasswordEncoder} y guarda los cambios.
     * </p>
     *
     * @param id identificador del usuario al que se le actualizará la contraseña.
     * @param request objeto con la nueva contraseña solicitada.
     * @return información actualizada del usuario, representada mediante un DTO.
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado.
     * @throws InvalidPasswordException si la nueva contraseña no cumple la longitud mínima requerida.
     */
    @Transactional
    public UserResponseDTO cambiarPassword(Long id, AdminChangePasswordRequestDTO request) {
        Optional<User> usuarioOpt = userRepo.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new UserNotFoundException();
        }
        validarPassword(request.getNuevaPassword());
        User usuario = usuarioOpt.get();
        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setDebeCambiarPassword(false);
        userRepo.save(usuario);
        return modelMapper.map(usuario, UserResponseDTO.class);
    }

    /**
     * Valida los datos de un usuario durante su registro.
     * <p>
     * Comprueba que el nombre de usuario y el correo electrónico no estén
     * registrados previamente. También verifica que la contraseña tenga
     * al menos ocho caracteres.
     * </p>
     *
     * @param register objeto con los datos del usuario que se desea validar.
     * @throws UsernameAlreadyExistsException si el nombre de usuario ya está registrado.
     * @throws EmailAlreadyExistsException si el correo electrónico ya está registrado.
     * @throws InvalidPasswordException si la contraseña es nula o tiene menos de ocho caracteres.
     */
    public void validarUsuario(RegisterRequestDTO register) {
        if (userRepo.existsByUsername(register.getNombre())) {
            throw new UsernameAlreadyExistsException(register.getNombre());
        }
        if (userRepo.existsByEmail(register.getCorreo())) {
            throw new EmailAlreadyExistsException(register.getCorreo());
        }
        validarPassword(register.getPassword());
    }

    /**
     * Valida los datos de un usuario durante su actualización.
     * <p>
     * Comprueba que el nombre de usuario y el correo electrónico no estén
     * registrados por otro usuario. Permite conservar los valores actuales
     * del usuario que se está modificando.
     * </p>
     *
     * @param register objeto con los nuevos datos del usuario.
     * @param user entidad del usuario que se está actualizando.
     * @throws UsernameAlreadyExistsException si el nombre de usuario pertenece a otro usuario.
     * @throws EmailAlreadyExistsException si el correo electrónico pertenece a otro usuario.
     */
    public void validarUsuario(UpdateUserRequestDTO register, User user) {
        if (userRepo.existsByUsername(register.getNombre()) && !user.getUsername().equals(register.getNombre())) {
            throw new UsernameAlreadyExistsException(register.getNombre());
        }
        if (userRepo.existsByEmail(register.getCorreo()) && !user.getEmail().equals(register.getCorreo())) {
            throw new EmailAlreadyExistsException(register.getCorreo());
        }
    }

    /**
     * Verifica que una contraseña cumpla con la longitud mínima establecida.
     *
     * @param password contraseña que se desea validar.
     * @throws InvalidPasswordException si la contraseña es nula o tiene menos de ocho caracteres.
     */
    public void validarPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}