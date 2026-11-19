package co.edu.unbosque.traducia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.traducia.entity.Idioma;

public interface IdiomaRepository extends JpaRepository<Idioma, Long>{
	Optional<Idioma> findByCodigo(String codigo);
	boolean existsByCodigo(String codigo);
}
