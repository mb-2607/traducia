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

/**
 * Prueba límites y contenido real, sin depender de un servidor OCR.
 *
 * <p>
 * Comprueba la lectura de texto UTF-8, el tratamiento del marcador BOM, los
 * límites de tamaño y la identificación básica de archivos PDF, DOC y DOCX.
 * Utiliza archivos en memoria y no realiza peticiones externas.
 * </p>
 */
class DocumentoArchivoValidatorTest {

	/** Validador configurado con un tamaño máximo de archivo de 10 MB. */
	private final DocumentoArchivoValidator validator = new DocumentoArchivoValidator("10MB");

	/**
	 * Comprueba que la lectura UTF-8 elimina únicamente el marcador BOM inicial y
	 * conserva los caracteres Unicode, espacios y saltos de línea.
	 */
	@Test
	void conservaTextoUnicodeYEliminaSoloBom() {
		assertEquals("  Español 🐱\n",
				validator.leerTextoUtf8("\uFEFF  Español 🐱\n".getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Comprueba que una secuencia de bytes que no representa UTF-8 válido provoca
	 * una {@link DocumentoException} con código HTTP 422.
	 */
	@Test
	void rechazaTextoConCodificacionInvalida() {
		DocumentoException error = assertThrows(DocumentoException.class,
				() -> validator.leerTextoUtf8(new byte[] { (byte) 0xc3, 0x28 }));

		assertEquals(422, error.getStatusCode());
	}

	/**
	 * Comprueba que un archivo sin contenido se rechaza mediante una
	 * {@link DocumentoException} con código HTTP 400.
	 */
	@Test
	void rechazaArchivoVacio() {
		DocumentoException error = assertThrows(DocumentoException.class,
				() -> validator.validar(new MockMultipartFile("archivo", new byte[0])));

		assertEquals(400, error.getStatusCode());
	}

	/**
	 * Comprueba que un archivo con extensión y tipo MIME de PDF se rechaza con
	 * código HTTP 415 cuando su contenido no presenta la firma esperada.
	 */
	@Test
	void rechazaFormatoFalsoAunqueElMimeDigaPdf() {
		MockMultipartFile archivo = archivo("falso.pdf", "texto que no es PDF");

		DocumentoException error = assertThrows(DocumentoException.class, () -> validator.validar(archivo));

		assertEquals(415, error.getStatusCode());
	}

	/**
	 * Comprueba que un archivo cuyo tamaño coincide exactamente con el límite
	 * configurado se acepta y que un byte adicional provoca una
	 * {@link DocumentoException} con código HTTP 413.
	 *
	 * <p>
	 * También verifica que el nombre del archivo aceptado se normaliza a
	 * {@code documento.txt}.
	 * </p>
	 */
	@Test
	void permiteExactamenteElLimiteYRechazaElByteSiguiente() {
		DocumentoArchivoValidator limitado = new DocumentoArchivoValidator("5B");

		assertEquals("documento.txt", limitado.validar(archivo("texto.txt", "12345")).nombre());

		DocumentoException error = assertThrows(DocumentoException.class,
				() -> limitado.validar(archivo("texto.txt", "123456")));

		assertEquals(413, error.getStatusCode());
	}

	/**
	 * Comprueba que la validación básica admite un archivo con extensión DOC y
	 * firma de contenedor OLE, y normaliza su nombre a {@code documento.doc}.
	 *
	 * <p>
	 * Esta prueba verifica el reconocimiento de la firma; no comprueba la
	 * estructura completa de un documento Word.
	 * </p>
	 */
	@Test
	void aceptaDocAntiguoConFirmaDeContenedorOle() {
		byte[] firma = java.util.HexFormat.of().parseHex("d0cf11e0a1b11ae1");

		assertEquals("documento.doc", validator
				.validar(new MockMultipartFile("archivo", "antiguo.doc", "application/msword", firma)).nombre());
	}

	/**
	 * Comprueba que la identificación de archivos DOCX examina las entradas del
	 * contenedor ZIP.
	 *
	 * <p>
	 * Acepta un contenedor con las entradas {@code [Content_Types].xml} y
	 * {@code word/document.xml}, y rechaza otro que carece de la entrada del
	 * documento Word.
	 * </p>
	 *
	 * @throws IOException si ocurre un error al construir los archivos ZIP
	 *                     utilizados por la prueba
	 */
	@Test
	void verificaQueUnZipContengaUnDocumentoWord() throws IOException {
		byte[] word = crearZip("[Content_Types].xml", "word/document.xml");

		assertEquals("documento.docx",
				validator.validar(new MockMultipartFile("archivo", "word.docx", "application/zip", word)).nombre());

		byte[] otroZip = crearZip("[Content_Types].xml", "datos.txt");

		assertThrows(DocumentoException.class,
				() -> validator.validar(new MockMultipartFile("archivo", "falso.docx", "application/zip", otroZip)));
	}

	/**
	 * Construye un archivo multipart de prueba con contenido UTF-8.
	 *
	 * <p>
	 * Declara deliberadamente el tipo MIME {@code application/pdf},
	 * independientemente del nombre y del contenido, para permitir comprobar que el
	 * validador no confía únicamente en ese dato.
	 * </p>
	 *
	 * @param nombre nombre original asignado al archivo de prueba
	 * @param texto  contenido que se codifica mediante UTF-8
	 * @return archivo multipart en memoria con el campo {@code archivo}
	 */
	private static MockMultipartFile archivo(String nombre, String texto) {
		return new MockMultipartFile("archivo", nombre, "application/pdf", texto.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Construye un contenedor ZIP en memoria con las entradas indicadas.
	 *
	 * <p>
	 * Cada entrada contiene el texto {@code <document/>} codificado mediante UTF-8.
	 * El resultado permite probar la inspección de nombres de entradas sin utilizar
	 * archivos en disco.
	 * </p>
	 *
	 * @param nombres nombres de las entradas que contendrá el ZIP
	 * @return bytes del contenedor ZIP generado
	 * @throws IOException si ocurre un error al escribir o cerrar el contenedor ZIP
	 */
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