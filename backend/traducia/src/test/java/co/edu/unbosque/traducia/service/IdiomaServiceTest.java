package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.entity.Idioma;
import co.edu.unbosque.traducia.exception.IdiomaNotFoundException;
import co.edu.unbosque.traducia.repository.IdiomaRepository;

/**
 * Pruebas unitarias para {@link IdiomaService} utilizando JUnit 5, Mockito y
 * AssertJ.
 *
 * <p>Valida el listado de idiomas mapeados a {@link IdiomaResponseDTO}, la
 * consulta de un idioma por su código y la verificación de existencia por
 * código aislando {@link IdiomaRepository} y {@link ModelMapper}.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class IdiomaServiceTest {

	@Mock
	private IdiomaRepository idiomaRepo;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private IdiomaService idiomaService;

	/**
	 * Verifica que {@code listarIdioma} obtenga todos los registros del
	 * repositorio y los convierta a {@link IdiomaResponseDTO}.
	 */
	@Test
	@DisplayName("listarIdioma debe retornar la lista de idiomas convertidos a DTO")
	void listarIdiomaDebeRetornarListaDeDtosMapeados() {
		Idioma idioma1 = mock(Idioma.class);
		Idioma idioma2 = mock(Idioma.class);
		IdiomaResponseDTO dto1 = mock(IdiomaResponseDTO.class);
		IdiomaResponseDTO dto2 = mock(IdiomaResponseDTO.class);

		when(idiomaRepo.findAll()).thenReturn(List.of(idioma1, idioma2));
		when(modelMapper.map(idioma1, IdiomaResponseDTO.class)).thenReturn(dto1);
		when(modelMapper.map(idioma2, IdiomaResponseDTO.class)).thenReturn(dto2);

		List<IdiomaResponseDTO> resultado = idiomaService.listarIdioma();

		assertThat(resultado).containsExactly(dto1, dto2);
		verify(idiomaRepo).findAll();
		verify(modelMapper).map(idioma1, IdiomaResponseDTO.class);
		verify(modelMapper).map(idioma2, IdiomaResponseDTO.class);
	}

	/**
	 * Verifica que {@code listarIdioma} retorne una lista vacía y no use
	 * {@link ModelMapper} cuando no hay idiomas registrados.
	 */
	@Test
	@DisplayName("listarIdioma debe retornar lista vacía cuando no hay idiomas")
	void listarIdiomaDebeRetornarListaVaciaCuandoNoHayRegistros() {
		when(idiomaRepo.findAll()).thenReturn(List.of());

		List<IdiomaResponseDTO> resultado = idiomaService.listarIdioma();

		assertThat(resultado).isEmpty();
		verify(idiomaRepo).findAll();
		verifyNoInteractions(modelMapper);
	}

	/**
	 * Verifica que {@code obtenerPorCodigo} retorne el idioma correspondiente
	 * cuando existe en el repositorio.
	 */
	@Test
	@DisplayName("obtenerPorCodigo debe retornar el idioma cuando existe")
	void obtenerPorCodigoDebeRetornarIdiomaCuandoExiste() {
		Idioma idioma = mock(Idioma.class);
		when(idiomaRepo.findByCodigo("es")).thenReturn(Optional.of(idioma));

		Idioma resultado = idiomaService.obtenerPorCodigo("es");

		assertThat(resultado).isSameAs(idioma);
		verify(idiomaRepo).findByCodigo("es");
	}

	/**
	 * Verifica que {@code obtenerPorCodigo} lance {@link IdiomaNotFoundException}
	 * cuando no existe un idioma con el código indicado.
	 */
	@Test
	@DisplayName("obtenerPorCodigo debe lanzar IdiomaNotFoundException cuando no existe")
	void obtenerPorCodigoDebeLanzarExcepcionCuandoNoExiste() {
		when(idiomaRepo.findByCodigo("xx")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> idiomaService.obtenerPorCodigo("xx"))
				.isInstanceOf(IdiomaNotFoundException.class);

		verify(idiomaRepo).findByCodigo("xx");
	}

	/**
	 * Verifica que {@code existePorCodigo} retorne {@code true} cuando el código
	 * existe en el repositorio.
	 */
	@Test
	@DisplayName("existePorCodigo debe retornar true cuando el idioma existe")
	void existePorCodigoDebeRetornarTrueCuandoExiste() {
		when(idiomaRepo.existsByCodigo("en")).thenReturn(true);

		boolean resultado = idiomaService.existePorCodigo("en");

		assertThat(resultado).isTrue();
		verify(idiomaRepo).existsByCodigo("en");
	}

	/**
	 * Verifica que {@code existePorCodigo} retorne {@code false} cuando el código
	 * no existe en el repositorio.
	 */
	@Test
	@DisplayName("existePorCodigo debe retornar false cuando no existe")
	void existePorCodigoDebeRetornarFalseCuandoNoExiste() {
		when(idiomaRepo.existsByCodigo("fr")).thenReturn(false);

		boolean resultado = idiomaService.existePorCodigo("fr");

		assertThat(resultado).isFalse();
		verify(idiomaRepo).existsByCodigo("fr");
	}
}
