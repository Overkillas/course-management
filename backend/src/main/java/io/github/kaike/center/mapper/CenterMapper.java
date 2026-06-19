package io.github.kaike.center.mapper;

import io.github.kaike.center.domain.Center;
import io.github.kaike.center.dtos.CenterResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Conversão manual entre {@link Center} e seus DTOs (decisões 5). Centraliza a tradução,
 * mantendo service e resource livres desse detalhe -- inclusive a de coleções (toResponseList).
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
