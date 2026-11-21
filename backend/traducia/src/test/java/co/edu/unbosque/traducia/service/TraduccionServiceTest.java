package co.edu.unbosque.traducia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.traducia.dto.SolicitudTraduccionResponseDTO;
import co.edu.unbosque.traducia.dto.TraduccionRequestDTO;
import co.edu.unbosque.traducia.entity.EstadoTraduccion;
import co.edu.unbosque.traducia.entity.Idioma;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.SolicitudTraduccion;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.IdiomaEqualsException;
import co.edu.unbosque.traducia.exception.PlanCharacterLimitExceededException;
import co.edu.unbosque.traducia.exception.TraduccionFailedException;
import co.edu.unbosque.traducia.repository.SolicitudTraduccionRepository;
import co.edu.unbosque.traducia.service.strategy.TraductorStrategy;
import co.edu.unbosque.traducia.service.strategy.TraductorStrategyRegistry;

/**
 * Pruebas unitarias para {@link TraduccionService} utilizando JUnit 5, Mockito
 * y AssertJ.
 *
 * <p>Valida la validación de límites de caracteres por {@link PlanType}, la
 * verificación de idiomas distintos, la ejecución de estrategias por proveedor
 * con estados {@link EstadoTraduccion#EXITOSA} o {@link EstadoTraduccion#FALLIDA}
 * y la persistencia de {@link SolicitudTraduccion}.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class TraduccionServiceTest {

	@Mock
	private UserService userService;

	@Mock
	private TraductorStrategyRegistry tsr;

	@Mock
	private SolicitudTraduccionRepository soliRepo;

	@Mock
	private IdiomaService idiomaService;

	@InjectMocks
	private TraduccionService traduccionService;

	/**
	 * Crea un {@link Idioma} con el código indicado.
	 *
	 * @param codigo código ISO del idioma.
	 * @return entidad {@link Idioma} configurada.
	 */
	private Idioma crearIdioma(String codigo) {
		Idioma idioma = new Idioma();
		idioma.setCodigo(codigo);
		return idioma;
	}

	/**
	 * Verifica que {@code traducir} valide el plan, consulte la estrategia del
	 * proveedor del plan {@link PlanType#FREE}, guarde la solicitud y retorne el
	 * DTO de respuesta con la traducción exitosa.
	 */
	@Test
	@DisplayName("traducir debe ejecutar estrategia del plan FREE y guardar la solicitud")
	void traducirDebeEjecutarEstrategiaYGuardarSolicitud() {
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);

		TraduccionRequestDTO request = new TraduccionRequestDTO();
		request.setOrigen("es");
		request.setDestino("en");
		request.setTexto("Hola mundo");

		Idioma origen = crearIdioma("es");
		Idioma destino = crearIdioma("en");

		TraductorStrategy strategyMock = mock(TraductorStrategy.class);
		when(strategyMock.traducir(request)).thenReturn("Hello world");

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuario);
		when(idiomaService.obtenerPorCodigo("es")).thenReturn(origen);
		when(idiomaService.obtenerPorCodigo("en")).thenReturn(destino);
		when(tsr.getStrategy(ProveedorTraduccion.DEEPL)).thenReturn(strategyMock);
		when(soliRepo.save(any(SolicitudTraduccion.class))).thenAnswer(invocation -> {
			SolicitudTraduccion guardada = invocation.getArgument(0);
			ReflectionTestUtils.setField(guardada, "id", 10L);
			return guardada;
		});

		SolicitudTraduccionResponseDTO respuesta = traduccionService.traducir(request, "dsantiago");

		assertThat(respuesta.getId()).isEqualTo(10L);
		assertThat(respuesta.getOrigen()).isEqualTo("es");
		assertThat(respuesta.getDestino()).isEqualTo("en");
		assertThat(respuesta.getTextoOriginal()).isEqualTo("Hola mundo");
		assertThat(respuesta.getListaTraduccion()).hasSize(1);
		assertThat(respuesta.getListaTraduccion().get(0).getTextoTraducido()).isEqualTo("Hello world");
		assertThat(respuesta.getListaTraduccion().get(0).getEstado()).isEqualTo(EstadoTraduccion.EXITOSA);
		assertThat(respuesta.getListaTraduccion().get(0).getProveedorTraduccion()).isEqualTo(ProveedorTraduccion.DEEPL);

		verify(soliRepo, times(1)).save(any(SolicitudTraduccion.class));
	}

	/**
	 * Verifica que {@code traducir} lance {@link PlanCharacterLimitExceededException}
	 * cuando la longitud del texto excede el máximo permitido por el plan.
	 */
	@Test
	@DisplayName("traducir debe lanzar PlanCharacterLimitExceededException si supera el límite del plan")
	void traducirDebeLanzarExcepcionCuandoSuperaLimiteDelPlan() {
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);

		String textoLargo = "a".repeat((int) PlanType.FREE.getNumeroCaracterPorSolicitud() + 1);
		TraduccionRequestDTO request = new TraduccionRequestDTO();
		request.setOrigen("es");
		request.setDestino("en");
		request.setTexto(textoLargo);

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuario);

		assertThatThrownBy(() -> traduccionService.traducir(request, "dsantiago"))
				.isInstanceOf(PlanCharacterLimitExceededException.class);

		verifyNoInteractions(idiomaService, tsr, soliRepo);
	}

	/**
	 * Verifica que {@code traducir} lance {@link IdiomaEqualsException} cuando el
	 * idioma de origen y el de destino tienen el mismo código.
	 */
	@Test
	@DisplayName("traducir debe lanzar IdiomaEqualsException cuando origen y destino son iguales")
	void traducirDebeLanzarExcepcionCuandoIdiomasSonIguales() {
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);

		TraduccionRequestDTO request = new TraduccionRequestDTO();
		request.setOrigen("es");
		request.setDestino("es");
		request.setTexto("Hola");

		Idioma origen = crearIdioma("es");
		Idioma destino = crearIdioma("es");

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuario);
		when(idiomaService.obtenerPorCodigo("es")).thenReturn(origen).thenReturn(destino);

		assertThatThrownBy(() -> traduccionService.traducir(request, "dsantiago"))
				.isInstanceOf(IdiomaEqualsException.class);

		verifyNoInteractions(tsr, soliRepo);
	}

	/**
	 * Verifica que si una estrategia lanza una excepción, la traducción quede
	 * registrada con estado {@link EstadoTraduccion#FALLIDA} y sin texto traducido,
	 * pero la solicitud igual se persista si se generó al menos una traducción.
	 */
	@Test
	@DisplayName("traducir debe marcar FALLIDA cuando la estrategia lanza excepción")
	void traducirDebeMarcarTraduccionFallidaAnteExcepcionDeEstrategia() {
		User usuario = new User();
		usuario.setPlanType(PlanType.FREE);

		TraduccionRequestDTO request = new TraduccionRequestDTO();
		request.setOrigen("es");
		request.setDestino("en");
		request.setTexto("Hola mundo");

		Idioma origen = crearIdioma("es");
		Idioma destino = crearIdioma("en");

		TraductorStrategy strategyMock = mock(TraductorStrategy.class);
		when(strategyMock.traducir(request)).thenThrow(new RuntimeException("Error de proveedor"));

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuario);
		when(idiomaService.obtenerPorCodigo("es")).thenReturn(origen);
		when(idiomaService.obtenerPorCodigo("en")).thenReturn(destino);
		when(tsr.getStrategy(ProveedorTraduccion.DEEPL)).thenReturn(strategyMock);
		when(soliRepo.save(any(SolicitudTraduccion.class))).thenAnswer(invocation -> {
			SolicitudTraduccion guardada = invocation.getArgument(0);
			ReflectionTestUtils.setField(guardada, "id", 11L);
			return guardada;
		});

		SolicitudTraduccionResponseDTO respuesta = traduccionService.traducir(request, "dsantiago");

		assertThat(respuesta.getId()).isEqualTo(11L);
		assertThat(respuesta.getListaTraduccion()).hasSize(1);
		assertThat(respuesta.getListaTraduccion().get(0).getEstado()).isEqualTo(EstadoTraduccion.FALLIDA);
		assertThat(respuesta.getListaTraduccion().get(0).getTextoTraducido()).isNull();
		verify(soliRepo, times(1)).save(any(SolicitudTraduccion.class));
	}

	/**
	 * Verifica que {@code traducir} lance {@link TraduccionFailedException} si la
	 * solicitud queda sin traducciones (por ejemplo, un plan sin proveedores).
	 */
	@Test
	@DisplayName("traducir debe lanzar TraduccionFailedException si la lista de traducciones queda vacía")
	void traducirDebeLanzarExcepcionCuandoListaTraduccionQuedaVacia() {
		User usuarioMock = mock(User.class);
		PlanType planMock = mock(PlanType.class);

		when(usuarioMock.getPlanType()).thenReturn(planMock);
		when(planMock.getNumeroCaracterPorSolicitud()).thenReturn(500L);
		when(planMock.getListaProveedor()).thenReturn(java.util.Set.of());

		TraduccionRequestDTO request = new TraduccionRequestDTO();
		request.setOrigen("es");
		request.setDestino("en");
		request.setTexto("Hola");

		Idioma origen = crearIdioma("es");
		Idioma destino = crearIdioma("en");

		when(userService.obtenerEntidadPorNombre("dsantiago")).thenReturn(usuarioMock);
		when(idiomaService.obtenerPorCodigo("es")).thenReturn(origen);
		when(idiomaService.obtenerPorCodigo("en")).thenReturn(destino);

		assertThatThrownBy(() -> traduccionService.traducir(request, "dsantiago"))
				.isInstanceOf(TraduccionFailedException.class);

		verifyNoInteractions(soliRepo);
	}
}
