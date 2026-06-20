package io.github.kaike.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Credenciais de login. Sem política de complexidade aqui: o login só confere se a senha
 * está correta (contra o hash), não se é forte. Ver planejamento 5.
 */
public record LoginRequest(

    @NotBlank
    @Email
    @Schema(examples = "admin@unifor.br")
    String email,

    @NotBlank
    @Schema(examples = "Ab!12345")
    String password
) {
}
