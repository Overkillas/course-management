package io.github.kaike.user.domain;

/**
 * Papel do usuario no sistema. Enum comportamental: a autorizacao raciocina sobre
 * este valor (admin vs aluno), entao ele vive no codigo. Persistido como string
 * (nunca ordinal) pelo {@link UserRoleConverter}. Ver db_diagram.md 3.3.
 */
public enum UserRole {
    ADMIN,
    ALUNO
}
