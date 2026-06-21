package io.github.kaike.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Dados de entrada para o admin cadastrar um aluno. A senha é a senha inicial (temporária);
 * o aluno troca no primeiro acesso. O limite de 72 (bytes) no password é o teto do bcrypt.
 */
public record CreateStudentRequest(

    @NotBlank
    @Size(max = 100)
    @Schema(examples = "Mario")
    String name,

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(examples = "aluno@edu.unifor.br")
    String email,

    @NotBlank
    /*
     * Por mais que 72 caracteres não garanta os 72 bytes (ç conta mais de um
     * byte por exemplo) a maioria dos casos vai ser ASCII, e fazer essa
     * validação mais garantida não agregaria muito.
     */
    @Size(min = 8, max = 72)
    @Schema(examples = "12345678")
    String password
) {
}
