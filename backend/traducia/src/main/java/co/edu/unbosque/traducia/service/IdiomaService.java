package co.edu.unbosque.traducia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.entity.Idioma;
import co.edu.unbosque.traducia.exception.IdiomaNotFoundException;
import co.edu.unbosque.traducia.repository.IdiomaRepository;

@Service
public class IdiomaService {

	private IdiomaRepository idiomaRepo;
	
	private ModelMapper modelMapper;

	public IdiomaService(IdiomaRepository idiomaRepo, ModelMapper modelMapper) {
		this.idiomaRepo = idiomaRepo;
		this.modelMapper = modelMapper;
	}

	public List<IdiomaResponseDTO> listarIdioma() {
		List<IdiomaResponseDTO> listaIdioma = new ArrayList<>();
		idiomaRepo.findAll().forEach((Idioma i) -> {listaIdioma.add(modelMapper.map(i, IdiomaResponseDTO.class));});
		return listaIdioma;
	}
	
	public Idioma obtenerPorCodigo(String codigo) {
		return idiomaRepo.findByCodigo(codigo)
				.orElseThrow(() -> new IdiomaNotFoundException(codigo));
	}
	
	public boolean existePorCodigo(String codigo) {
		return idiomaRepo.existsByCodigo(codigo);
	}
	
}
