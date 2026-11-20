package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.Role;

/**
 * DTO (Data Transfer Object) utilizado para representar la información de un
 * usuario que se envía como respuesta desde la aplicación.
 * <p>
 * Contiene los datos básicos del usuario, incluyendo su nombre, correo
 * electrónico, identificador del plan asociado y rol dentro del sistema.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class UserResponseDTO {

    /**
     * Nombre del usuario.
     */
    private String nombre;

    /**
     * Dirección de correo electrónico del usuario.
     */
    private String correo;

    /**
     * Identificador del plan asociado al usuario.
     */
    private PlanType planId;

    /**
     * Rol asignado al usuario dentro del sistema.
     */
    private Role rol;

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param nombre nombre que se asignará al usuario.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return correo electrónico del usuario.
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param correo dirección de correo electrónico que se asignará.
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene el identificador del plan asociado al usuario.
     *
     * @return identificador del plan.
     */
    public PlanType getPlanId() {
        return planId;
    }

    /**
     * Establece el identificador del plan asociado al usuario.
     *
     * @param planId identificador del plan que se asignará.
     */
    public void setPlanId(PlanType planId) {
        this.planId = planId;
    }

    /**
     * Obtiene el rol del usuario dentro del sistema.
     *
     * @return rol asignado al usuario.
     */
    public Role getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario dentro del sistema.
     *
     * @param rol rol que se asignará al usuario.
     */
    public void setRol(Role rol) {
        this.rol = rol;
    }

    /**
     * Construye un objeto UserResponseDTO con todos sus atributos inicializados.
     *
     * @param nombre nombre del usuario.
     * @param correo dirección de correo electrónico del usuario.
     * @param planId identificador del plan asociado al usuario.
     * @param rol rol asignado al usuario dentro del sistema.
     */
    public UserResponseDTO(String nombre, String correo, PlanType planId, Role rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.planId = planId;
        this.rol = rol;
    }

    /**
     * Construye un objeto UserResponseDTO sin inicializar sus atributos.
     */
    public UserResponseDTO() {
    }
}

