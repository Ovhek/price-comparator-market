package com.alexcruceat.pricecomparatormarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Represents a discount applied to a product at a specific store for a given period.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "discounts", uniqueConstraints = {
        // One discount record per product-store-start_date.
        @UniqueConstraint(name = "uk_discount_product_store_from_date",
                columnNames = {"product_id", "store_id", "from_date"})
})
public class Discount extends AbstractEntity {

    /**
     * The product to which this discount applies.
     */
    @NotNull(message = "Product for discount cannot be null.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_discount_product"))
    private Product product;

    /**
     * The store offering this discount.
     * This information is not directly in the discount CSV sample but is derived from the filename.
     */
    @NotNull(message = "Store for discount cannot be null.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_discount_store"))
    private Store store;

    /**
     * The percentage of the discount (e.g., 10 for 10%, 25 for 25%).
     * Must be between 0 and 100 (exclusive of 0, inclusive of 100 for free items).
     */
    @NotNull(message = "Percentage of discount cannot be null.")
    @Min(value = 1, message = "Discount percentage must be at least 1.")
    @Max(value = 100, message = "Discount percentage must be at most 100.")
    @Column(name = "percentage", nullable = false)
    private Integer percentage;

    /**
     * The start date from which the discount is valid.
     */
    @NotNull(message = "Discount from_date cannot be null.")
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    /**
     * The end date until which the discount is valid (inclusive).
     */
    @NotNull(message = "Discount to_date cannot be null.")
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    /**
     * The date this discount information was recorded or became known.
     */
    @NotNull(message = "Discount recorded_at_date cannot be null.")
    @Column(name = "recorded_at_date", nullable = false)
    private LocalDate recordedAtDate;


    /**
     * Constructs a new Discount.
     *
     * @param product        The product being discounted.
     * @param store          The store offering the discount.
     * @param percentage     The discount percentage.
     * @param fromDate       The start date of the discount.
     * @param toDate         The end date of the discount.
     * @param recordedAtDate The date this discount was recorded (from CSV filename).
     */
    public Discount(Product product, Store store, Integer percentage, LocalDate fromDate, LocalDate toDate, LocalDate recordedAtDate) {
        this.product = product;
        this.store = store;
        this.percentage = percentage;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.recordedAtDate = recordedAtDate;
    }
}
