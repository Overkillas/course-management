package io.github.kaike.center.service;

import io.github.kaike.center.dtos.CenterResponse;
import io.github.kaike.center.mapper.CenterMapper;
import io.github.kaike.center.repository.CenterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Regra de negocio do centro. No escopo atual ha apenas leitura (centro e dado de
 * referencia, sem CRUD -- ver decisoes 8.1).
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
