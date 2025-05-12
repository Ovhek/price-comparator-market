package com.alexcruceat.pricecomparatormarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a unique product available in the market.
 * A product is typically identified by its name and brand.
 * It belongs to a category and can have multiple price entries from different stores
 * and associated discounts.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"priceEntries", "discounts"})
@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_name_brand", columnNames = {"name", "brand_id"})
})
public class Product extends AbstractEntity {

    /**
     * The name of the product (e.g., "lapte zuzu", "spaghetti nr.5").
     * This field is mandatory and has a maximum length of 255 characters.
     */
    @NotBlank(message = "Product name cannot be blank.")
    @Size(max = 255, message = "Product name must be less than 255 characters.")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The category to which this product belongs.
     * This is a many-to-one relationship with the {@link Category} entity.
     * This field is mandatory.
     */
    @NotNull(message = "Product category cannot be null.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    /**
     * The brand of this product.
     * This is a many-to-one relationship with the {@link Brand} entity.
     * This field is mandatory.
     */
    @NotNull(message = "Product brand cannot be null.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_brand"))
    private Brand brand;


    /**
     * List of price entries for this product from various stores over time.
     * This is a one-to-many relationship, mapped by the {@code product} field in the {@link PriceEntry} entity.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PriceEntry> priceEntries = new ArrayList<>();

    /**
     * List of discounts available for this product.
     * This is a one-to-many relationship, mapped by the {@code product} field in the {@link Discount} entity.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Discount> discounts = new ArrayList<>();


    /**
     * Constructs a new Product.
     *
     * @param name     The name of the product.
     * @param category The category of the product.
     * @param brand    The brand of the product.
     */
    public Product(String name, Category category, Brand brand) {
        this.name = name;
        this.category = category;
        this.brand = brand;
    }

    // Custom equals and hashCode because of relationships and potential lazy loading issues
    // Base equality on natural keys if ID is null (new entity), otherwise ID.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false; // Checks ID from AbstractEntity
        Product product = (Product) o;
        if (getId() == null && product.getId() == null) {
            return Objects.equals(name, product.name) &&
                    Objects.equals(brand, product.brand); // Relies on Brand's equals
        }
        return true; // If super.equals was true and IDs are not both null
    }

    @Override
    public int hashCode() {
        // If ID is null (new entity), hash by business key.
        if (getId() == null) {
            return Objects.hash(name, brand);
        }
        return super.hashCode();
    }
}
