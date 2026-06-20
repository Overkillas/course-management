package io.github.kaike.auth.service;

import io.github.kaike.auth.dtos.LoginRequest;
import io.github.kaike.auth.dtos.LoginResponse;
import io.github.kaike.shared.exceptions.AuthenticationException;
import io.github.kaike.user.domain.User;
import io.github.kaike.user.repository.UserRepository;
import io.github.kaike.user.service.PasswordEncoder;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Autenticação: valida as credenciais e emite o JWT. O papel vai no claim padrão "groups"
 * (que o @RolesAllowed lê) e o mustChangePassword vai como claim próprio. Assinatura HS256
 * com o segredo da config. Depende de user (UserRepository, PasswordEncoder), numa
 * dependência acíclica auth -> user.
 */
@ApplicationScoped
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String secret;
    private final long durationSeconds;
    private final String issuer;

    @Inject
    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @ConfigProperty(name = "jwt.secret") String secret,
        @ConfigProperty(name = "jwt.duration") long durationSeconds,
        @ConfigProperty(name = "mp.jwt.verify.issuer") String issuer
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.secret = secret;
        this.durationSeconds = durationSeconds;
        this.issuer = issuer;
    }

    public LoginResponse login(LoginRequest request) {
        // Mesma exceção genérica para e-mail inexistente e senha errada (anti-enumeration).
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Credenciais inválidas");
        }

        String token = Jwt.issuer(issuer)
            .subject(String.valueOf(user.getId()))
            .upn(user.getEmail())
            .groups(Set.of(user.getRole().name().toLowerCase()))
            .claim("mustChangePassword", user.isMustChangePassword())
            .expiresIn(Duration.ofSeconds(durationSeconds))
            .signWithSecret(secret);

        return new LoginResponse(token);
    }
}
