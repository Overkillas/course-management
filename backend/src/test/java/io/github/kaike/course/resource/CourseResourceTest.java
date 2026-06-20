package io.github.kaike.course.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

/**
 * Testa o CRD de curso contra a aplicação real (@QuarkusTest sobe app + MySQL efêmero com
 * as migrations e os 5 centros semeados). Como os endpoints rodam em transação própria, os
 * testes que criam dados removem o que criaram, para não interferir nos demais.
 */
@QuarkusTest
@TestSecurity(user = "admin", roles = "admin")
class CourseResourceTest {

    @Test
    void createValidCourseReturns201WithNestedCenter() {
        int id =
            given()
                .contentType("application/json")
                .body("""
                    { "name": "Ciência da Computação", "centerId": 1, "totalSemesters": 8 }
                    """)
            .when()
                .post("/courses")
            .then()
                .statusCode(201)
                .body("name", is("Ciência da Computação"))
                .body("totalSemesters", is(8))
                .body("center.code", is("CCT"))
                .extract().path("id");

        // limpa o registro criado para não poluir os demais testes
        given().when().delete("/courses/" + id).then().statusCode(204);
    }

    @Test
    void listReturnsCreatedCourseWithCenter() {
        int id =
            given()
                .contentType("application/json")
                .body("""
                    { "name": "Engenharia de Producao", "centerId": 2, "totalSemesters": 10 }
                    """)
            .when()
                .post("/courses")
            .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .when()
                .get("/courses")
            .then()
                .statusCode(200)
                .body("id", hasItem(id))
                .body("find { it.id == " + id + " }.center.code", is("CCS"));

        given().when().delete("/courses/" + id).then().statusCode(204);
    }

    @Test
    void createWithInvalidBodyReturns400WithViolations() {
        given()
            .contentType("application/json")
            .body("""
                { "name": "", "centerId": null, "totalSemesters": 0 }
                """)
        .when()
            .post("/courses")
        .then()
            .statusCode(400)
            .body("violations.field", hasItems("name", "centerId", "totalSemesters"));
    }

    @Test
    void createWithNonExistentCenterReturns400OnCenterId() {
        given()
            .contentType("application/json")
            .body("""
                { "name": "Curso Qualquer", "centerId": 9999, "totalSemesters": 4 }
                """)
        .when()
            .post("/courses")
        .then()
            .statusCode(400)
            .body("violations.field", hasItem("centerId"));
    }

    @Test
    void deleteNonExistentCourseReturns404() {
        given()
            .when()
                .delete("/courses/999999")
            .then()
                .statusCode(404);
    }
}
