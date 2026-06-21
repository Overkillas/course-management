package io.github.kaike.user.resource;

import io.github.kaike.user.dtos.ChangePasswordRequest;
import io.github.kaike.user.dtos.UserResponse;
import io.github.kaike.user.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Self-service do usuário autenticado: consultar o próprio perfil e trocar a própria senha. A
 * identidade vem do token (claim sub = id do usuário), nunca do path ou do corpo, então o
 * usuário só acessa os próprios dados. Aberto a qualquer papel autenticado. Recurso separado do
 * StudentResource de propósito (ver decisões §2.4): mesma regra por baixo (UserService), mas
 * rota e autorização distintas.
 */
@Path("/me")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Perfil", description = "Dados e senha do próprio usuário autenticado")
@Authenticated
public class MeResource {

    private final UserService service;
    private final JsonWebToken jwt;

    @Inject
    public MeResource(UserService service, JsonWebToken jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @GET
    @Operation(summary = "Retorna o perfil do usuário autenticado")
    public UserResponse me() {
        return service.getProfile(currentUserId());
    }

    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Troca a senha do usuário autenticado (obrigatória no primeiro acesso)")
    public Response changePassword(@Valid ChangePasswordRequest request) {
        service.changePassword(currentUserId(), request);
        return Response.noContent().build();
    }

    private Integer currentUserId() {
        return Integer.valueOf(jwt.getSubject());
    }
}
