package io.github.kaike.center.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

/**
 * Testa o endpoint de leitura de centros contra a aplicação de verdade (a anotação
 * @QuarkusTest sobe o app + um MySQL efêmero com as migrations aplicadas, então os 5
 * centros semeados na V2 existem). Estabelece o padrão @QuarkusTest + RestAssured.
 */
@QuarkusTest
@TestSecurity(user = "admin", roles = "admin")
class CenterResourceTest {

    @Test
    void listShouldReturnAllSeededCenters() {
        given()
            .when()
                .get("/centers")
            .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", is(5))
                .body("code", containsInAnyOrder("CCT", "CCS", "CCJ", "CCA", "CCH"));
    }
}
