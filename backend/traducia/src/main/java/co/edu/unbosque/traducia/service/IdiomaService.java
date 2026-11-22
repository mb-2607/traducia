package co.edu.unbosque.traducia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.entity.Idioma;
import co.edu.unbosque.traducia.exception.IdiomaNotFoundException;
import co.edu.unbosque.traducia.repository.IdiomaRepository;

/**
 * Servicio para consultar los idiomas soportados por el sistema.
 */
@Service
public class IdiomaService {

	/**
	 * Repositorio de idiomas.
	 */
	private IdiomaRepository idiomaRepo;
	
	/**
	 * Conversor ModelMapper que transforma entidades en DTO.
	 */
	private ModelMapper modelMapper;

	/**
	 * Crea el componente con sus dependencias inyectadas por Spring.
	 *
	 * @param idiomaRepo repositorio de idiomas
	 * @param modelMapper conversor ModelMapper que transforma entidades en DTO
	 */
	public IdiomaService(IdiomaRepository idiomaRepo, ModelMapper modelMapper) {
		this.idiomaRepo = idiomaRepo;
		this.modelMapper = modelMapper;
	}

	/**
	 * Lista todos los idiomas registrados.
	 *
	 * @return lista de idiomas convertidos a {@link IdiomaResponseDTO}
	 */
	@Transactional(readOnly = true)
	public List<IdiomaResponseDTO> listarIdioma() {
		List<IdiomaResponseDTO> listaIdioma = new ArrayList<>();
		idiomaRepo.findAll().forEach((Idioma i) -> {listaIdioma.add(modelMapper.map(i, IdiomaResponseDTO.class));});
		return listaIdioma;
	}
	
	/**
	 * Busca un idioma por su código.
	 *
	 * @param codigo código del idioma, por ejemplo "es"
	 * @return el idioma encontrado
	 * @throws IdiomaNotFoundException si no existe un idioma con ese código
	 */
	@Transactional(readOnly = true)
	public Idioma obtenerPorCodigo(String codigo) {
		return idiomaRepo.findByCodigo(codigo)
				.orElseThrow(() -> new IdiomaNotFoundException(codigo));
	}
	
	/**
	 * Indica si existe un idioma con el código dado.
	 *
	 * @param codigo código del idioma
	 * @return {@code true} si el idioma existe, {@code false} en caso contrario
	 */
	@Transactional(readOnly = true)
	public boolean existePorCodigo(String codigo) {
		return idiomaRepo.existsByCodigo(codigo);
	}
	
}
