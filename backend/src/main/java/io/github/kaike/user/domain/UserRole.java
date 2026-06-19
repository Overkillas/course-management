package io.github.kaike.user.domain;

/**
 * Papel do usuário no sistema. Enum comportamental: a autorização raciocina sobre
 * este valor (admin vs aluno), então ele vive no código. Persistido como string
 * (nunca ordinal) pelo {@link UserRoleConverter}. Ver db_diagram.md 3.3.
 */
public enum UserRole {
    ADMIN,
    ALUNO
}
