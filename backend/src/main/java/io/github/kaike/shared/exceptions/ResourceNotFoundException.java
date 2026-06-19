package io.github.kaike.shared.exceptions;

/**
 * Lancada quando o recurso enderecado pela URL nao existe (ex.: excluir um curso por id
 * inexistente). E uma excecao de dominio (nao WebApplicationException); o handler global a
 * converte em 404 (ver decisoes 6). Para referencia invalida no corpo da requisicao, use
 * {@link InvalidRequestException} (400), nao esta.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
