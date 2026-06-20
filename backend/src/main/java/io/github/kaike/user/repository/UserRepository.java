package io.github.kaike.user.repository;

import io.github.kaike.user.domain.User;
import io.github.kaike.user.domain.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Acesso a dados de {@link User}. Padrão Repository (decisões 2.2). PK Integer.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Integer> {

    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }

    public List<User> listByRole(UserRole role) {
        return list("role", Sort.by("name"), role);
    }
}
