package com.alexcruceat.pricecomparatormarket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a supermarket chain or store where products are sold.
 * Each store has a unique name.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "stores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_name", columnNames = "name")
})
public class Store extends AbstractEntity {

    /**
     * The name of the store (e.g., "Lidl", "Kaufland").
     * This field is mandatory and has a maximum length of 100 characters.
     * It must be unique across all stores.
     */
    @NotBlank(message = "Store name cannot be blank.")
    @Size(max = 100, message = "Store name must be less than 100 characters.")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Constructs a new Store with the given name.
     * @param name The name of the store.
     */
    public Store(String name) {
        this.name = name;
    }
}