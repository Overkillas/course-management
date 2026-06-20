package io.github.kaike.user.dtos;

/**
 * Representação de saída de um usuário (qualquer papel), usada no perfil do próprio autenticado
 * (/me). Traz só dados de exibição: id, nome e e-mail. Papel e mustChangePassword não entram
 * aqui porque o front já os tem do próprio token (decodificado no login) e os consome antes de
 * chamar /me; o dado novo que /me agrega é o nome.
 */
public record UserResponse(
    Integer id,
    String name,
    String email
) {
}
