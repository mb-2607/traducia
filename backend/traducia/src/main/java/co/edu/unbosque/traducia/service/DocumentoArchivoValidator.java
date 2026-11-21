package co.edu.unbosque.traducia.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.traducia.dto.ArchivoMultipartDTO;
import co.edu.unbosque.traducia.exception.DocumentoException;

/**
 * Valida archivos antes de leer TXT o enviarlos a LlamaParse.
 * No confía en el Content-Type aportado por el navegador ni escribe en disco.
 * Las comprobaciones de firma no sustituyen la validación del parser de LlamaParse.
 */
@Component
public class DocumentoArchivoValidator {

    private static final int MAX_ENTRADAS_DOCX = 2_000;
    private static final int MAX_DOCX_DESCOMPRIMIDO = 50 * 1024 * 1024;
    private static final Set<String> FORMATOS = Set.of(
            "txt", "pdf", "docx", "doc", "png", "jpg", "jpeg", "tif", "tiff", "bmp", "webp");
    private static final Map<String, String> FIRMAS = Map.of(
            "pdf", "255044462d", "png", "89504e470d0a1a0a", "jpg", "ffd8ff",
            "jpeg", "ffd8ff", "bmp", "424d", "doc", "d0cf11e0a1b11ae1");

    /** Límite leído de la misma propiedad que limita las subidas de Spring. */
    private final int maxBytes;

    /**
     * Inicializa el límite compartido de subida. Rechaza valores no acotados.
     *
     * @param tamanio valor como 10MB; debe estar entre 1 byte y 50 MiB
     */
    public DocumentoArchivoValidator(
            @Value("${spring.servlet.multipart.max-file-size:10MB}") String tamanio) {
        long bytes = DataSize.parse(tamanio).toBytes();
        if (bytes < 1 || bytes > 50L * 1024 * 1024) {
            throw new IllegalArgumentException("El límite de documentos debe estar entre 1 byte y 50 MiB");
        }
        this.maxBytes = Math.toIntExact(bytes);
    }

    /**
     * Verifica tamaño, extensión y firma. Lee como máximo el límite más un byte.
     *
     * @param archivo archivo recibido por el controlador
     * @return archivo preparado con nombre generado y campo remoto file
     */
    public ArchivoMultipartDTO validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new DocumentoException(400, "Selecciona un archivo que tenga contenido.");
        }
        if (archivo.getSize() > maxBytes) {
            throw new DocumentoException(413, "El archivo supera el tamaño permitido.");
        }
        String extension = obtenerExtension(archivo.getOriginalFilename());
        byte[] contenido = leerContenido(archivo);
        validarFirma(extension, contenido);
        return new ArchivoMultipartDTO("file", "documento." + extension, contenido);
    }

    /**
     * Lee TXT codificado en UTF-8, con BOM opcional, sin reemplazar bytes inválidos.
     *
     * @param contenido bytes del archivo TXT
     * @return texto con sus espacios y saltos de línea conservados
     */
    public String leerTextoUtf8(byte[] contenido) {
        try {
            String texto = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(contenido)).toString();
            if (texto.indexOf('\0') >= 0) {
                throw new DocumentoException(422, "El TXT contiene datos binarios o una codificación distinta de UTF-8.");
            }
            return texto.startsWith("\uFEFF") ? texto.substring(1) : texto;
        } catch (CharacterCodingException e) {
            throw new DocumentoException(422, "Guarda el archivo TXT con codificación UTF-8.", e);
        }
    }

    /** Obtiene una extensión permitida; nunca utiliza el nombre como ruta. */
    private static String obtenerExtension(String nombre) {
        if (nombre == null || nombre.length() > 255) {
            throw new DocumentoException(400, "El nombre del archivo no es válido.");
        }
        int punto = nombre.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!FORMATOS.contains(extension)) {
            throw new DocumentoException(415,
                    "Formato no admitido. Usa TXT, PDF, DOC/DOCX, PNG, JPG, TIFF, BMP o WebP.");
        }
        return extension;
    }

    /** Evita leer sin límite un archivo cuyo tamaño declarado no sea fiable. */
    private byte[] leerContenido(MultipartFile archivo) {
        try (InputStream entrada = archivo.getInputStream()) {
            byte[] contenido = entrada.readNBytes(maxBytes + 1);
            if (contenido.length > maxBytes) {
                throw new DocumentoException(413, "El archivo supera el tamaño permitido.");
            }
            if (contenido.length == 0) {
                throw new DocumentoException(400, "El archivo está vacío.");
            }
            return contenido;
        } catch (IOException e) {
            throw new DocumentoException(500, "No se pudo leer el archivo recibido.", e);
        }
    }

    /** Comprueba firmas básicas antes de delegar la interpretación al proveedor. */
    private static void validarFirma(String extension, byte[] contenido) {
        String firma = HexFormat.of().formatHex(contenido, 0, Math.min(contenido.length, 12));
        boolean valida = switch (extension) {
            case "txt" -> true;
            case "docx" -> firma.startsWith("504b0304") && esDocxValido(contenido);
            case "tif", "tiff" -> firma.startsWith("49492a00") || firma.startsWith("4d4d002a");
            case "webp" -> firma.startsWith("52494646") && firma.endsWith("57454250")
                    && contenido.length >= 12;
            default -> firma.startsWith(FIRMAS.get(extension));
        };
        if (!valida) {
            throw new DocumentoException(415, "El contenido del archivo no corresponde al formato indicado.");
        }
    }

    /**
     * Verifica el contenedor Word sin extraer archivos al sistema.
     * Acota entradas y bytes descomprimidos para rechazar contenedores excesivos.
     */
    private static boolean esDocxValido(byte[] contenido) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(contenido))) {
            boolean documento = false;
            boolean tipos = false;
            int entradas = 0;
            long total = 0;
            byte[] buffer = new byte[8_192];
            ZipEntry entrada;
            while ((entrada = zip.getNextEntry()) != null) {
                entradas++;
                if (entradas > MAX_ENTRADAS_DOCX) {
                    throw new DocumentoException(422, "El documento Word contiene demasiados elementos.");
                }
                documento |= "word/document.xml".equals(entrada.getName());
                tipos |= "[Content_Types].xml".equals(entrada.getName());
                int leidos;
                while ((leidos = zip.read(buffer)) != -1) {
                    total += leidos;
                    if (total > MAX_DOCX_DESCOMPRIMIDO) {
                        throw new DocumentoException(422, "El documento Word descomprimido supera el límite técnico.");
                    }
                }
                zip.closeEntry();
            }
            return documento && tipos;
        } catch (IOException e) {
            throw new DocumentoException(422, "El documento Word está dañado o no puede leerse.", e);
        }
    }
}
