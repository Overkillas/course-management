package io.github.kaike.course.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Corpo da atualização de um curso. Só o nome é editável (centro e total de semestres não mudam
 * por aqui), então é um PATCH (atualização parcial). A regra do nome espelha a do cadastro.
 */
public record UpdateCourseNameRequest(

    @NotBlank
    @Size(min = 3, max = 100)
    @Schema(examples = "Ciência da Computação")
    String name
) {
}
