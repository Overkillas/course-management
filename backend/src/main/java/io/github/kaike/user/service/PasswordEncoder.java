package io.github.kaike.user.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Encapsula o hashing de senha (bcrypt, via BcryptUtil do Quarkus). Isola a escolha do
 * algoritmo num único ponto (trocá-lo seria só aqui) e permite mockar nos testes, já que o
 * bcrypt é lento de propósito.
 */
@ApplicationScoped
public class PasswordEncoder {

    public String hash(String rawPassword) {
        return BcryptUtil.bcryptHash(rawPassword);
    }
}
