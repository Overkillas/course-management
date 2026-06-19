package io.github.kaike.shared.exceptions;

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
 * Tratador global de excecoes: captura falhas e as converte para {@link ApiError}, de modo
 * que o tratamento de erro nao fique espalhado pelos resources (ver decisoes 6). O Quarkus
 * escolhe o mapper pelo tipo mais especifico da excecao.
 */
public class GlobalExceptionMappers {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMappers.class);

    /** Falha de Bean Validation na entrada: 400 com a lista de campos invalidos. */
    @ServerExceptionMapper
    public Response handleValidation(ConstraintViolationException ex, UriInfo uriInfo) {
        List<ApiError.FieldViolation> violations = ex.getConstraintViolations().stream()
            .map(v -> new ApiError.FieldViolation(fieldName(v), v.getMessage()))
            .toList();

        ApiError error = ApiError.withViolations(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST.getReasonPhrase(),
            "Dados de entrada invalidos",
            uriInfo.getPath(),
            violations
        );
        return build(Response.Status.BAD_REQUEST.getStatusCode(), error);
    }

    /** Recurso enderecado pela URL nao existe: 404. */
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

    /** Valor invalido no corpo (ex.: referencia inexistente): 400 como violacao de campo. */
    @ServerExceptionMapper
    public Response handleInvalidRequest(InvalidRequestException ex, UriInfo uriInfo) {
        ApiError error = ApiError.withViolations(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST.getReasonPhrase(),
            "Dados de entrada invalidos",
            uriInfo.getPath(),
            List.of(new ApiError.FieldViolation(ex.getField(), ex.getMessage()))
        );
        return build(Response.Status.BAD_REQUEST.getStatusCode(), error);
    }

    /** Excecoes do proprio JAX-RS (rota inexistente, metodo nao suportado, etc.). */
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

    /** Qualquer falha nao prevista: 500 generico, sem vazar detalhes internos. */
    @ServerExceptionMapper
    public Response handleUnexpected(Throwable ex, UriInfo uriInfo) {
        LOG.error("Erro inesperado ao processar a requisicao", ex);
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

    /** Extrai o nome do campo (ultimo no do caminho da propriedade). */
    private static String fieldName(ConstraintViolation<?> violation) {
        String field = null;
        for (Path.Node node : violation.getPropertyPath()) {
            field = node.getName();
        }
        return field;
    }
}
