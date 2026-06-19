package io.github.kaike.center.service;

import io.github.kaike.center.dtos.CenterResponse;
import io.github.kaike.center.mapper.CenterMapper;
import io.github.kaike.center.repository.CenterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Regra de negócio do centro. No escopo atual há apenas leitura (centro é dado de
 * referência, sem CRUD -- ver decisões 8.1).
 */
@ApplicationScoped
public class CenterService {

    private final CenterRepository repository;
    private final CenterMapper mapper;

    @Inject
    public CenterService(CenterRepository repository, CenterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CenterResponse> listAll() {
        return mapper.toResponseList(repository.listAll());
    }
}
