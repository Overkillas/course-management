package io.github.kaike.center.repository;

import io.github.kaike.center.domain.Center;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Acesso a dados de {@link Center}. Padrão Repository (decisões 2.2): a persistência vive
 * aqui, fora da entidade. PanacheRepositoryBase com Integer porque a PK é Integer, não Long.
 */
@ApplicationScoped
public class CenterRepository implements PanacheRepositoryBase<Center, Integer> {
}
