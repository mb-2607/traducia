package co.edu.unbosque.traducia.service;

import org.springframework.stereotype.Service;

import co.edu.unbosque.traducia.entity.AuditoriaLog;
import co.edu.unbosque.traducia.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

	private final AuditoriaRepository auditRepo;

	public AuditoriaService(AuditoriaRepository auditRepo) {
		this.auditRepo = auditRepo;
	}
	
	public void registrar(AuditoriaLog log) {
		try {
			auditRepo.save(log);
		} catch(Exception e) {
			
		}
	}
}
