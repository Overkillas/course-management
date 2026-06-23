package io.github.kaike.course.service;

import io.github.kaike.center.domain.Center;
import io.github.kaike.center.repository.CenterRepository;
import io.github.kaike.course.domain.Course;
import io.github.kaike.course.dtos.CourseResponse;
import io.github.kaike.course.dtos.CreateCourseRequest;
import io.github.kaike.course.dtos.UpdateCourseNameRequest;
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
 * referenciado existe antes de persistir. A listagem traz, em cada curso, a quantidade de alunos
 * matriculados (no cadastro a contagem é sempre 0, por ser um curso novo).
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
        return courseRepository.listAllWithStudentCount().stream()
            .map(row -> mapper.toResponse(row.course(), row.studentCount()))
            .toList();
    }

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        Center center = centerRepository.findByIdOptional(request.centerId())
            .orElseThrow(() -> new InvalidRequestException(
                "centerId", "Centro " + request.centerId() + " não encontrado"));

        Course course = mapper.toEntity(request, center);
        courseRepository.persist(course);
        return mapper.toResponse(course, 0);
    }

    /**
     * Atualiza o nome de um curso (só o nome é editável). A entidade é gerenciada, então o UPDATE
     * sai no commit (dirty checking).
     *
     * Retorna o CourseResponse completo, recalculando a contagem de alunos para a resposta ser a
     * representação canônica do curso (igual ao create e à listagem). Considerei devolver 204 sem
     * corpo: o studentCount não muda no rename e o cliente, vindo da listagem, já o tem, então
     * recontar é redundante no fluxo comum. Optei pela primeira (200 com o recurso) pela uniformidade da
     * representação e simetria com o create; o custo é um COUNT trivial.
     */
    @Transactional
    public CourseResponse updateName(Integer id, UpdateCourseNameRequest request) {
        Course course = courseRepository.findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curso " + id + " não encontrado"));
        course.setName(request.name());
        return mapper.toResponse(course, courseRepository.countStudents(id));
    }

    @Transactional
    public void delete(Integer id) {
        boolean deleted = courseRepository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Curso " + id + " não encontrado");
        }
    }
}
