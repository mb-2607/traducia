package co.edu.unbosque.traducia.dto;

/**
 * Respuesta al crear un trabajo mediante POST /api/v2/parse/upload.
 *
 * @param id identificador remoto del trabajo
 * @param status estado inicial, normalmente PENDING
 */
public record LlamaParseJobDTO(String id, String status) {
}
