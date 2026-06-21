package io.github.kaike.course.dtos;

import io.github.kaike.center.dtos.CenterResponse;

/**
 * Representação de saída de um curso. Inclui o centro aninhado (reaproveitando o DTO de centro)
 * para o consumidor não precisar de uma segunda chamada para resolver a sigla, e a quantidade de
 * alunos matriculados (studentCount), poupando uma chamada por curso só para contar.
 */
public record CourseResponse(
    Integer id,
    String name,
    Integer totalSemesters,
    CenterResponse center,
    long studentCount
) {
}
