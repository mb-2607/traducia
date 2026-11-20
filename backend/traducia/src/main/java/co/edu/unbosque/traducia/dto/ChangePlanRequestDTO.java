package co.edu.unbosque.traducia.dto;

import co.edu.unbosque.traducia.entity.PlanType;

/**
 * Objeto de transferencia de datos (DTO) utilizado para las solicitudes de cambio
 * de plan realizadas por un usuario autenticado sobre su propia cuenta.
 * <p>
 * Contiene el nuevo tipo de plan que el usuario desea asignar a su perfil.
 *
 * @author
 * @version 1.0
 * @see PlanType
 */

public class ChangePlanRequestDTO {
	
	/**
	 * Nuevo tipo de plan que el usuario desea establecer en su cuenta.
	 */
	
	
	private PlanType plan;
	
	/**
	 * Constructor por defecto requerido para la deserialización del objeto.
	 */
	

	public ChangePlanRequestDTO() {
	}
	
	/**
	 * Crea una nueva instancia del DTO con el tipo de plan especificado.
	 *
	 * @param plan nuevo tipo de plan que se asignará al usuario.
	 */
	

	public ChangePlanRequestDTO(PlanType plan) {
		this.plan = plan;
	}
	
	/**
	 * Obtiene el tipo de plan enviado en la solicitud.
	 *
	 * @return tipo de plan seleccionado por el usuario.
	 */
	

	public PlanType getPlan() {
		return plan;
	}
	
	/**
	 * Establece el tipo de plan que se desea asignar al usuario.
	 *
	 * @param plan nuevo tipo de plan del usuario.
	 */
	

	public void setPlan(PlanType plan) {
		this.plan = plan;
	}
}


