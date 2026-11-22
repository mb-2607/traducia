package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.edu.unbosque.traducia.dto.AuditoriaLogDTO;
import co.edu.unbosque.traducia.entity.AuditoriaLog;
import co.edu.unbosque.traducia.repository.AuditoriaRepository;

/**
 * Pruebas unitarias para {@link AuditoriaService} utilizando JUnit 5, Mockito
 * y AssertJ.
 *
 * <p>Valida el registro de eventos de auditoría (incluyendo la tolerancia a
 * fallos de persistencia) y la consulta paginada con mapeo de entidades
 * {@link AuditoriaLog} a {@link AuditoriaLogDTO}.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

	@Mock
	private AuditoriaRepository auditRepo;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private AuditoriaService auditoriaService;

	/**
	 * Verifica que {@code registrar} persista el registro de auditoría mediante el
	 * repositorio.
	 */
	@Test
	@DisplayName("registrar debe guardar el registro de auditoría en el repositorio")
	void registrarDebeGuardarRegistroEnRepositorio() {
		AuditoriaLog log = mock(AuditoriaLog.class);

		auditoriaService.registrar(log);

		verify(auditRepo).save(log);
	}

	/**
	 * Verifica que {@code registrar} capture las excepciones de persistencia sin
	 * propagarlas para no interrumpir el flujo principal.
	 */
	@Test
	@DisplayName("registrar no debe propagar excepciones si falla el repositorio")
	void registrarNoDebePropagarExcepcionCuandoFallaPersistencia() {
		AuditoriaLog log = mock(AuditoriaLog.class);
		when(auditRepo.save(log)).thenThrow(new RuntimeException("Error de base de datos"));

		assertThatCode(() -> auditoriaService.registrar(log))
				.doesNotThrowAnyException();

		verify(auditRepo).save(log);
	}

	/**
	 * Verifica que {@code listar} consulte la página en el repositorio y convierta
	 * cada entidad {@link AuditoriaLog} en {@link AuditoriaLogDTO}.
	 */
	@Test
	@DisplayName("listar debe retornar la página de registros convertidos a DTO")
	void listarDebeRetornarPaginaDeDtosMapeados() {
		Pageable pageable = PageRequest.of(0, 10);
		AuditoriaLog log1 = mock(AuditoriaLog.class);
		AuditoriaLog log2 = mock(AuditoriaLog.class);
		AuditoriaLogDTO dto1 = mock(AuditoriaLogDTO.class);
		AuditoriaLogDTO dto2 = mock(AuditoriaLogDTO.class);

		Page<AuditoriaLog> paginaEntidades = new PageImpl<>(List.of(log1, log2), pageable, 2);

		when(auditRepo.findAll(pageable)).thenReturn(paginaEntidades);
		when(modelMapper.map(log1, AuditoriaLogDTO.class)).thenReturn(dto1);
		when(modelMapper.map(log2, AuditoriaLogDTO.class)).thenReturn(dto2);

		Page<AuditoriaLogDTO> resultado = auditoriaService.listar(pageable);

		assertThat(resultado.getContent()).containsExactly(dto1, dto2);
		assertThat(resultado.getTotalElements()).isEqualTo(2);
		verify(auditRepo).findAll(pageable);
		verify(modelMapper).map(log1, AuditoriaLogDTO.class);
		verify(modelMapper).map(log2, AuditoriaLogDTO.class);
	}

	/**
	 * Verifica que {@code listar} retorne una página vacía y no invoque a
	 * {@link ModelMapper} cuando no hay registros almacenados.
	 */
	@Test
	@DisplayName("listar debe retornar una página vacía cuando no existen registros")
	void listarDebeRetornarPaginaVaciaCuandoNoHayRegistros() {
		Pageable pageable = PageRequest.of(0, 10);
		when(auditRepo.findAll(pageable)).thenReturn(Page.empty(pageable));

		Page<AuditoriaLogDTO> resultado = auditoriaService.listar(pageable);

		assertThat(resultado.getContent()).isEmpty();
		assertThat(resultado.getTotalElements()).isZero();
		verify(auditRepo).findAll(pageable);
		verifyNoInteractions(modelMapper);
	}
}