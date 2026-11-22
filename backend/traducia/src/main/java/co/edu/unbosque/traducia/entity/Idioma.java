package co.edu.unbosque.traducia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un idioma disponible para traducir (tabla <code>idioma</code>).
 *
 * El código es único y se usa para identificar el idioma en las peticiones.
 */
@Entity
@Table(name = "idioma")
public class Idioma {

	/**
	 * Identificador único del registro.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	/**
	 * Código único del idioma (por ejemplo, "es" o "en").
	 */
	@Column(nullable = false, unique = true, length = 10)
	private String codigo;

	/**
	 * Nombre del idioma.
	 */
	@Column(nullable = false)
	private String nombre;

	/**
	 * Constructor vacío, requerido por JPA y por la deserialización JSON.
	 */
	public Idioma() {
		
	}

	/**
	 * Crea una instancia con todos los atributos.
	 *
	 * @param codigo código único del idioma
	 * @param nombre nombre del idioma
	 */
	public Idioma(String codigo, String nombre) {
		this.codigo = codigo;
		this.nombre = nombre;
	}

	/**
	 * Obtiene el identificador único del registro.
	 *
	 * @return identificador único del registro
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Obtiene el código único del idioma
	 *
	 * @return código único del idioma
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * Establece el código único del idioma
	 *
	 * @param codigo código único del idioma a asignar
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * Obtiene el nombre del idioma.
	 *
	 * @return nombre del idioma
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Establece el nombre del idioma.
	 *
	 * @param nombre nombre del idioma a asignar
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}

