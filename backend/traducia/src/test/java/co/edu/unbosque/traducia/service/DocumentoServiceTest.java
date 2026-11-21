package co.edu.unbosque.traducia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import co.edu.unbosque.traducia.dto.DocumentoExtraidoResponseDTO;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.exception.DocumentoException;

/** Prueba límites de plan, preservación de texto y ruta local de TXT. */
class DocumentoServiceTest {

    @Test
    void informaElExcesoSinRecortarNiInvocarLlamaParseParaTxt() {
        LlamaParseService proveedor = mock(LlamaParseService.class);
        UserService usuarios = mock(UserService.class);
        when(usuarios.obtenerEntidadPorNombre("usuario")).thenReturn(new User());
        DocumentoService servicio = new DocumentoService(new DocumentoArchivoValidator("10MB"), proveedor, usuarios);
        String texto = "a".repeat(501);
        DocumentoExtraidoResponseDTO respuesta = servicio.extraer(archivo(texto), "es", "usuario");
        assertEquals(texto, respuesta.texto());
        assertEquals(501, respuesta.caracteres());
        assertEquals(500, respuesta.limiteCaracteres());
        assertTrue(respuesta.excedeLimite());
        verifyNoInteractions(proveedor);
    }

    @Test
    void cuentaUtf16ComoElServicioDeTraduccionExistente() {
        UserService usuarios = mock(UserService.class);
        when(usuarios.obtenerEntidadPorNombre("usuario")).thenReturn(new User());
        DocumentoService servicio = new DocumentoService(
                new DocumentoArchivoValidator("10MB"), mock(LlamaParseService.class), usuarios);
        assertEquals(2, servicio.extraer(archivo("🐱"), "es", "usuario").caracteres());
    }

    @Test
    void rechazaArchivoConSoloEspacios() {
        UserService usuarios = mock(UserService.class);
        when(usuarios.obtenerEntidadPorNombre("usuario")).thenReturn(new User());
        DocumentoService servicio = new DocumentoService(
                new DocumentoArchivoValidator("10MB"), mock(LlamaParseService.class), usuarios);
        assertEquals(422, assertThrows(DocumentoException.class,
                () -> servicio.extraer(archivo("  \n"), "es", "usuario")).getStatusCode());
    }

    private static MockMultipartFile archivo(String texto) {
        return new MockMultipartFile("archivo", "ejemplo.txt", "text/plain", texto.getBytes(StandardCharsets.UTF_8));
    }
}
