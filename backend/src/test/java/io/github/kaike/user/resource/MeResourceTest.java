package io.github.kaike.user.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Testa o /me com tokens reais: sem token dá 401, e com token o perfil retornado é o do dono
 * do token (identidade vinda do claim sub, nunca do path ou do corpo).
 */
@QuarkusTest
class MeResourceTest {

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
