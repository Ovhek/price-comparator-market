package com.alexcruceat.pricecomparatormarket.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for JPA entities.
 * Provides common fields such as a unique identifier (id), creation timestamp,
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity {

    /**
     * The unique identifier for the entity.
     * Generated automatically using a sequence strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    /**
     * Timestamp indicating when the entity was created.
     * Automatically set by Hibernate upon entity creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating when the entity was last updated.
     * Automatically set by Hibernate upon entity update.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Indicates whether some other object is "equal to" this one.
     * Equality is determined based on the {@code id} field.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEntity that = (AbstractEntity) o;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    /**
     * Returns a hash code value for the object.
     * The hash code is based on the {@code id} field.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}