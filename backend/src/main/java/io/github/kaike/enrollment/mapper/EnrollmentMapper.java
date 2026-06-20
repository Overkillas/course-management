package io.github.kaike.enrollment.mapper;

import io.github.kaike.enrollment.domain.Enrollment;
import io.github.kaike.enrollment.dtos.EnrollmentResponse;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Conversão entre {@link Enrollment} e seu DTO de saída (decisões 5).
 */
@ApplicationScoped
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
            enrollment.getId(),
            enrollment.getUser().getId(),
            enrollment.getCourse().getId(),
            enrollment.getCreatedAt()
        );
    }
}
