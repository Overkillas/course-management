package io.github.kaike.enrollment.resource;

import io.github.kaike.course.dtos.CourseResponse;
import io.github.kaike.enrollment.service.EnrollmentService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Self-service do aluno: lista os cursos em que ele próprio está matriculado. Vive no módulo
 * enrollment (a regra é de matrícula), mas é um resource separado do EnrollmentResource porque
 * a rota (/me/courses) e a tranca (@Authenticated, não admin-only) são distintas (ver decisões
 * §2.4). A identidade vem do token (claim sub), nunca do path.
 */
@Path("/me/courses")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Perfil", description = "Dados do próprio usuário autenticado")
@Authenticated
public class MyCoursesResource {

    private final EnrollmentService service;
    private final JsonWebToken jwt;

    @Inject
    public MyCoursesResource(EnrollmentService service, JsonWebToken jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @GET
    @Operation(summary = "Lista os cursos em que o aluno autenticado está matriculado")
    public List<CourseResponse> myCourses() {
        Integer userId = Integer.valueOf(jwt.getSubject());
        return service.listMyCourses(userId);
    }
}
