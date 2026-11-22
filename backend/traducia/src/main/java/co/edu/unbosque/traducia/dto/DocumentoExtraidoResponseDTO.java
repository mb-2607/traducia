package co.edu.unbosque.traducia.dto;

/**
 * Vista previa del documento, antes de solicitar una traducción.
 * El texto se conserva completo para permitir su revisión y edición.
 *
 * @param texto texto extraído, sin recortarlo al límite del plan
 * @param caracteres longitud en unidades UTF-16, igual que String.length() en Java
 * @param limiteCaracteres límite obtenido del plan del usuario en el servidor
 * @param excedeLimite indica si el usuario debe editar el texto antes de traducir
 */
public record DocumentoExtraidoResponseDTO(
        String texto,
        int caracteres,
        long limiteCaracteres,
        boolean excedeLimite) {
}
