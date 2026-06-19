package io.github.kaike.course.service;

import io.github.kaike.center.domain.Center;
import io.github.kaike.center.repository.CenterRepository;
import io.github.kaike.course.domain.Course;
import io.github.kaike.course.dtos.CourseResponse;
import io.github.kaike.course.dtos.CreateCourseRequest;
import io.github.kaike.course.mapper.CourseMapper;
import io.github.kaike.course.repository.CourseRepository;
import io.github.kaike.shared.exceptions.InvalidRequestException;
import io.github.kaike.shared.exceptions.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Regra de negócio do curso: cadastrar, listar e excluir. A criação valida que o centro
 * referenciado existe antes de persistir.
 */
@ApplicationScoped
public class CourseService {

    private final CourseRepository courseRepository;
    private final CenterRepository centerRepository;
    private final CourseMapper mapper;

    @Inject
    public CourseService(
        CourseRepository courseRepository,
        CenterRepository centerRepository,
        CourseMapper mapper
    ) {
        this.courseRepository = courseRepository;
        this.centerRepository = centerRepository;
        this.mapper = mapper;
    }

    public List<CourseResponse> listAll() {
        return mapper.toResponseList(courseRepository.listAllWithCenter());
    }

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        Center center = centerRepository.findByIdOptional(request.centerId())
            .orElseThrow(() -> new InvalidRequestException(
                "centerId", "Centro " + request.centerId() + " não encontrado"));

        Course course = mapper.toEntity(request, center);
        courseRepository.persist(course);
        return mapper.toResponse(course);
    }

    @Transactional
    public void delete(Integer id) {
        boolean deleted = courseRepository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Curso " + id + " não encontrado");
        }
    }
}
