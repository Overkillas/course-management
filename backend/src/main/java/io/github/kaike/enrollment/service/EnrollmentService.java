package io.github.kaike.enrollment.service;

import io.github.kaike.course.domain.Course;
import io.github.kaike.course.repository.CourseRepository;
import io.github.kaike.enrollment.domain.Enrollment;
import io.github.kaike.enrollment.dtos.CreateEnrollmentRequest;
import io.github.kaike.enrollment.dtos.EnrollmentResponse;
import io.github.kaike.enrollment.mapper.EnrollmentMapper;
import io.github.kaike.enrollment.repository.EnrollmentRepository;
import io.github.kaike.shared.exceptions.ConflictException;
import io.github.kaike.shared.exceptions.InvalidRequestException;
import io.github.kaike.shared.exceptions.ResourceNotFoundException;
import io.github.kaike.user.domain.User;
import io.github.kaike.user.domain.UserRole;
import io.github.kaike.user.dtos.StudentResponse;
import io.github.kaike.user.mapper.UserMapper;
import io.github.kaike.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Regra de negócio da matrícula, sempre a partir do curso: matricular um aluno e listar os
 * matriculados. A regra central (um aluno não se matricula duas vezes no mesmo curso) é
 * garantida aqui e reforçada pela constraint UNIQUE no banco.
 */
@ApplicationScoped
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentMapper mapper;
    private final UserMapper userMapper;

    @Inject
    public EnrollmentService(
        EnrollmentRepository enrollmentRepository,
        CourseRepository courseRepository,
        UserRepository userRepository,
        EnrollmentMapper mapper,
        UserMapper userMapper
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    public List<StudentResponse> listStudents(Integer courseId) {
        requireCourse(courseId);
        List<User> students = enrollmentRepository.listByCourseWithStudent(courseId).stream()
            .map(Enrollment::getUser)
            .toList();
        return userMapper.toStudentResponseList(students);
    }

    @Transactional
    public EnrollmentResponse enroll(Integer courseId, CreateEnrollmentRequest request) {
        Course course = requireCourse(courseId);
        User student = requireStudent(request.studentId());

        if (enrollmentRepository.exists(student.getId(), course.getId())) {
            throw new ConflictException("Aluno já matriculado neste curso");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(student);
        enrollment.setCourse(course);

        // O pré-check acima resolve o duplicado comum; este try/catch cobre a corrida: duas
        // requisições podem passar o exists() ao mesmo tempo (cada uma no seu snapshot) e só a
        // UNIQUE do banco barra a segunda. persistAndFlush força o INSERT agora (não no commit),
        // para a violação estourar aqui e virar 409 em vez de 500.
        try {
            enrollmentRepository.persistAndFlush(enrollment);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new ConflictException("Aluno já matriculado neste curso");
            }
            throw e;
        }
        return mapper.toResponse(enrollment);
    }

    /**
     * Busca o curso do path ou lança 404 (recurso de URL inexistente). Virou método por
     * reúso: tanto listStudents quanto enroll precisam resolver o curso da mesma forma.
     */
    private Course requireCourse(Integer courseId) {
        return courseRepository.findByIdOptional(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Curso " + courseId + " não encontrado"));
    }

    /**
     * Resolve um aluno válido a partir do studentId do corpo: 400 se não existe, e 400 se o
     * usuário não for um aluno. Diferente do requireCourse, este é usado num único lugar
     * (enroll); extraí mesmo assim por legibilidade, para o enroll ler como uma sequência de
     * "require" e para dar nome à regra "me dê um aluno válido".
     */
    private User requireStudent(Integer studentId) {
        User student = userRepository.findByIdOptional(studentId)
            .orElseThrow(() -> new InvalidRequestException(
                "studentId", "Aluno " + studentId + " não encontrado"));

        if (student.getRole() != UserRole.ALUNO) {
            throw new InvalidRequestException(
                "studentId", "Usuário " + studentId + " não é um aluno");
        }
        return student;
    }
}
