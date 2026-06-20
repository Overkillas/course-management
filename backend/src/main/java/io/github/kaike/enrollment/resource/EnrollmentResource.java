package io.github.kaike.enrollment.resource;

import io.github.kaike.enrollment.dtos.CreateEnrollmentRequest;
import io.github.kaike.enrollment.dtos.EnrollmentResponse;
import io.github.kaike.enrollment.service.EnrollmentService;
import io.github.kaike.user.dtos.StudentResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposição HTTP da matrícula como subcoleção do curso: a partir de um curso, lista os
 * alunos matriculados e matricula novos (ver decisões 8.2).
 *
 * Nota de organização: esta classe vive no módulo enrollment, não no course, mesmo o
 * {@code @Path} sendo course-nested. A escolha mantém a regra de matrícula coesa no seu
 * próprio módulo (princípio de organização por domínio, decisões 1). O custo é que o
 * espaço de URL /courses fica servido por duas classes: quem procurar estes endpoints no
 * CourseResource não os encontra, já que estão aqui.
 */
@Path("/courses/{courseId}/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Matrículas", description = "Matrícula de alunos em cursos e listagem por curso")
public class EnrollmentResource {

    private final EnrollmentService service;

    @Inject
    public EnrollmentResource(EnrollmentService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "Lista os alunos matriculados no curso")
    public List<StudentResponse> list(@PathParam("courseId") @Parameter(example = "1") Integer courseId) {
        return service.listStudents(courseId);
    }

    @POST
    @Operation(summary = "Matricula um aluno no curso")
    public Response enroll(
        @PathParam("courseId") @Parameter(example = "1") Integer courseId,
        @Valid CreateEnrollmentRequest request
    ) {
        EnrollmentResponse created = service.enroll(courseId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
