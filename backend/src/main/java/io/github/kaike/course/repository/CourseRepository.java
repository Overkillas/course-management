package io.github.kaike.course.repository;

import io.github.kaike.course.domain.Course;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Acesso a dados de {@link Course}. Padrão Repository (decisões 2.2). PK Integer.
 */
@ApplicationScoped
public class CourseRepository implements PanacheRepositoryBase<Course, Integer> {

    /**
     * Lista os cursos com o centro já materializado (JOIN FETCH) e a quantidade de alunos
     * matriculados em cada um. O JOIN FETCH evita o N+1 ao acessar o centro (ver decisões 2.3); a
     * contagem é um subselect sobre Enrollment referenciado apenas pelo nome no JPQL, sem import
     * Java do pacote enrollment, o que mantém o grafo de dependências acíclico (ver decisões 8.2).
     * O Object[] do projection (entidade + escalar, exigido junto do JOIN FETCH) fica contido
     * aqui, mapeado para um {@link CourseWithStudentCount} tipado.
     */
    public List<CourseWithStudentCount> listAllWithStudentCount() {
        return getEntityManager().createQuery(
                "SELECT c, (SELECT COUNT(e.id) FROM Enrollment e WHERE e.course = c) "
                    + "FROM Course c JOIN FETCH c.center ORDER BY c.name",
                Object[].class)
            .getResultList().stream()
            .map(row -> new CourseWithStudentCount((Course) row[0], (Long) row[1]))
            .toList();
    }
}
