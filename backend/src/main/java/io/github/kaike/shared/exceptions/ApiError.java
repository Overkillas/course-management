package io.github.kaike.shared.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Formato padrao de erro da API. Toda falha tratada pelo handler global e devolvida nesta
 * estrutura, para que quem consome a API encontre sempre a mesma forma (ver decisoes 6).
 *
 * O campo {@code violations} so aparece em erros de validacao; nos demais casos e omitido
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

    /** Uma violacao de validacao em um campo especifico da entrada. */
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
