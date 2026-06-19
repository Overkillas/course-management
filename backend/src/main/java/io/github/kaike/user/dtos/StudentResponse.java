package io.github.kaike.user.dtos;

/**
 * Representação de saída de um aluno. Nunca expõe o hash da senha (decisões 4). O
 * mustChangePassword permite ao front saber se deve exigir a troca no primeiro acesso.
 */
public record StudentResponse(
    Integer id,
    String name,
    String email,
    boolean mustChangePassword
) {
}
