package co.edu.unbosque.traducia.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.traducia.entity.ProveedorTraduccion;

/**
 * Pruebas unitarias para {@link TraductorStrategyRegistry} utilizando JUnit 5,
 * Mockito y AssertJ.
 *
 * <p>Valida el registro de implementaciones de {@link TraductorStrategy} en el
 * {@link java.util.EnumMap} interno por {@link ProveedorTraduccion}, la
 * recuperación correcta de cada estrategia, el reemplazo ante proveedores
 * duplicados y el lanzamiento de {@link IllegalArgumentException} cuando el
 * proveedor no está registrado o es nulo.</p>
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class TraductorStrategyRegistryTest {

	/**
	 * Crea un mock de {@link TraductorStrategy} asociado al proveedor indicado.
	 *
	 * @param proveedor proveedor que retornará {@link TraductorStrategy#getProveedor()}.
	 * @return mock configurado de {@link TraductorStrategy}.
	 */
	private TraductorStrategy crearEstrategiaMock(ProveedorTraduccion proveedor) {
		TraductorStrategy strategy = mock(TraductorStrategy.class);
		when(strategy.getProveedor()).thenReturn(proveedor);
		return strategy;
	}

	/**
	 * Verifica que {@code getStrategy} retorne exactamente la instancia de
	 * {@link TraductorStrategy} registrada para cada {@link ProveedorTraduccion}.
	 */
	@Test
	@DisplayName("getStrategy debe retornar la estrategia registrada para cada proveedor")
	void getStrategyDebeRetornarEstrategiaRegistradaParaCadaProveedor() {
		TraductorStrategy deepl = crearEstrategiaMock(ProveedorTraduccion.DEEPL);
		TraductorStrategy gemma = crearEstrategiaMock(ProveedorTraduccion.GEMMA);
		TraductorStrategy riva = crearEstrategiaMock(ProveedorTraduccion.RIVA);

		TraductorStrategyRegistry registry = new TraductorStrategyRegistry(List.of(deepl, gemma, riva));

		assertThat(registry.getStrategy(ProveedorTraduccion.DEEPL)).isSameAs(deepl);
		assertThat(registry.getStrategy(ProveedorTraduccion.GEMMA)).isSameAs(gemma);
		assertThat(registry.getStrategy(ProveedorTraduccion.RIVA)).isSameAs(riva);
	}

	/**
	 * Verifica que {@code getStrategy} lance {@link IllegalArgumentException}
	 * cuando se solicita un proveedor que no fue registrado en el constructor.
	 */
	@Test
	@DisplayName("getStrategy debe lanzar IllegalArgumentException si el proveedor no está registrado")
	void getStrategyDebeLanzarExcepcionCuandoProveedorNoEstaRegistrado() {
		TraductorStrategy deepl = crearEstrategiaMock(ProveedorTraduccion.DEEPL);
		TraductorStrategyRegistry registry = new TraductorStrategyRegistry(List.of(deepl));

		assertThatThrownBy(() -> registry.getStrategy(ProveedorTraduccion.RIVA))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Proveedor no soportado: RIVA");
	}

	/**
	 * Verifica que {@code getStrategy} lance {@link IllegalArgumentException}
	 * cuando el registro se inicializa con una lista vacía de estrategias.
	 */
	@Test
	@DisplayName("getStrategy debe lanzar IllegalArgumentException cuando no hay estrategias registradas")
	void getStrategyDebeLanzarExcepcionCuandoListaEstaVacia() {
		TraductorStrategyRegistry registry = new TraductorStrategyRegistry(List.of());

		assertThatThrownBy(() -> registry.getStrategy(ProveedorTraduccion.GEMMA))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Proveedor no soportado: GEMMA");
	}

	/**
	 * Verifica que {@code getStrategy} lance {@link IllegalArgumentException}
	 * cuando recibe {@code null} como proveedor.
	 */
	@Test
	@DisplayName("getStrategy debe lanzar IllegalArgumentException cuando el proveedor es nulo")
	void getStrategyDebeLanzarExcepcionCuandoProveedorEsNulo() {
		TraductorStrategy deepl = crearEstrategiaMock(ProveedorTraduccion.DEEPL);
		TraductorStrategyRegistry registry = new TraductorStrategyRegistry(List.of(deepl));

		assertThatThrownBy(() -> registry.getStrategy(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Proveedor no soportado: null");
	}

	/**
	 * Verifica que, si se registran dos estrategias para el mismo proveedor, el
	 * registro conserve la última instancia recibida.
	 */
	@Test
	@DisplayName("constructor debe conservar la última estrategia cuando hay un proveedor duplicado")
	void constructorDebeConservarUltimaEstrategiaAnteProveedorDuplicado() {
		TraductorStrategy primera = crearEstrategiaMock(ProveedorTraduccion.DEEPL);
		TraductorStrategy segunda = crearEstrategiaMock(ProveedorTraduccion.DEEPL);

		TraductorStrategyRegistry registry = new TraductorStrategyRegistry(List.of(primera, segunda));

		assertThat(registry.getStrategy(ProveedorTraduccion.DEEPL)).isSameAs(segunda);
	}
}