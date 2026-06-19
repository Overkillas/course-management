package io.github.kaike.shared.exceptions;

/**
 * Lancada quando um valor do corpo da requisicao e invalido por uma regra que o Bean
 * Validation nao cobre sozinho -- tipicamente uma referencia para um recurso inexistente
 * (ex.: cadastrar curso apontando para um centro que nao existe). O handler global a
 * converte em 400, expondo {@link #getField()} como uma violacao de campo, para a resposta
 * ter a mesma forma dos demais erros de validacao.
 */
public class InvalidRequestException extends RuntimeException {

    private final String field;

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
