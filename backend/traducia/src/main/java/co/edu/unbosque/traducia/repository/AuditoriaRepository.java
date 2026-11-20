package co.edu.unbosque.traducia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.AuditoriaLog;

/**
 * Repositorio encargado de las operaciones de persistencia de los registros de
 * auditoría.
 * <p>
 * Hereda las operaciones CRUD y de consulta paginada proporcionadas por
 * {@link JpaRepository} para la entidad {@link AuditoriaLog}.
 * </p>
 *
 * @see JpaRepository
 * @see AuditoriaLog
 */
public interface AuditoriaRepository extends JpaRepository<AuditoriaLog, Long> {

}