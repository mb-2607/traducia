package co.edu.unbosque.traducia.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.traducia.dto.ArchivoMultipartDTO;
import co.edu.unbosque.traducia.dto.DocumentoExtraidoResponseDTO;
import co.edu.unbosque.traducia.entity.PlanType;
import co.edu.unbosque.traducia.exception.DocumentoException;

/**
 * Coordina la validación del archivo, extracción y consulta del plan real.
 * TXT se lee localmente en UTF-8; PDF, DOCX e imágenes se envían a LlamaParse.
 * Este servicio no traduce, no persiste archivos y no modifica la suscripción.
 */
@Service
public class DocumentoService {

    /** Límite técnico de vista previa, independiente de los límites de cada plan. */
    private static final int MAX_CARACTERES_EXTRAIDOS = 100_000;

    private final DocumentoArchivoValidator validator;
    private final LlamaParseService llamaParseService;
    private final UserService userService;

    /**
     * Inyecta las responsabilidades del proceso de extracción.
     *
     * @param validator validación del archivo y lectura de TXT
     * @param llamaParseService adaptador del proveedor documental
     * @param userService consulta de usuarios y su plan actual
     */
    public DocumentoService(DocumentoArchivoValidator validator,
            LlamaParseService llamaParseService, UserService userService) {
        this.validator = validator;
        this.llamaParseService = llamaParseService;
        this.userService = userService;
    }

    /**
     * Extrae una vista previa sin recortar el texto al límite del plan.
     *
     * @param archivo documento recibido
     * @param idiomaOcr idioma escrito en el documento; no es el idioma de destino
     * @param nombreUsuario nombre tomado de Authentication, no del formulario
     * @return texto completo y su relación con el límite del plan
     */
    public DocumentoExtraidoResponseDTO extraer(
            MultipartFile archivo, String idiomaOcr, String nombreUsuario) {
        PlanType plan = userService.obtenerEntidadPorNombre(nombreUsuario).getPlanType();
        ArchivoMultipartDTO preparado = validator.validar(archivo);
        String texto = preparado.nombre().endsWith(".txt")
                ? validator.leerTextoUtf8(preparado.contenido())
                : llamaParseService.extraer(preparado, idiomaOcr);

        if (texto.isBlank()) {
            throw new DocumentoException(422, "No se encontró texto legible en el archivo.");
        }
        if (texto.length() > MAX_CARACTERES_EXTRAIDOS) {
            throw new DocumentoException(422,
                    "El documento supera los 100.000 caracteres de extracción. Divide el archivo antes de subirlo.");
        }
        long limite = plan.getNumeroCaracterPorSolicitud();
        return new DocumentoExtraidoResponseDTO(texto, texto.length(), limite, texto.length() > limite);
    }
}
