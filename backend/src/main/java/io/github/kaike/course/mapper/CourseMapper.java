package io.github.kaike.course.mapper;

import io.github.kaike.center.domain.Center;
import io.github.kaike.center.mapper.CenterMapper;
import io.github.kaike.course.domain.Course;
import io.github.kaike.course.dtos.CourseResponse;
import io.github.kaike.course.dtos.CreateCourseRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Conversao manual entre {@link Course} e seus DTOs (decisoes 5). Reaproveita o
 * {@link CenterMapper} para o centro aninhado na resposta, mantendo o service livre de
 * saber a forma do CourseResponse.
 */
@ApplicationScoped
public class CourseMapper {

    private final CenterMapper centerMapper;

    @Inject
    public CourseMapper(CenterMapper centerMapper) {
        this.centerMapper = centerMapper;
    }

    /** O centro ja resolvido (e validado) e injetado pelo service; o mapper so monta a entidade. */
    public Course toEntity(CreateCourseRequest request, Center center) {
        Course course = new Course();
        course.setName(request.name());
        course.setTotalSemesters(request.totalSemesters());
        course.setCenter(center);
        return course;
    }

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(
            course.getId(),
            course.getName(),
            course.getTotalSemesters(),
            centerMapper.toResponse(course.getCenter())
        );
    }

    public List<CourseResponse> toResponseList(List<Course> courses) {
        return courses.stream().map(this::toResponse).toList();
    }
}
