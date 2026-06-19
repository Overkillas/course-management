package io.github.kaike.shared.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Formato padrão de erro da API. Toda falha tratada pelo handler global é devolvida nesta
 * estrutura, para que quem consome a API encontre sempre a mesma forma (ver decisões 6).
 *
 * O campo {@code violations} só aparece em erros de validação; nos demais casos é omitido
 * da resposta (JsonInclude.NON_NULL).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldViolation> violations
) {

    /** Uma violação de validação em um campo específico da entrada. */
    public record FieldViolation(String field, String message) {}

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ApiError withViolations(
        int status, String error, String message, String path, List<FieldViolation> violations
    ) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, violations);
    }
}
