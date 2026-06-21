package io.github.kaike.user.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Senha forte: mínimo de 8 caracteres, com ao menos uma letra, um dígito e um caractere
 * especial. Aplicada à senha que o usuário define na troca, não à senha inicial gerada pelo
 * admin (a inicial é descartável e trocada no primeiro acesso, por isso não passa por esta regra).
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {

    String message() default "A senha deve ter ao menos 8 caracteres, incluindo uma letra, um dígito e um caractere especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
