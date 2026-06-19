package io.github.kaike.course.resource;

import io.github.kaike.course.dtos.CourseResponse;
import io.github.kaike.course.dtos.CreateCourseRequest;
import io.github.kaike.course.service.CourseService;
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
 * Exposição HTTP do curso: cadastrar, listar e excluir. A validação de entrada é disparada
 * por @Valid; falhas viram 400 formatado pelo handler global.
 */
@Path("/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cursos", description = "Cadastro, listagem e exclusão de cursos")
public class CourseResource {

    private final CourseService service;

    @Inject
    public CourseResource(CourseService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "Lista todos os cursos")
    public List<CourseResponse> list() {
        return service.listAll();
    }

    @POST
    @Operation(summary = "Cadastra um curso")
    public Response create(@Valid CreateCourseRequest request) {
        CourseResponse created = service.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Exclui um curso")
    public Response delete(@PathParam("id") @Parameter(example = "1") Integer id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
