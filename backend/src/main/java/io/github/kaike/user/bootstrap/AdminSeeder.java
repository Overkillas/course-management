package io.github.kaike.user.bootstrap;

import io.github.kaike.user.domain.User;
import io.github.kaike.user.domain.UserRole;
import io.github.kaike.user.repository.UserRepository;
import io.github.kaike.user.service.PasswordEncoder;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Semeia o administrador inicial no boot, a partir da config (admin.email/admin.password),
 * se ele ainda não existir. Diferente dos centros (Flyway), o admin é semeado por código
 * porque precisa de hash e de credenciais configuráveis por ambiente. O admin nasce com
 * mustChangePassword = false (a senha dele é a definida pelo operador, não uma temporária).
 */
@ApplicationScoped
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    @Inject
    public AdminSeeder(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @ConfigProperty(name = "admin.email") String adminEmail,
        @ConfigProperty(name = "admin.password") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setName("Administrador");
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.hash(adminPassword));
        admin.setRole(UserRole.ADMIN);
        admin.setMustChangePassword(false);

        userRepository.persist(admin);
    }
}
