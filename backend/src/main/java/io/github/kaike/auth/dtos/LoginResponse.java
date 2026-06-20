package io.github.kaike.auth.dtos;

/**
 * Resposta do login: apenas o token. O papel e o mustChangePassword já vivem dentro do JWT
 * (claims "groups" e "mustChangePassword"), que é a fonte única. O front decodifica o token
 * para ler o que precisa na UI; o servidor usa os claims para autorização.
 */
public record LoginResponse(String token) {
}
