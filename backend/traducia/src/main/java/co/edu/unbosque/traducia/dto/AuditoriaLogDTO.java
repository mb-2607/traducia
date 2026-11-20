package co.edu.unbosque.traducia.dto;

import java.time.LocalDateTime;

public class AuditoriaLogDTO {

	private Long id;
	private String usuario;
	private String metodo;
	private String ruta;
	private int status;
	private String ip;
	private long duracionMs;
	private LocalDateTime fecha;

	public AuditoriaLogDTO() {

	}

	public AuditoriaLogDTO(Long id, String usuario, String metodo, String ruta, int status, String ip, long duracionMs,
			LocalDateTime fecha) {
		this.id = id;
		this.usuario = usuario;
		this.metodo = metodo;
		this.ruta = ruta;
		this.status = status;
		this.ip = ip;
		this.duracionMs = duracionMs;
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

}
