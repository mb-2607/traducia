package co.edu.unbosque.traducia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.SolicitudTraduccion;

/**
 * Repositorio JPA de la entidad {@link SolicitudTraduccion}.
 *
 * Usa únicamente los métodos heredados de {@link JpaRepository}.
 */
public interface SolicitudTraduccionRepository extends JpaRepository<SolicitudTraduccion, Long>{

}