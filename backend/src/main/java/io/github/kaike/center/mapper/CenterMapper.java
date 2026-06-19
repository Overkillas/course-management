package io.github.kaike.center.mapper;

import io.github.kaike.center.domain.Center;
import io.github.kaike.center.dtos.CenterResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Conversao manual entre {@link Center} e seus DTOs (decisoes 5). Centraliza a traducao,
 * mantendo service e resource livres desse detalhe -- inclusive a de colecoes (toResponseList).
 */
@ApplicationScoped
public class CenterMapper {

    public CenterResponse toResponse(Center center) {
        return new CenterResponse(center.getId(), center.getCode());
    }

    public List<CenterResponse> toResponseList(List<Center> centers) {
        return centers.stream().map(this::toResponse).toList();
    }
}
