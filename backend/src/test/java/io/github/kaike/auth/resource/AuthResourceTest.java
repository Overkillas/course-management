package io.github.kaike.auth.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Testa o login contra a aplicação real (@QuarkusTest). O admin é semeado no startup a partir
 * da config (admin@unifor.br / Ab!12345 nos defaults de dev/test), então dá para autenticar.
 * E-mail desconhecido e senha errada devem dar a mesma resposta genérica (anti-enumeration).
 */
@QuarkusTest
class AuthResourceTest {

    @Test
    void loginWithValidAdminCredentialsReturnsToken() {
        given()
            .contentType("application/json")
            .body("""
                { "email": "admin@unifor.br", "password": "Ab!12345" }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue());
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        given()
            .contentType("application/json")
            .body("""
                { "email": "admin@unifor.br", "password": "senhaErrada" }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("message", is("Credenciais inválidas"));
    }

    @Test
    void loginWithUnknownEmailReturns401() {
        given()
            .contentType("application/json")
            .body("""
                { "email": "naoexiste@unifor.br", "password": "Ab!12345" }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("message", is("Credenciais inválidas"));
    }

    @Test
    void loginWithInvalidBodyReturns400() {
        given()
            .contentType("application/json")
            .body("""
                { "email": "", "password": "" }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400)
            .body("violations.field", hasItems("email", "password"));
    }
}
