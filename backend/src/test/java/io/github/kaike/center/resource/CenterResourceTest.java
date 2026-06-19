package io.github.kaike.center.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Testa o endpoint de leitura de centros contra a aplicacao de verdade (a anotacao
 * @QuarkusTest sobe o app + um MySQL efemero com as migrations aplicadas, entao os 5
 * centros semeados na V2 existem). Estabelece o padrao @QuarkusTest + RestAssured.
 */
@QuarkusTest
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
