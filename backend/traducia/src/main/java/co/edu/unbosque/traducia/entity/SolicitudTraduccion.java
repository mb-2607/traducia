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

/**
 * Entidad que representa una solicitud de traducción hecha por un usuario (tabla <code>solicitud_traduccion</code>).
 *
 * Guarda el texto original, los idiomas y la fecha, y agrupa las traducciones
 * obtenidas de cada proveedor. Las traducciones se guardan y eliminan en cascada.
 */
@Entity
@Table(name = "solicitud_traduccion")
public class SolicitudTraduccion {

	/**
	 * Identificador único del registro.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	/**
	 * Texto original que se desea traducir.
	 */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String textoOriginal;

	/**
	 * Idioma de origen de la solicitud.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "idioma_origen_id")
	private Idioma idiomaOrigen;

	/**
	 * Idioma de destino de la solicitud.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "idioma_destino_id")
	private Idioma idiomaDestino;

	/**
	 * Fecha y hora en que se creó la solicitud.
	 */
	@Column(nullable = false)
	private LocalDateTime fecha;

	/**
	 * Usuario que realizó la solicitud.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private User usuario;

	/**
	 * Lista de traducciones, una por cada proveedor consultado.
	 */
	@OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Traduccion> listaTraduccion = new ArrayList<>();

	/**
	 * Agrega una traducción a la solicitud y enlaza ambos lados de la relación.
	 *
	 * @param t traducción a agregar
	 */
	public void addTraduccion(Traduccion t) {
		listaTraduccion.add(t);
		t.setSolicitud(this);
	}

	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public SolicitudTraduccion() {

	}

	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param textoOriginal texto original que se desea traducir
	 * @param idiomaOrigen idioma de origen de la solicitud
	 * @param idiomaDestino idioma de destino de la solicitud
	 * @param fecha fecha y hora en que se creó la solicitud
	 * @param usuario usuario que realizó la solicitud
	 * @param listaTraduccion lista de traducciones, una por cada proveedor consultado
	 */
	public SolicitudTraduccion(String textoOriginal, Idioma idiomaOrigen, Idioma idiomaDestino, LocalDateTime fecha,
			User usuario, List<Traduccion> listaTraduccion) {
		this.textoOriginal = textoOriginal;
		this.idiomaOrigen = idiomaOrigen;
		this.idiomaDestino = idiomaDestino;
		this.fecha = fecha;
		this.usuario = usuario;
		this.listaTraduccion = listaTraduccion;
	}

	/**
	 * Obtiene el texto original que se desea traducir.
	 *
	 * @return texto original que se desea traducir
	 */
	public String getTextoOriginal() {
		return textoOriginal;
	}

	/**
	 * Establece el texto original que se desea traducir.
	 *
	 * @param textoOriginal texto original que se desea traducir a asignar
	 */
	public void setTextoOriginal(String textoOriginal) {
		this.textoOriginal = textoOriginal;
	}

	/**
	 * Obtiene el idioma de origen de la solicitud.
	 *
	 * @return idioma de origen de la solicitud
	 */
	public Idioma getIdiomaOrigen() {
		return idiomaOrigen;
	}

	/**
	 * Establece el idioma de origen de la solicitud.
	 *
	 * @param idiomaOrigen idioma de origen de la solicitud a asignar
	 */
	public void setIdiomaOrigen(Idioma idiomaOrigen) {
		this.idiomaOrigen = idiomaOrigen;
	}

	/**
	 * Obtiene el idioma de destino de la solicitud.
	 *
	 * @return idioma de destino de la solicitud
	 */
	public Idioma getIdiomaDestino() {
		return idiomaDestino;
	}

	/**
	 * Establece el idioma de destino de la solicitud.
	 *
	 * @param idiomaDestino idioma de destino de la solicitud a asignar
	 */
	public void setIdiomaDestino(Idioma idiomaDestino) {
		this.idiomaDestino = idiomaDestino;
	}

	/**
	 * Obtiene la fecha y hora en que se creó la solicitud.
	 *
	 * @return fecha y hora en que se creó la solicitud
	 */
	public LocalDateTime getFecha() {
		return fecha;
	}

	/**
	 * Establece la fecha y hora en que se creó la solicitud.
	 *
	 * @param fecha fecha y hora en que se creó la solicitud a asignar
	 */
	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	/**
	 * Obtiene el usuario que realizó la solicitud.
	 *
	 * @return usuario que realizó la solicitud
	 */
	public User getUsuario() {
		return usuario;
	}

	/**
	 * Establece el usuario que realizó la solicitud.
	 *
	 * @param usuario usuario que realizó la solicitud a asignar
	 */
	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	/**
	 * Obtiene la lista de traducciones, una por cada proveedor consultado.
	 *
	 * @return lista de traducciones, una por cada proveedor consultado
	 */
	public List<Traduccion> getListaTraduccion() {
		return listaTraduccion;
	}

	/**
	 * Establece la lista de traducciones, una por cada proveedor consultado.
	 *
	 * @param listaTraduccion lista de traducciones, una por cada proveedor consultado a asignar
	 */
	public void setListaTraduccion(List<Traduccion> listaTraduccion) {
		this.listaTraduccion = listaTraduccion; // skipcq: JAVA-E1086
	}

	/**
	 * Obtiene el identificador único del registro.
	 *
	 * @return identificador único del registro
	 */
	public Long getId() {
		return id;
	}

}
