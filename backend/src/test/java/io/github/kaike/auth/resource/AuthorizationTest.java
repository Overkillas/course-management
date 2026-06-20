package io.github.kaike.auth.resource;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Testa a autorização com tokens reais (sem @TestSecurity), exercitando o caminho completo
 * do JWT: verificação da assinatura e checagem de papel. Sem token dá 401, papel errado dá
 * 403 e admin passa com 200.
 */
@QuarkusTest
class AuthorizationTest {

    @Test
    void protectedEndpointWithoutTokenReturns401() {
        given()
            .when().get("/courses")
            .then().statusCode(401);
    }

    @Test
    void protectedEndpointWithAdminTokenReturns200() {
        String token = login("admin@unifor.br", "Ab!12345");

        given()
            .header("Authorization", "Bearer " + token)
            .when().get("/courses")
            .then().statusCode(200);
    }

    @Test
    void protectedEndpointWithStudentTokenReturns403() {
        String adminToken = login("admin@unifor.br", "Ab!12345");

        int studentId =
            given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("""
                    { "name": "Aluno Authz", "email": "authz.aluno@edu.unifor.br", "password": "senhaInicial123" }
                    """)
            .when().post("/students")
            .then().statusCode(201).extract().path("id");

        String studentToken = login("authz.aluno@edu.unifor.br", "senhaInicial123");

        given()
            .header("Authorization", "Bearer " + studentToken)
            .when().get("/courses")
            .then().statusCode(403);

        given()
            .header("Authorization", "Bearer " + adminToken)
            .when().delete("/students/" + studentId)
            .then().statusCode(204);
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
