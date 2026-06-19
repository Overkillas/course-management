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
 * Centro acadêmico. Dado de referência (semeado via migration), referenciado pelos cursos.
 *
 * Read-only no escopo atual: não há CRUD de centro, então a entidade não expõe setters
 * (não haveria quem os usasse). O updatedAt, por outro lado, existe de propósito: o centro
 * é a entidade planejada para ganhar edição/CRUD no futuro (db_diagram.md 7.1), e já deixar
 * a coluna no schema torna essa evolução aditiva -- não será preciso uma nova migration só
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
