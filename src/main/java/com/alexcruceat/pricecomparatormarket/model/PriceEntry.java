package com.alexcruceat.pricecomparatormarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents the price of a specific product at a specific store on a particular date.
 * It also includes details about the package size and unit of measure for that price.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "price_entries", uniqueConstraints = {
        // One price entry per store per date
        @UniqueConstraint(name = "uk_price_entry_product_store_date",
                columnNames = {"product_id", "store_id", "entry_date"})
})
public class PriceEntry extends AbstractEntity {

    /**
     * The product associated with this price entry.
     */
    @NotNull(message = "Product for price entry cannot be null.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_price_entry_product"))
    private Product product;

    /**
     * The store where this price was recorded.
     */
    @NotNull(message = "Store for price entry cannot be null.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_price_entry_store"))
    private Store store;

    /**
     * The specific product ID used by the store (from the CSV).
     */
    @Column(name = "store_product_id", length = 50)
    private String storeProductId;

    /**
     * The price of the product.
     * Must be a positive value.
     */
    @NotNull(message = "Price cannot be null.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0.")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid (max 10 integer, 2 fraction digits).")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * The currency of the price.
     */
    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * The quantity of the product in the package for this price (e.g., 0.5, 1, 500).
     */
    @NotNull(message = "Package quantity cannot be null.")
    @Column(name = "package_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal packageQuantity;

    /**
     * The unit of measure for the package quantity.
     * Stored as an enum {@link UnitOfMeasure}.
     */
    @NotNull(message = "Package unit cannot be null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "package_unit", nullable = false, length = 20)
    private UnitOfMeasure packageUnit;

    /**
     * The date for which this price entry is valid.
     * Date in the CSV filename.
     */
    @NotNull(message = "Entry date cannot be null.")
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;


    /**
     * Constructs a new PriceEntry.
     *
     * @param product         The product.
     * @param store           The store.
     * @param storeProductId  The store's specific ID for the product.
     * @param price           The price.
     * @param currency        The currency.
     * @param packageQuantity The package quantity.
     * @param packageUnit     The package unit of measure.
     * @param entryDate       The date this price is valid for.
     */
    public PriceEntry(Product product, Store store, String storeProductId, BigDecimal price, String currency,
                      BigDecimal packageQuantity, UnitOfMeasure packageUnit, LocalDate entryDate) {
        this.product = product;
        this.store = store;
        this.storeProductId = storeProductId;
        this.price = price;
        this.currency = currency;
        this.packageQuantity = packageQuantity;
        this.packageUnit = packageUnit;
        this.entryDate = entryDate;
    }
}