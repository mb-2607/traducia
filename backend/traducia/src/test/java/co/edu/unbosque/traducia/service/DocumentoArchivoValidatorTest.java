package co.edu.unbosque.traducia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import co.edu.unbosque.traducia.exception.DocumentoException;

/** Prueba límites y contenido real, sin depender de un servidor OCR. */
class DocumentoArchivoValidatorTest {

    private final DocumentoArchivoValidator validator = new DocumentoArchivoValidator("10MB");

    @Test
    void conservaTextoUnicodeYEliminaSoloBom() {
        assertEquals("  Español 🐱\n", validator.leerTextoUtf8(
                "\uFEFF  Español 🐱\n".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void rechazaTextoConCodificacionInvalida() {
        DocumentoException error = assertThrows(DocumentoException.class,
                () -> validator.leerTextoUtf8(new byte[] {(byte) 0xc3, 0x28}));
        assertEquals(422, error.getStatusCode());
    }

    @Test
    void rechazaArchivoVacio() {
        DocumentoException error = assertThrows(DocumentoException.class,
                () -> validator.validar(new MockMultipartFile("archivo", new byte[0])));
        assertEquals(400, error.getStatusCode());
    }

    @Test
    void rechazaFormatoFalsoAunqueElMimeDigaPdf() {
        MockMultipartFile archivo = archivo("falso.pdf", "texto que no es PDF");
        DocumentoException error = assertThrows(DocumentoException.class, () -> validator.validar(archivo));
        assertEquals(415, error.getStatusCode());
    }

    @Test
    void permiteExactamenteElLimiteYRechazaElByteSiguiente() {
        DocumentoArchivoValidator limitado = new DocumentoArchivoValidator("5B");
        assertEquals("documento.txt", limitado.validar(archivo("texto.txt", "12345")).nombre());
        DocumentoException error = assertThrows(DocumentoException.class,
                () -> limitado.validar(archivo("texto.txt", "123456")));
        assertEquals(413, error.getStatusCode());
    }

    @Test
    void aceptaDocAntiguoConFirmaDeContenedorOle() {
        byte[] firma = java.util.HexFormat.of().parseHex("d0cf11e0a1b11ae1");
        assertEquals("documento.doc", validator.validar(new MockMultipartFile(
                "archivo", "antiguo.doc", "application/msword", firma)).nombre());
    }

    @Test
    void verificaQueUnZipContengaUnDocumentoWord() throws IOException {
        byte[] word = crearZip("[Content_Types].xml", "word/document.xml");
        assertEquals("documento.docx", validator.validar(
                new MockMultipartFile("archivo", "word.docx", "application/zip", word)).nombre());

        byte[] otroZip = crearZip("[Content_Types].xml", "datos.txt");
        assertThrows(DocumentoException.class, () -> validator.validar(
                new MockMultipartFile("archivo", "falso.docx", "application/zip", otroZip)));
    }

    private static MockMultipartFile archivo(String nombre, String texto) {
        return new MockMultipartFile("archivo", nombre, "application/pdf", texto.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] crearZip(String... nombres) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(salida)) {
            for (String nombre : nombres) {
                zip.putNextEntry(new ZipEntry(nombre));
                zip.write("<document/>".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return salida.toByteArray();
    }
}
