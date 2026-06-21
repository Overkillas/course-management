package io.github.kaike.enrollment.repository;

import io.github.kaike.course.domain.Course;
import io.github.kaike.course.repository.CourseWithStudentCount;
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
     * Cursos em que um aluno está matriculado, com o centro materializado (JOIN FETCH) e a
     * quantidade total de alunos de cada curso. Enraizada em Course (filtrando por EXISTS de
     * matrícula do aluno) para o JOIN FETCH do centro sair limpo. O Object[] do projection fica
     * contido aqui, mapeado para um {@link CourseWithStudentCount} tipado.
     */
    public List<CourseWithStudentCount> listEnrolledCoursesWithStudentCount(Integer studentId) {
        return getEntityManager().createQuery(
                "SELECT c, (SELECT COUNT(e2.id) FROM Enrollment e2 WHERE e2.course = c) "
                    + "FROM Course c JOIN FETCH c.center "
                    + "WHERE EXISTS (SELECT 1 FROM Enrollment e WHERE e.course = c AND e.user.id = ?1) "
                    + "ORDER BY c.name",
                Object[].class)
            .setParameter(1, studentId).getResultList().stream()
            .map(row -> new CourseWithStudentCount((Course) row[0], (Long) row[1]))
            .toList();
    }
}
