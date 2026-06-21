package io.github.kaike.shared.exceptions;

/**
 * Sinaliza que o usuário autenticado precisa trocar a senha (primeiro acesso) antes de usar
 * qualquer outro endpoint. Mapeada para 403 pelo tratador global de exceções.
 */
public class PasswordChangeRequiredException extends RuntimeException {

    public PasswordChangeRequiredException() {
        super("Troque a senha no primeiro acesso para continuar");
    }
}
