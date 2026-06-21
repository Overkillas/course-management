package io.github.kaike.shared.exceptions;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Tratador global de exceções: captura falhas e as converte para {@link ApiError}, de modo
 * que o tratamento de erro não fique espalhado pelos resources (ver decisões 6). O Quarkus
 * escolhe o mapper pelo tipo mais específico da exceção.
 */
public class GlobalExceptionMappers {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMappers.class);

    /** Falha de Bean Validation na entrada: 400 com a lista de campos inválidos. */
    @ServerExceptionMapper
    public Response handleValidation(ConstraintViolationException ex, UriInfo uriInfo) {
        List<ApiError.FieldViolation> violations = ex.getConstraintViolations().stream()
            .map(v -> new ApiError.FieldViolation(fieldName(v), v.getMessage()))
            .toList();

        ApiError error = ApiError.withViolations(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST.getReasonPhrase(),
            "Dados de entrada inválidos",
            uriInfo.getPath(),
            violations
        );
        return build(Response.Status.BAD_REQUEST.getStatusCode(), error);
    }

    /** Recurso endereçado pela URL não existe: 404. */
    @ServerExceptionMapper
    public Response handleNotFound(ResourceNotFoundException ex, UriInfo uriInfo) {
        ApiError error = ApiError.of(
            Response.Status.NOT_FOUND.getStatusCode(),
            Response.Status.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            uriInfo.getPath()
        );
        return build(Response.Status.NOT_FOUND.getStatusCode(), error);
    }

    /** Valor inválido no corpo (ex.: referência inexistente): 400 como violação de campo. */
    @ServerExceptionMapper
    public Response handleInvalidRequest(InvalidRequestException ex, UriInfo uriInfo) {
        ApiError error = ApiError.withViolations(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST.getReasonPhrase(),
            "Dados de entrada inválidos",
            uriInfo.getPath(),
            List.of(new ApiError.FieldViolation(ex.getField(), ex.getMessage()))
        );
        return build(Response.Status.BAD_REQUEST.getStatusCode(), error);
    }

    /** Violação de unicidade (ex.: e-mail ou matrícula já existentes): 409. */
    @ServerExceptionMapper
    public Response handleConflict(ConflictException ex, UriInfo uriInfo) {
        ApiError error = ApiError.of(
            Response.Status.CONFLICT.getStatusCode(),
            Response.Status.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            uriInfo.getPath()
        );
        return build(Response.Status.CONFLICT.getStatusCode(), error);
    }

    /** Falha de autenticação (credenciais inválidas): 401, com mensagem genérica. */
    @ServerExceptionMapper
    public Response handleAuthentication(AuthenticationException ex, UriInfo uriInfo) {
        ApiError error = ApiError.of(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            Response.Status.UNAUTHORIZED.getReasonPhrase(),
            ex.getMessage(),
            uriInfo.getPath()
        );
        return build(Response.Status.UNAUTHORIZED.getStatusCode(), error);
    }

    /** Requisição sem credenciais válidas em endpoint protegido: 401. */
    @ServerExceptionMapper
    public Response handleUnauthorized(UnauthorizedException ex, UriInfo uriInfo) {
        ApiError error = ApiError.of(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            Response.Status.UNAUTHORIZED.getReasonPhrase(),
            "Autenticação necessária",
            uriInfo.getPath()
        );
        return build(Response.Status.UNAUTHORIZED.getStatusCode(), error);
    }

    /** Autenticado mas sem permissão (papel insuficiente): 403. */
    @ServerExceptionMapper
    public Response handleForbidden(ForbiddenException ex, UriInfo uriInfo) {
        ApiError error = ApiError.of(
            Response.Status.FORBIDDEN.getStatusCode(),
            Response.Status.FORBIDDEN.getReasonPhrase(),
            "Acesso negado",
            uriInfo.getPath()
        );
        return build(Response.Status.FORBIDDEN.getStatusCode(), error);
    }

    /** Usuário com troca de senha pendente acessando outro endpoint: 403 com orientação. */
    @ServerExceptionMapper
    public Response handlePasswordChangeRequired(PasswordChangeRequiredException ex, UriInfo uriInfo) {
        ApiError error = ApiError.of(
            Response.Status.FORBIDDEN.getStatusCode(),
            Response.Status.FORBIDDEN.getReasonPhrase(),
            ex.getMessage(),
            uriInfo.getPath()
        );
        return build(Response.Status.FORBIDDEN.getStatusCode(), error);
    }

    /** Exceções do próprio JAX-RS (rota inexistente, método não suportado, etc.). */
    @ServerExceptionMapper
    public Response handleWebApplication(WebApplicationException ex, UriInfo uriInfo) {
        Response.StatusType status = ex.getResponse().getStatusInfo();
        ApiError error = ApiError.of(
            status.getStatusCode(),
            status.getReasonPhrase(),
            status.getReasonPhrase(),
            uriInfo.getPath()
        );
        return build(status.getStatusCode(), error);
    }

    /** Qualquer falha não prevista: 500 genérico, sem vazar detalhes internos. */
    @ServerExceptionMapper
    public Response handleUnexpected(Throwable ex, UriInfo uriInfo) {
        LOG.error("Erro inesperado ao processar a requisição", ex);
        ApiError error = ApiError.of(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Ocorreu um erro inesperado",
            uriInfo.getPath()
        );
        return build(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), error);
    }

    private static Response build(int status, ApiError error) {
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(error).build();
    }

    /** Extrai o nome do campo (último nó do caminho da propriedade). */
    private static String fieldName(ConstraintViolation<?> violation) {
        String field = null;
        for (Path.Node node : violation.getPropertyPath()) {
            field = node.getName();
        }
        return field;
    }
}
