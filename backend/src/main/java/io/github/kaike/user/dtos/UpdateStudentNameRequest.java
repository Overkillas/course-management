package io.github.kaike.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Corpo da atualização de um aluno. Só o nome é editável (e-mail e papel não mudam por aqui),
 * então é um PATCH (atualização parcial). A regra do nome espelha a do cadastro.
 */
public record UpdateStudentNameRequest(

    @NotBlank
    @Size(max = 100)
    @Schema(examples = "Mario Silva")
    String name
) {
}
