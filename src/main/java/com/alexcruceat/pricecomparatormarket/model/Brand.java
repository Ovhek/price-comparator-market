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
 * Represents a product brand (e.g., "Zuzu", "Pilos", "Barilla").
 * Each brand has a unique name.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "brands", uniqueConstraints = {
        @UniqueConstraint(name = "uk_brand_name", columnNames = "name")
})
public class Brand extends AbstractEntity {

    /**
     * The name of the product brand.
     * This field is mandatory and has a maximum length of 255 characters.
     * It must be unique across all brands.
     */
    @NotBlank(message = "Brand name cannot be blank.")
    @Size(max = 255, message = "Brand name must be less than 255 characters.")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Constructs a new Brand with the given name.
     * @param name The name of the brand.
     */
    public Brand(String name) {
        this.name = name;
    }
}
