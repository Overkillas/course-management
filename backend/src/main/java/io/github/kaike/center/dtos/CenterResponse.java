package io.github.kaike.center.dtos;

/**
 * Representação de saída de um centro. Expõe só o necessário para o consumidor (o front
 * preencher um campo de seleção ao cadastrar curso): id e a sigla. Não vaza a entidade.
 */
public record CenterResponse(Integer id, String code) {
}
