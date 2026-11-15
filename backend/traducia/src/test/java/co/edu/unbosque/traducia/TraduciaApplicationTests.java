package co.edu.unbosque.traducia;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Pruebas de integración de arranque de la aplicación Traducia.
 *
 * <p>Verifica que el contexto de Spring se carga correctamente con la
 * configuración actual (beans, propiedades y conexiones).</p>
 */
@SpringBootTest
class TraduciaApplicationTests {

	@Autowired
    private ApplicationContext context;
	
	/**
     * Comprueba que el contexto de Spring se inicializa sin errores y
     * queda disponible. Si falla, hay un problema de configuración
     * (bean que no se puede crear, propiedad faltante o base de datos
     * inaccesible).
     */
    @Test
    void contextLoads() {
        assertNotNull(context);
    }

}
