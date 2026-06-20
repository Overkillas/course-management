package io.github.kaike.enrollment.repository;

import io.github.kaike.enrollment.domain.Enrollment;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Acesso a dados de {@link Enrollment}. Padrão Repository (decisões 2.2). PK Integer.
 */
@ApplicationScoped
public class EnrollmentRepository implements PanacheRepositoryBase<Enrollment, Integer> {

    /** Já existe matrícula deste aluno neste curso? Materializa a checagem da regra de unicidade. */
    public boolean exists(Integer userId, Integer courseId) {
        return count("user.id = ?1 and course.id = ?2", userId, courseId) > 0;
    }

    /**
     * Matrículas de um curso com o aluno já materializado (JOIN FETCH), para listar os alunos
     * sem o N+1 ao acessar cada matrícula (ver decisões 2.3).
     */
    public List<Enrollment> listByCourseWithStudent(Integer courseId) {
        return find(
            "SELECT e FROM Enrollment e JOIN FETCH e.user WHERE e.course.id = ?1 ORDER BY e.user.name",
            courseId
        ).list();
    }

    /**
     * Matrículas de um aluno com o curso (e seu centro) já materializados (JOIN FETCH), para
     * montar a lista de cursos do aluno sem o N+1 ao acessar cada curso (ver decisões 2.3).
     */
    public List<Enrollment> listByStudentWithCourse(Integer studentId) {
        return find(
            "SELECT e FROM Enrollment e JOIN FETCH e.course c JOIN FETCH c.center WHERE e.user.id = ?1 ORDER BY c.name",
            studentId
        ).list();
    }
}
