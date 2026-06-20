package io.github.kaike.enrollment.dtos;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Corpo da matrícula. O curso vem do path (/courses/{courseId}/students), então aqui só
 * entra o aluno a ser matriculado.
 */
public record CreateEnrollmentRequest(

    @NotNull
    @Schema(examples = "1")
    Integer studentId
) {
}
