package co.edu.unbosque.traducia.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad con la informacion de cada registro de auditoria(tabla <code>auditoria</code>).
 *
 * Guarda el usuario, metodo, ruta, status de la petición, ip, la duración del proceso y la fecha de cada solicitud.
 */
@Entity
@Table(name = "auditoria")
public class AuditoriaLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/** Identificador único del registro de auditoría. */
	private Long id; // skipcq: JAVA-E1065
	/** Nombre del usuario asociado a la solicitud. */
	private String usuario;
	/** Método HTTP utilizado en la solicitud. */
	private String metodo;
	/** Ruta o URI solicitada. */
	private String ruta;
	/** Código de estado HTTP devuelto en la respuesta. */
	private int status;
	/** Dirección IP de origen de la solicitud. */
	private String ip;
	/** Duración del procesamiento de la solicitud, expresada en milisegundos. */
	private long duracionMs;
	/** Fecha y hora en que se generó el registro de auditoría. */
	private LocalDateTime fecha;

	/**
	 * Construye una entidad de auditoría con los datos de una solicitud HTTP.
	 *
	 * @param usuario    usuario asociado a la solicitud
	 * @param metodo     método HTTP utilizado
	 * @param ruta       ruta solicitada
	 * @param status     código de estado HTTP de la respuesta
	 * @param ip         dirección IP de origen de la solicitud
	 * @param duracionMs duración del procesamiento en milisegundos
	 * @param fecha      fecha y hora del registro
	 */
	public AuditoriaLog(String usuario, String metodo, String ruta, int status, String ip, long duracionMs,
			LocalDateTime fecha) {
		this.usuario = usuario;
		this.metodo = metodo;
		this.ruta = ruta;
		this.status = status;
		this.ip = ip;
		this.duracionMs = duracionMs;
		this.fecha = fecha;
	}

	/**
	 * Construye una entidad de auditoría vacía. Se utiliza, entre otros casos, por
	 * el mecanismo de persistencia JPA.
	 */
	public AuditoriaLog() {
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getDuracionMs() {
		return duracionMs;
	}

	public void setDuracionMs(long duracionMs) {
		this.duracionMs = duracionMs;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

}
