package io.github.kaike.user.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Testa o CRD de aluno contra a aplicação real (@QuarkusTest sobe app + MySQL efêmero). Os
 * endpoints rodam em transação própria, então os testes que criam alunos removem o que
 * criaram. Verifica também que o hash da senha nunca volta na resposta.
 */
@QuarkusTest
class StudentResourceTest {

    @Test
    void createValidStudentReturns201WithoutPasswordHash() {
        int id =
            given()
                .contentType("application/json")
                .body("""
                    { "name": "Maria Silva", "email": "teste.criar@edu.unifor.br", "password": "senhaInicial123" }
                    """)
            .when()
                .post("/students")
            .then()
                .statusCode(201)
                .body("name", is("Maria Silva"))
                .body("email", is("teste.criar@edu.unifor.br"))
                .body("mustChangePassword", is(true))
                .body("passwordHash", nullValue())
                .extract().path("id");

        // limpa o registro criado para não poluir os demais testes
        given().when().delete("/students/" + id).then().statusCode(204);
    }

    @Test
    void listReturnsCreatedStudent() {
        int id =
            given()
                .contentType("application/json")
                .body("""
                    { "name": "João Souza", "email": "teste.lista@edu.unifor.br", "password": "senhaInicial123" }
                    """)
            .when()
                .post("/students")
            .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .when()
                .get("/students")
            .then()
                .statusCode(200)
                .body("id", hasItem(id))
                .body("find { it.id == " + id + " }.email", is("teste.lista@edu.unifor.br"));

        given().when().delete("/students/" + id).then().statusCode(204);
    }

    @Test
    void createWithDuplicateEmailReturns409() {
        int id =
            given()
                .contentType("application/json")
                .body("""
                    { "name": "Ana Lima", "email": "teste.duplicado@edu.unifor.br", "password": "senhaInicial123" }
                    """)
            .when()
                .post("/students")
            .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .contentType("application/json")
            .body("""
                { "name": "Outra Ana", "email": "teste.duplicado@edu.unifor.br", "password": "senhaInicial123" }
                """)
        .when()
            .post("/students")
        .then()
            .statusCode(409)
            .body("message", is("E-mail já cadastrado"));

        given().when().delete("/students/" + id).then().statusCode(204);
    }

    @Test
    void createWithInvalidBodyReturns400WithViolations() {
        given()
            .contentType("application/json")
            .body("""
                { "name": "", "email": "not-an-email", "password": "123" }
                """)
        .when()
            .post("/students")
        .then()
            .statusCode(400)
            .body("violations.field", hasItems("name", "email", "password"));
    }

    @Test
    void deleteNonExistentStudentReturns404() {
        given()
            .when()
                .delete("/students/999999")
            .then()
                .statusCode(404);
    }
}
