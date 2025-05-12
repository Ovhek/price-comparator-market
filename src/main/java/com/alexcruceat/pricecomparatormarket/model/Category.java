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
 * Represents a product category (e.g., "lactate", "panificație", "băuturi").
 * Each category has a unique name.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name", columnNames = "name")
})
public class Category extends AbstractEntity {

    /**
     * The name of the product category.
     * This field is mandatory and has a maximum length of 100 characters.
     * It must be unique across all categories.
     */
    @NotBlank(message = "Category name cannot be blank.")
    @Size(max = 255, message = "Category name must be less than 255 characters.")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Constructs a new Category with the given name.
     * @param name The name of the category.
     */
    public Category(String name) {
        this.name = name;
    }
}