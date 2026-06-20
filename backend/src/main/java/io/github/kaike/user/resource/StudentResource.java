package io.github.kaike.user.resource;

import io.github.kaike.user.dtos.CreateStudentRequest;
import io.github.kaike.user.dtos.StudentResponse;
import io.github.kaike.user.service.StudentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
 * Exposição HTTP do aluno: cadastrar, listar e excluir. Operações de gestão do administrador
 * (o controle de acesso por papel entra na Etapa 3).
 */
@Path("/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Alunos", description = "Cadastro, listagem e exclusão de alunos")
@RolesAllowed("admin")
public class StudentResource {

    private final StudentService service;

    @Inject
    public StudentResource(StudentService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "Lista todos os alunos")
    public List<StudentResponse> list() {
        return service.listAll();
    }

    @POST
    @Operation(summary = "Cadastra um aluno")
    public Response create(@Valid CreateStudentRequest request) {
        StudentResponse created = service.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Exclui um aluno")
    public Response delete(@PathParam("id") @Parameter(example = "1") Integer id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
