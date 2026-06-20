package io.github.kaike.shared.exceptions;

/**
 * Lançada quando a autenticação falha (e-mail inexistente ou senha errada). O handler global
 * a converte em 401 com mensagem genérica, sem distinguir os dois casos, para não revelar se
 * um e-mail existe.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
