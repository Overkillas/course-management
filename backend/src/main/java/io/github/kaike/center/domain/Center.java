package io.github.kaike.center.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Centro academico. Dado de referencia (semeado via migration), referenciado pelos cursos.
 *
 * Read-only no escopo atual: nao ha CRUD de centro, entao a entidade nao expoe setters
 * (nao haveria quem os usasse). O updatedAt, por outro lado, existe de proposito: o centro
 * e a entidade planejada para ganhar edicao/CRUD no futuro (db_diagram.md 7.1), e ja deixar
 * a coluna no schema torna essa evolucao aditiva -- nao sera preciso uma nova migration so
 * para adicionar o campo quando o update existir.
 */
@Entity
@Table(name = "centers")
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 10, unique = true)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
