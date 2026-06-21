package io.github.kaike.course.mapper;

import io.github.kaike.center.domain.Center;
import io.github.kaike.center.mapper.CenterMapper;
import io.github.kaike.course.domain.Course;
import io.github.kaike.course.dtos.CourseResponse;
import io.github.kaike.course.dtos.CreateCourseRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Conversão manual entre {@link Course} e seus DTOs (decisões 5). Reaproveita o
 * {@link CenterMapper} para o centro aninhado na resposta, mantendo o service livre de saber a
 * forma do CourseResponse. A contagem de alunos (studentCount) é calculada por consulta e
 * injetada pelo service, então o mapper só a repassa.
 */
@ApplicationScoped
public class CourseMapper {

    private final CenterMapper centerMapper;

    @Inject
    public CourseMapper(CenterMapper centerMapper) {
        this.centerMapper = centerMapper;
    }

    /** O centro já resolvido (e validado) é injetado pelo service; o mapper só monta a entidade. */
    public Course toEntity(CreateCourseRequest request, Center center) {
        Course course = new Course();
        course.setName(request.name());
        course.setTotalSemesters(request.totalSemesters());
        course.setCenter(center);
        return course;
    }

    public CourseResponse toResponse(Course course, long studentCount) {
        return new CourseResponse(
            course.getId(),
            course.getName(),
            course.getTotalSemesters(),
            centerMapper.toResponse(course.getCenter()),
            studentCount
        );
    }
}
