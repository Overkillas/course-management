package io.github.kaike.course.repository;

import io.github.kaike.course.domain.Course;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Acesso a dados de {@link Course}. Padrao Repository (decisoes 2.2). PK Integer.
 */
@ApplicationScoped
public class CourseRepository implements PanacheRepositoryBase<Course, Integer> {

    /**
     * Lista os cursos com o centro ja materializado (JOIN FETCH), em vez de deixar a
     * associacao lazy disparar uma consulta por curso (problema N+1). Ver decisoes 2.3.
     */
    public List<Course> listAllWithCenter() {
        return find("SELECT c FROM Course c JOIN FETCH c.center").list();
    }
}
