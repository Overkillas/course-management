package io.github.kaike.user.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

/**
 * Testa a troca de senha no primeiro acesso e a trava do filtro: o aluno com a flag ligada é
 * bloqueado (403) em tudo, exceto na troca; depois de trocar e relogar, passa. Cobre também a
 * exigência de senha forte.
 */
@QuarkusTest
class PasswordChangeResourceTest {

    @Test
    void changePasswordWithoutTokenReturns401() {
        given()
            .contentType("application/json")
            .body("{ \"newPassword\": \"NovaSenha!123\" }")
        .when().post("/me/password")
        .then().statusCode(401);
    }

    @Test
    void mustChangeUserIsBlockedUntilPasswordIsChanged() {
        String adminToken = login("admin@unifor.br", "Ab!12345");
        int studentId = createStudent(adminToken, "trava@edu.unifor.br");

        String studentToken = login("trava@edu.unifor.br", "senhaInicial123");

        // bloqueado em /me enquanto a senha nao for trocada
        given()
            .header("Authorization", "Bearer " + studentToken)
            .when().get("/me")
            .then().statusCode(403);

        // a troca e a unica acao liberada
        changePassword(studentToken, "NovaSenha!123").then().statusCode(204);

        // com o token novo (sem a flag), /me passa
        String fresh = login("trava@edu.unifor.br", "NovaSenha!123");
        given()
            .header("Authorization", "Bearer " + fresh)
            .when().get("/me")
            .then().statusCode(200);

        deleteStudent(adminToken, studentId);
    }

    @Test
    void weakNewPasswordReturns400() {
        String adminToken = login("admin@unifor.br", "Ab!12345");
        int studentId = createStudent(adminToken, "fraca@edu.unifor.br");
        String studentToken = login("fraca@edu.unifor.br", "senhaInicial123");

        changePassword(studentToken, "fraca")
            .then().statusCode(400).body("violations.field", hasItem("newPassword"));

        deleteStudent(adminToken, studentId);
    }

    @Test
    void newPasswordWithoutSpecialCharReturns400() {
        String adminToken = login("admin@unifor.br", "Ab!12345");
        int studentId = createStudent(adminToken, "semespecial@edu.unifor.br");
        String studentToken = login("semespecial@edu.unifor.br", "senhaInicial123");

        // 8+ caracteres, com letra e dígito, mas sem caractere especial (deve falhar por isso)
        changePassword(studentToken, "abcdefgh1")
            .then().statusCode(400).body("violations.field", hasItem("newPassword"));

        deleteStudent(adminToken, studentId);
    }

    // --- helpers ---

    private Response changePassword(String token, String newPassword) {
        return given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("{ \"newPassword\": \"" + newPassword + "\" }")
        .when().post("/me/password");
    }

    private String login(String email, String password) {
        return given()
            .contentType("application/json")
            .body("{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }")
        .when().post("/auth/login")
        .then().statusCode(200).extract().path("token");
    }

    private int createStudent(String adminToken, String email) {
        return given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .body("{ \"name\": \"Aluno Trava\", \"email\": \"" + email + "\", \"password\": \"senhaInicial123\" }")
        .when().post("/students")
        .then().statusCode(201).extract().path("id");
    }

    private void deleteStudent(String adminToken, int id) {
        given().header("Authorization", "Bearer " + adminToken)
            .when().delete("/students/" + id).then().statusCode(204);
    }
}
