package io.github.kaike.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validador de {@link StrongPassword}. Trata null/vazio como válido (a ausência é
 * responsabilidade do {@code @NotBlank}), e exige tamanho mínimo + letra + dígito + especial.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern LETTER = Pattern.compile("[a-zA-Z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.length() >= MIN_LENGTH
            && LETTER.matcher(value).find()
            && DIGIT.matcher(value).find()
            && SPECIAL.matcher(value).find();
    }
}
