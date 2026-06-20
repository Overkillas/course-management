package io.github.kaike.enrollment.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Testa o /me/courses com tokens reais: sem token dá 401, e o aluno autenticado vê apenas os
 * próprios cursos (identidade vinda do claim sub). Os dados são criados via HTTP pelo admin e
 * limpos ao fim.
 */
@QuarkusTest
class MyCoursesResourceTest {

    @Test
    void myCoursesWithoutTokenReturns401() {
        given()
            .when().get("/me/courses")
            .then().statusCode(401);
    }

    @Test
    void myCoursesReturnsTheStudentsEnrolledCourses() {
        String adminToken = login("admin@unifor.br", "Ab!12345");

        int studentId = createStudent(adminToken, "me.courses@edu.unifor.br");
        int courseId = createCourse(adminToken);

        String studentToken = login("me.courses@edu.unifor.br", "senhaInicial123");

        // sem matrícula: lista vazia
        given()
            .header("Authorization", "Bearer " + studentToken)
            .when().get("/me/courses")
            .then().statusCode(200).body("size()", is(0));

        enroll(adminToken, courseId, studentId);

        // com matrícula: o curso aparece
        given()
            .header("Authorization", "Bearer " + studentToken)
            .when().get("/me/courses")
            .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(courseId));

        deleteStudent(adminToken, studentId);
        deleteCourse(adminToken, courseId);
    }

    // --- helpers ---

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

    private int createStudent(String adminToken, String email) {
        return given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .body("{ \"name\": \"Aluno MeCourses\", \"email\": \"" + email + "\", \"password\": \"senhaInicial123\" }")
        .when()
            .post("/students")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    private int createCourse(String adminToken) {
        return given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .body("{ \"name\": \"Curso MeCourses\", \"centerId\": 1, \"totalSemesters\": 8 }")
        .when()
            .post("/courses")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    private void enroll(String adminToken, int courseId, int studentId) {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .body("{ \"studentId\": " + studentId + " }")
        .when()
            .post("/courses/" + courseId + "/students")
        .then()
            .statusCode(201);
    }

    private void deleteStudent(String adminToken, int id) {
        given().header("Authorization", "Bearer " + adminToken)
            .when().delete("/students/" + id).then().statusCode(204);
    }

    private void deleteCourse(String adminToken, int id) {
        given().header("Authorization", "Bearer " + adminToken)
            .when().delete("/courses/" + id).then().statusCode(204);
    }
}
