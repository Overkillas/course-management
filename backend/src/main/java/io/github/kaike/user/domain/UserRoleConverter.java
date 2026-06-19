package io.github.kaike.user.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persiste {@link UserRole} como string em minúsculas (admin/aluno), coerente com os
 * valores do db_diagram.md e com o DEFAULT 'aluno' da migration. Converter explícito
 * (em vez de @Enumerated(STRING)) para que o valor gravado case exatamente com o schema,
 * sem depender do nome em maiúsculas das constantes Java.
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.name().toLowerCase();
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        return value == null ? null : UserRole.valueOf(value.toUpperCase());
    }
}
