package br.com.webbudget.domain.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@ToString
@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode
public abstract class PersistentEntity implements IPersistentEntity<Long>, Serializable {

    @Id
    @Getter
    @SequenceGenerator(name = "pooled_sequence_generator", sequenceName = "pooled_sequence_generator", allocationSize = 5, initialValue = 1)
    @Column(name = "id", unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled_sequence_generator")
    private Long id;

    @Getter
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    @Getter
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @PrePersist
    protected void beforeInsert() {
        this.createdOn = LocalDateTime.now();
    }

    @PreUpdate
    protected void beforeUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    @Override
    public boolean isSaved() {
        return this.id != null && this.id != 0;
    }
}
