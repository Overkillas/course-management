package io.github.kaike.course.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Dados de entrada para cadastrar um curso. As constraints são a primeira linha de defesa
 * (validação na aplicação); as principais também são reforçadas no schema do banco (ver
 * db_diagram 3.4). O tamanho mínimo do nome fica só na aplicação.
 */
public record CreateCourseRequest(

    @NotBlank
    @Size(min = 3, max = 100)
    @Schema(examples = "Ciência da Computação")
    String name,

    @NotNull
    @Schema(examples = "1")
    Integer centerId,

    @NotNull
    @Positive
    @Max(100)
    @Schema(examples = "8")
    Integer totalSemesters
) {
}
