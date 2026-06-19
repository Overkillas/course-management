package io.github.kaike.user.mapper;

import io.github.kaike.user.domain.User;
import io.github.kaike.user.dtos.StudentResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Conversão manual entre {@link User} e seus DTOs (decisões 5). Por ora só a visão de aluno;
 * por isso os métodos são nomeados "student" (o User pode ganhar outras visões, como perfil,
 * na Etapa 3). A construção da entidade no cadastro fica no service, não aqui, porque envolve
 * regra de negócio (papel, troca de senha) e o hash já calculado.
 */
@ApplicationScoped
public class UserMapper {

    public StudentResponse toStudentResponse(User user) {
        return new StudentResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isMustChangePassword()
        );
    }

    public List<StudentResponse> toStudentResponseList(List<User> users) {
        return users.stream().map(this::toStudentResponse).toList();
    }
}
