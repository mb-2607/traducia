package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;


/**
 * DTO (Data Transfer Object) utilizado para representar los datos enviados
 * por un administrador durante la creación de una nueva cuenta de usuario.
 * <p>
 * Extiende RegisterRequestDTO para reutilizar los atributos correspondientes
 * al nombre, correo electrónico y contraseña. Además, incorpora el rol y
 * el tipo de plan que se asignarán al usuario durante su creación.
 * </p>
 *
 * @author
 * @version 1.0
 * @see RegisterRequestDTO
 */
public class AdminCreateUserRequestDTO extends RegisterRequestDTO {

    /**
     * Rol que se asignará al usuario dentro del sistema.
     */
    private Role rol;

    /**
     * Tipo de plan que se asociará al usuario.
     */
    private PlanType planId;

    /**
     * Obtiene el rol asignado al usuario.
     *
     * @return rol que se asignará al usuario.
     */
    public Role getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario.
     *
     * @param rol rol que se asignará al usuario dentro del sistema.
     */
    public void setRol(Role rol) {
        this.rol = rol;
    }

    /**
     * Obtiene el tipo de plan asociado al usuario.
     *
     * @return tipo de plan del usuario.
     */
    public PlanType getPlanId() {
        return planId;
    }

    /**
     * Establece el tipo de plan que se asociará al usuario.
     *
     * @param planId tipo de plan que se asignará al usuario.
     */
    public void setPlanId(PlanType planId) {
        this.planId = planId;
    }

    /**
     * Construye un objeto AdminCreateUserRequestDTO con los datos personales,
     * credenciales, rol y tipo de plan del usuario que se desea crear.
     *
     * @param nombre nombre del usuario que se registrará.
     * @param correo dirección de correo electrónico del usuario.
     * @param password contraseña proporcionada para crear la cuenta.
     * @param rol rol que se asignará al usuario dentro del sistema.
     * @param planId tipo de plan que se asociará al usuario.
     */
    public AdminCreateUserRequestDTO(String nombre, String correo, String password, Role rol, PlanType planId) {
        super(nombre, correo, password);
        this.rol = rol;
        this.planId = planId;
    }

    /**
     * Construye un objeto AdminCreateUserRequestDTO sin inicializar sus
     * atributos.
     */
    public AdminCreateUserRequestDTO() {
    }
}

