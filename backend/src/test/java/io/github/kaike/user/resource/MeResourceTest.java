package io.github.kaike.user.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.github.kaike.user.repository.UserRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Testa o /me com tokens reais: sem token dá 401, e com token o perfil retornado é o do dono do
 * token (identidade vinda do claim sub). Afirma também o nome, que não vai no token, provando que
 * o perfil veio do banco pelo sub, e não ecoado do claim.
 */
@QuarkusTest
class MeResourceTest {

    @Inject
    UserRepository userRepository;

    @Test
    void meWithoutTokenReturns401() {
        given()
            .when().get("/me")
            .then().statusCode(401);
    }

    @Test
    void meReturnsAuthenticatedUserProfile() {
        String adminToken = login("admin@unifor.br", "Ab!12345");

        int studentId =
            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    { "name": "Aluno Me", "email": "me.aluno@edu.unifor.br", "password": "senhaInicial123" }
                    """)
            .when().post("/students")
            .then().statusCode(201).extract().path("id");

        clearMustChangePassword("me.aluno@edu.unifor.br");
        String studentToken = login("me.aluno@edu.unifor.br", "senhaInicial123");

        given()
            .header("Authorization", "Bearer " + studentToken)
            .when().get("/me")
            .then()
                .statusCode(200)
                .body("id", is(studentId))
                .body("name", is("Aluno Me"))
                .body("email", is("me.aluno@edu.unifor.br"));

        given()
            .header("Authorization", "Bearer " + adminToken)
            .when().delete("/students/" + studentId)
            .then().statusCode(204);
    }

    // --- helpers ---

    /**
     * Zera a flag de troca obrigatória direto no banco, para o aluno ficar utilizável sem passar
     * pelo endpoint de troca de senha. De propósito: este teste é sobre o /me, não sobre a troca
     * de senha (que tem teste próprio em PasswordChangeResourceTest). Montar a pré-condição via
     * repositório mantém os testes desacoplados, sem que um bug na troca quebre os testes do /me.
     */
    private void clearMustChangePassword(String email) {
        QuarkusTransaction.requiringNew().run(() ->
            userRepository.update("mustChangePassword = false where email = ?1", email));
    }

    private String login(String email, String password) {
        return given()
            .contentType("application/json")
            .body("{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract().path("token");
    }
}
