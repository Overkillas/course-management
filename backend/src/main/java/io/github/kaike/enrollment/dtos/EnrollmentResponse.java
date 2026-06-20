package io.github.kaike.enrollment.dtos;

import java.time.LocalDateTime;

/**
 * Confirmação leve da matrícula criada. Devolve só os ids do vínculo (quem matricula já tem
 * os dados de aluno e curso em mãos); aninhar as entidades inteiras seria payload sem ganho.
 */
public record EnrollmentResponse(
    Integer id,
    Integer studentId,
    Integer courseId,
    LocalDateTime createdAt
) {
}
