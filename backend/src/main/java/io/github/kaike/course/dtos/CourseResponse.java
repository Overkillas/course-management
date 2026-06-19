package io.github.kaike.course.dtos;

import io.github.kaike.center.dtos.CenterResponse;

/**
 * Representacao de saida de um curso. Inclui o centro aninhado (reaproveitando o DTO de
 * centro) para o consumidor nao precisar de uma segunda chamada para resolver a sigla.
 */
public record CourseResponse(
    Integer id,
    String name,
    Integer totalSemesters,
    CenterResponse center
) {
}
