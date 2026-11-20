package co.edu.unbosque.traducia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.Idioma;

/**
 * Repositorio JPA de la entidad {@link Idioma}.
 */
public interface IdiomaRepository extends JpaRepository<Idioma, Long>{
	/**
	 * Busca un idioma por su código.
	 *
	 * @param codigo código del idioma
	 * @return el idioma, o vacío si no existe
	 */
	Optional<Idioma> findByCodigo(String codigo);
	/**
	 * Indica si existe un idioma con el código dado.
	 *
	 * @param codigo código del idioma
	 * @return {@code true} si existe
	 */
	boolean existsByCodigo(String codigo);
}
