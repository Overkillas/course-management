package io.github.kaike.enrollment.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.github.kaike.enrollment.repository.EnrollmentRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

/**
 * Testa a matrícula a partir do curso: happy path, regra de unicidade (409) e exclusão em
 * cascata (excluir aluno ou curso remove as matrículas, sem vínculo órfão), as duas regras
 * que o planejamento §8 prioriza. Os dados são criados via HTTP e limpos ao fim.
 */
@QuarkusTest
@TestSecurity(user = "admin", roles = "admin")
class EnrollmentResourceTest {

    @Inject
    EnrollmentRepository enrollmentRepository;

    @Test
    void enrollReturns201AndStudentAppearsInCourseList() {
        int studentId = createStudent("teste.matricula@edu.unifor.br");
        int courseId = createCourse();

        enroll(courseId, studentId)
            .then()
                .statusCode(201)
                .body("studentId", is(studentId))
                .body("courseId", is(courseId));

        given()
            .when().get("/courses/" + courseId + "/students")
            .then().statusCode(200).body("id", hasItem(studentId));

        deleteStudent(studentId);
        deleteCourse(courseId);
    }

    @Test
    void enrollingSameStudentTwiceReturns409() {
        int studentId = createStudent("teste.duplicada@edu.unifor.br");
        int courseId = createCourse();

        enroll(courseId, studentId).then().statusCode(201);
        enroll(courseId, studentId)
            .then()
                .statusCode(409)
                .body("message", is("Aluno já matriculado neste curso"));

        deleteStudent(studentId);
        deleteCourse(courseId);
    }

    @Test
    void enrollWithNonExistentStudentReturns400() {
        int courseId = createCourse();

        enroll(courseId, 999999)
            .then()
                .statusCode(400)
                .body("violations.field", hasItem("studentId"));

        deleteCourse(courseId);
    }

    @Test
    void enrollOnNonExistentCourseReturns404() {
        int studentId = createStudent("teste.cursoinexistente@edu.unifor.br");

        enroll(999999, studentId).then().statusCode(404);

        deleteStudent(studentId);
    }

    @Test
    void deletingStudentRemovesEnrollmentFromCourseList() {
        int studentId = createStudent("teste.cascata.aluno@edu.unifor.br");
        int courseId = createCourse();
        enroll(courseId, studentId).then().statusCode(201);

        given()
            .when().get("/courses/" + courseId + "/students")
            .then().statusCode(200).body("id", hasItem(studentId));

        // excluir o aluno deve remover a matrícula em cascata
        deleteStudent(studentId);

        given()
            .when().get("/courses/" + courseId + "/students")
            .then().statusCode(200).body("id", not(hasItem(studentId)));

        deleteCourse(courseId);
    }

    @Test
    @Transactional
    void deletingCourseRemovesEnrollments() {
        int studentId = createStudent("teste.cascata.curso@edu.unifor.br");
        int courseId = createCourse();
        enroll(courseId, studentId).then().statusCode(201);

        // excluir o curso deve remover a matrícula em cascata
        deleteCourse(courseId);

        // nenhum vínculo órfão referenciando o curso excluído
        assertThat(enrollmentRepository.count("course.id", courseId), is(0L));

        deleteStudent(studentId);
    }

    // --- helpers ---

    private int createStudent(String email) {
        return given()
            .contentType("application/json")
            .body("{ \"name\": \"Aluno Teste\", \"email\": \"" + email + "\", \"password\": \"senhaInicial123\" }")
        .when()
            .post("/students")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    private int createCourse() {
        return given()
            .contentType("application/json")
            .body("{ \"name\": \"Curso Teste\", \"centerId\": 1, \"totalSemesters\": 8 }")
        .when()
            .post("/courses")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    private Response enroll(int courseId, int studentId) {
        return given()
            .contentType("application/json")
            .body("{ \"studentId\": " + studentId + " }")
        .when()
            .post("/courses/" + courseId + "/students");
    }

    private void deleteStudent(int id) {
        given().when().delete("/students/" + id).then().statusCode(204);
    }

    private void deleteCourse(int id) {
        given().when().delete("/courses/" + id).then().statusCode(204);
    }
}
