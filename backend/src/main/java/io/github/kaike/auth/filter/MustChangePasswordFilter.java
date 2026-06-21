package io.github.kaike.auth.filter;

import io.github.kaike.shared.exceptions.PasswordChangeRequiredException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.security.Principal;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Trava de primeiro acesso: enquanto o usuário autenticado tiver o claim mustChangePassword
 * ligado, bloqueia toda requisição com 403, exceto a própria troca de senha (POST /me/password).
 *
 * Lê apenas o claim do token, sem ir ao banco. Depois de trocar a senha (que zera a flag no
 * banco), o usuário reloga e o token novo, já sem a flag, libera o acesso normal. O token antigo
 * continua barrado aqui e expira sozinho.
 *
 * Usa a SecurityIdentity para agir só sobre quem tem JWT de verdade: requisições anônimas e
 * identidades sem JWT (por exemplo, os testes funcionais com @TestSecurity) passam direto, sem que
 * a autenticação/autorização padrão seja afetada.
 */
@Provider
public class MustChangePasswordFilter implements ContainerRequestFilter {

    private final SecurityIdentity identity;

    @Inject
    public MustChangePasswordFilter(SecurityIdentity identity) {
        this.identity = identity;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (identity.isAnonymous()) {
            return;
        }
        Principal principal = identity.getPrincipal();
        if (!(principal instanceof JsonWebToken jwt)) {
            return;
        }
        if (!isMustChange(jwt) || isPasswordChange(requestContext)) {
            return;
        }
        throw new PasswordChangeRequiredException();
    }

    /**
     * O SmallRye faz parse do token em tipos JSON-P, então o claim booleano chega como JsonValue
     * (TRUE/FALSE). Leio como Object para não cair na inferência de generics do getClaim (que
     * causaria um cast indevido) e comparo o tipo do JsonValue.
     */
    private boolean isMustChange(JsonWebToken jwt) {
        Object claim = jwt.getClaim("mustChangePassword");
        return claim instanceof JsonValue json && json.getValueType() == JsonValue.ValueType.TRUE;
    }

    private boolean isPasswordChange(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return "POST".equals(ctx.getMethod()) && "me/password".equals(path);
    }
}
