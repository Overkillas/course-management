package io.github.kaike.shared.exceptions;

/**
 * Lançada quando o recurso endereçado pela URL não existe (ex.: excluir um curso por id
 * inexistente). É uma exceção de domínio (não WebApplicationException); o handler global a
 * converte em 404 (ver decisões 6). Para referência inválida no corpo da requisição, use
 * {@link InvalidRequestException} (400), não esta.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
