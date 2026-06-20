package io.github.kaike.user.resource;

import io.github.kaike.user.dtos.UserResponse;
import io.github.kaike.user.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Self-service do usuário autenticado. A identidade vem do token (claim sub = id do usuário),
 * nunca do path ou do corpo, então o usuário só acessa os próprios dados. Aberto a qualquer
 * papel autenticado. Recurso separado do StudentResource de propósito (ver decisões §2.4):
 * mesma regra por baixo (UserService), mas rota e autorização distintas.
 */
@Path("/me")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Perfil", description = "Dados do próprio usuário autenticado")
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
        Integer userId = Integer.valueOf(jwt.getSubject());
        return service.getProfile(userId);
    }
}
