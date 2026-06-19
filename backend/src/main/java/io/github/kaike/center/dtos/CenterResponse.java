package io.github.kaike.center.dtos;

/**
 * Representacao de saida de um centro. Expoe so o necessario para o consumidor (o front
 * preencher um campo de selecao ao cadastrar curso): id e a sigla. Nao vaza a entidade.
 */
public record CenterResponse(Integer id, String code) {
}
