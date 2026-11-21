package co.edu.unbosque.traducia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import co.edu.unbosque.traducia.dto.ArchivoMultipartDTO;
import co.edu.unbosque.traducia.exception.DocumentoException;
import co.edu.unbosque.traducia.exception.ExternalAPIException;

/** Verifica el flujo asíncrono remoto con un servidor HTTP local de prueba. */
class LlamaParseServiceTest {

    private final AtomicReference<String> creacion = new AtomicReference<>("{\"id\":\"trabajo-1\",\"status\":\"PENDING\"}");
    private final AtomicReference<String> resultado = new AtomicReference<>(completado("Hola.", true));
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
    private final AtomicReference<String> autorizacion = new AtomicReference<>();
    private final AtomicInteger codigo = new AtomicInteger(200);
    private final AtomicInteger subidas = new AtomicInteger();
    private final AtomicInteger consultas = new AtomicInteger();
    private final AtomicInteger pendientes = new AtomicInteger();
    private HttpServer server;
    private String url;

    @BeforeEach
    void iniciarServidor() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v2/parse", exchange -> {
            try (exchange) {
                autorizacion.set(exchange.getRequestHeaders().getFirst("Authorization"));
                String respuesta;
                if ("POST".equals(exchange.getRequestMethod())) {
                    subidas.incrementAndGet();
                    cuerpoRecibido.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    respuesta = creacion.get();
                } else {
                    int consulta = consultas.incrementAndGet();
                    respuesta = consulta <= pendientes.get()
                            ? "{\"job\":{\"id\":\"trabajo-1\",\"status\":\"RUNNING\"}}" : resultado.get();
                }
                byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(codigo.get(), bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void detenerServidor() {
        server.stop(0);
    }

    @Test
    void enviaArchivoConfiguracionYBearerYConsultaElTrabajo() {
        pendientes.set(1);
        assertEquals("Hola.", servicio().extraer(archivo(), "es"));
        assertEquals("Bearer clave-solo-de-prueba", autorizacion.get());
        assertTrue(cuerpoRecibido.get().contains("name=\"file\"; filename=\"documento.pdf\""));
        assertTrue(cuerpoRecibido.get().contains("name=\"configuration\""));
        assertTrue(cuerpoRecibido.get().contains("\"languages\":[\"es\"]"));
        assertEquals(1, subidas.get());
        assertEquals(2, consultas.get());
    }

    @Test
    void rechazaPaginaFallidaAunqueElTrabajoEsteCompletado() {
        resultado.set(completado("Parcial", false));
        assertEquals(422, assertThrows(DocumentoException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
    }

    @Test
    void rechazaTrabajoFallidoSinDevolverUnTextoParcial() {
        resultado.set("{\"job\":{\"id\":\"trabajo-1\",\"status\":\"FAILED\"}}");
        assertEquals(422, assertThrows(DocumentoException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
    }

    @Test
    void conservaCodigoExternoParaQueElControllerLoClasifique() {
        codigo.set(401);
        assertEquals(401, assertThrows(ExternalAPIException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
        assertEquals(1, subidas.get());
    }

    @Test
    void rechazaIdentificadorQuePuedaAlterarLaRuta() {
        creacion.set("{\"id\":\"../otro\",\"status\":\"PENDING\"}");
        assertEquals(502, assertThrows(DocumentoException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
        assertEquals(0, consultas.get());
    }

    @Test
    void rechazaResultadoSinLasRepresentacionesSolicitadas() {
        resultado.set("{\"job\":{\"id\":\"trabajo-1\",\"status\":\"COMPLETED\"}}");
        assertEquals(502, assertThrows(DocumentoException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
    }

    @Test
    void rechazaJsonInvalidoYJsonNulo() {
        creacion.set("no JSON");
        assertEquals(502, assertThrows(ExternalAPIException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
        creacion.set("null");
        assertEquals(502, assertThrows(ExternalAPIException.class,
                () -> servicio().extraer(archivo(), "es")).getStatusCode());
    }

    @Test
    void rechazaClaveAusenteAntesDeSubirElArchivo() {
        LlamaParseService servicio = new LlamaParseService(url, "", "cost_effective", 10, 10);
        assertEquals(503, assertThrows(DocumentoException.class,
                () -> servicio.extraer(archivo(), "es")).getStatusCode());
        assertEquals(0, subidas.get());
    }

    @Test
    void aplicaElPlazoTotalSinCrearSubidasRepetidas() {
        pendientes.set(Integer.MAX_VALUE);
        LlamaParseService servicio = new LlamaParseService(url, "clave-solo-de-prueba", "cost_effective", 1, 50);
        assertEquals(504, assertThrows(DocumentoException.class,
                () -> servicio.extraer(archivo(), "es")).getStatusCode());
        assertEquals(1, subidas.get());
    }

    @Test
    void rechazaIdiomasEscritosComoNombreCompleto() {
        assertEquals(400, assertThrows(DocumentoException.class,
                () -> servicio().extraer(archivo(), "english")).getStatusCode());
        assertEquals(0, subidas.get());
    }

    private LlamaParseService servicio() {
        return new LlamaParseService(url, "clave-solo-de-prueba", "cost_effective", 10, 10);
    }

    private static ArchivoMultipartDTO archivo() {
        return new ArchivoMultipartDTO("file", "documento.pdf", "%PDF-1.7".getBytes(StandardCharsets.UTF_8));
    }

    private static String completado(String texto, boolean exito) {
        return "{\"job\":{\"id\":\"trabajo-1\",\"status\":\"COMPLETED\"},"
                + "\"text\":{\"pages\":[{\"page_number\":1,\"text\":\"" + texto + "\"}]},"
                + "\"markdown\":{\"pages\":[{\"page_number\":1,\"success\":" + exito + "}]}}";
    }
}
