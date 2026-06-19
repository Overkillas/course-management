package io.github.kaike.center.resource;

import io.github.kaike.center.dtos.CenterResponse;
import io.github.kaike.center.service.CenterService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposição HTTP do centro. Só leitura: lista os centros para o front consumir.
 * Ver decisões 8.1.
 */
@Path("/centers")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Centros", description = "Dados de referência dos centros acadêmicos")
public class CenterResource {

    private final CenterService service;

    @Inject
    public CenterResource(CenterService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "Lista todos os centros")
    public List<CenterResponse> list() {
        return service.listAll();
    }
}
