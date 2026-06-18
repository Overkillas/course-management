package io.github.kaike.course.domain;

import io.github.kaike.center.domain.Center;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Curso, pertencente a um centro. A regra total_semesters > 0 e o ON DELETE RESTRICT
 * sobre o centro vivem no schema (ver db_diagram.md 3.4).
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(name = "total_semesters", nullable = false)
    private Integer totalSemesters;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public Integer getTotalSemesters() {
        return totalSemesters;
    }

    public void setTotalSemesters(Integer totalSemesters) {
        this.totalSemesters = totalSemesters;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
