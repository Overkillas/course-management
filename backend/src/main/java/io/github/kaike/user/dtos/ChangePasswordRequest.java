package io.github.kaike.user.dtos;

import io.github.kaike.user.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Corpo da troca de senha do próprio usuário. Só a senha nova: a identidade vem do token e o
 * fluxo de primeiro acesso vem logo após o login, então pedir a senha atual de novo seria
 * redundante. A nova precisa ser forte ({@link StrongPassword}).
 */
public record ChangePasswordRequest(

    @NotBlank
    @StrongPassword
    @Schema(examples = "NovaSenha!123")
    String newPassword
) {
}
