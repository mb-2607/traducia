package co.edu.unbosque.traducia.service;

import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.dto.AuditoriaLogDTO;
import co.edu.unbosque.traducia.entity.AuditoriaLog;
import co.edu.unbosque.traducia.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

	private final AuditoriaRepository auditRepo;

	private final ModelMapper modelMapper;

	public AuditoriaService(AuditoriaRepository auditRepo, ModelMapper modelMapper) {
		this.auditRepo = auditRepo;
		this.modelMapper = modelMapper;
	}

	public void registrar(AuditoriaLog log) {
		try {
			auditRepo.save(log);
		} catch (Exception e) {

		}
	}
	
	public Page<AuditoriaLogDTO> listar(Pageable pageable) {
		Page<AuditoriaLog> audiPage = auditRepo.findAll(pageable);
		return audiPage.map((AuditoriaLog al) -> {
			return modelMapper.map(al, AuditoriaLogDTO.class);
		});
	}
}
