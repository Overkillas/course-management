package io.github.kaike.shared.exceptions;

/**
 * Lançada quando um valor do corpo da requisição é inválido por uma regra que o Bean
 * Validation não cobre sozinho -- tipicamente uma referência para um recurso inexistente
 * (ex.: cadastrar curso apontando para um centro que não existe). O handler global a
 * converte em 400, expondo {@link #getField()} como uma violação de campo, para a resposta
 * ter a mesma forma dos demais erros de validação.
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
