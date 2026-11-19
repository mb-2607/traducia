package co.edu.unbosque.traducia.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitud_traduccion")
public class SolicitudTraduccion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String textoOriginal;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "idioma_origen_id")
	private Idioma idiomaOrigen;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "idioma_destino_id")
	private Idioma idiomaDestino;

	@Column(nullable = false)
	private LocalDateTime fecha;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private User usuario;

	@OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Traduccion> listaTraduccion = new ArrayList<>();

	public void addTraduccion(Traduccion t) {
		listaTraduccion.add(t);
		t.setSolicitud(this);
	}

	public SolicitudTraduccion() {

	}

	public SolicitudTraduccion(String textoOriginal, Idioma idiomaOrigen, Idioma idiomaDestino, LocalDateTime fecha,
			User usuario, List<Traduccion> listaTraduccion) {
		this.textoOriginal = textoOriginal;
		this.idiomaOrigen = idiomaOrigen;
		this.idiomaDestino = idiomaDestino;
		this.fecha = fecha;
		this.usuario = usuario;
		this.listaTraduccion = listaTraduccion;
	}

	public String getTextoOriginal() {
		return textoOriginal;
	}

	public void setTextoOriginal(String textoOriginal) {
		this.textoOriginal = textoOriginal;
	}

	public Idioma getIdiomaOrigen() {
		return idiomaOrigen;
	}

	public void setIdiomaOrigen(Idioma idiomaOrigen) {
		this.idiomaOrigen = idiomaOrigen;
	}

	public Idioma getIdiomaDestino() {
		return idiomaDestino;
	}

	public void setIdiomaDestino(Idioma idiomaDestino) {
		this.idiomaDestino = idiomaDestino;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public List<Traduccion> getListaTraduccion() {
		return listaTraduccion;
	}

	public void setListaTraduccion(List<Traduccion> listaTraduccion) {
		this.listaTraduccion = listaTraduccion;
	}

	public Long getId() {
		return id;
	}

}
