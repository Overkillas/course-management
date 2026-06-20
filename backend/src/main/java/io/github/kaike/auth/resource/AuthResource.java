package io.github.kaike.auth.resource;

import io.github.kaike.auth.dtos.LoginRequest;
import io.github.kaike.auth.dtos.LoginResponse;
import io.github.kaike.auth.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposição HTTP da autenticação. Endpoint público: o login é o que emite o token.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthResource {

    private final AuthService service;

    @Inject
    public AuthResource(AuthService service) {
        this.service = service;
    }

    @POST
    @Path("/login")
    @Operation(summary = "Autentica e retorna um token JWT")
    public LoginResponse login(@Valid LoginRequest request) {
        return service.login(request);
    }
}
