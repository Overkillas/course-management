package io.github.kaike.shared.exceptions;

/**
 * Lançada quando a requisição é válida mas conflita com o estado atual, tipicamente uma
 * violação de unicidade (e-mail já cadastrado, aluno já matriculado no curso). O handler
 * global a converte em 409. Distingue-se do 400: o dado não é malformado, ele colide com o
 * que já existe.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
