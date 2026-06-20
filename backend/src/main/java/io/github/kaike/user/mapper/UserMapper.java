package io.github.kaike.user.mapper;

import io.github.kaike.user.domain.User;
import io.github.kaike.user.dtos.StudentResponse;
import io.github.kaike.user.dtos.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Conversão manual entre {@link User} e seus DTOs (decisões 5): a visão de aluno (gestão pelo
 * admin, com mustChangePassword) e a visão de perfil ({@link UserResponse}, usada no /me do
 * próprio usuário). A construção da entidade no cadastro fica no service, não aqui, porque
 * envolve regra de negócio (papel, troca de senha) e o hash já calculado.
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

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
    }
}
