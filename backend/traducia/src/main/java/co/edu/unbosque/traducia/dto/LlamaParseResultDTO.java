package co.edu.unbosque.traducia.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Resultado de GET /api/v2/parse/{id}?expand=text,markdown.
 * La representación Markdown se consulta para detectar páginas fallidas;
 * a TraducIA solo se devuelve el texto plano.
 *
 * @param job estado actual del trabajo
 * @param text texto por página
 * @param markdown estado de extracción por página
 */
public record LlamaParseResultDTO(Trabajo job, Texto text, Markdown markdown) {

    /** @param id identificador del trabajo; @param status estado remoto. */
    public record Trabajo(String id, String status) {
    }

    /** @param pages páginas en el orden del documento original. */
    public record Texto(List<PaginaTexto> pages) {
    }

    /**
     * Página de texto plano.
     *
     * @param numero número de página basado en uno
     * @param text texto reconocido
     */
    public record PaginaTexto(@SerializedName("page_number") int numero, String text) {
    }

    /** @param pages resultados de conversión por página. */
    public record Markdown(List<PaginaEstado> pages) {
    }

    /**
     * Estado de una página, sin conservar su contenido Markdown.
     *
     * @param numero número de página basado en uno
     * @param success true cuando la página pudo procesarse
     */
    public record PaginaEstado(@SerializedName("page_number") int numero, Boolean success) {
    }
}
