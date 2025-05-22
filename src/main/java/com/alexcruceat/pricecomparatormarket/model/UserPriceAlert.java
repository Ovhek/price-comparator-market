package com.alexcruceat.pricecomparatormarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a price alert set by a user for a specific product.
 * The alert triggers when the product's price at a specific store (or any store if store is null)
 * drops to or below the target price.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"product", "store"})
@Entity
@Table(name = "user_price_alerts", indexes = {
        @Index(name = "idx_user_price_alert_user_product", columnList = "user_id, product_id"),
        @Index(name = "idx_user_price_alert_active_target", columnList = "is_active, target_price")
})
public class UserPriceAlert extends AbstractEntity {

    /**
     * Identifier for the user who set this alert.
     * In a real system, this would be a foreign key to a Users table.
     * For this challenge, it's a string identifier.
     */
    @NotBlank(message = "User ID cannot be blank for a price alert.")
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * The product for which the alert is set.
     */
    @NotNull(message = "Product cannot be null for a price alert.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alert_product"))
    private Product product;

    /**
     * The target price set by the user.
     * The alert triggers if the product's price is less than or equal to this value.
     */
    @NotNull(message = "Target price cannot be null.")
    @DecimalMin(value = "0.01", message = "Target price must be greater than 0.")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "target_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetPrice;

    /**
     * Optional: The specific store to monitor for this alert.
     * If null, the alert applies to the product's price in any store.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Optional relationship
    @JoinColumn(name = "store_id", foreignKey = @ForeignKey(name = "fk_alert_store"), nullable = true)
    private Store store; // If null, alert is for any store

    /**
     * Indicates whether this alert is currently active and should be checked.
     * Becomes false after being triggered and notified, or if manually deactivated.
     */
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Default to active when created

    /**
     * Timestamp indicating when the user was last notified about this alert being met.
     * Null if not yet notified or if reactivated.
     */
    @Column(name = "notified_at", nullable = true)
    private LocalDateTime notifiedAt;

    /**
     * The price at which the alert was triggered and notification was sent.
     */
    @Digits(integer = 10, fraction = 2)
    @Column(name = "triggered_price", precision = 12, scale = 2, nullable = true)
    private BigDecimal triggeredPrice;

    /**
     * The store where the triggering price was found.
     * Relevant if the alert was not store-specific or if multiple stores met the criteria.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_store_id", foreignKey = @ForeignKey(name = "fk_alert_triggered_store"), nullable = true)
    private Store triggeredStore;


    public UserPriceAlert(String userId, Product product, BigDecimal targetPrice, Store store) {
        this.userId = userId;
        this.product = product;
        this.targetPrice = targetPrice;
        this.store = store;
        this.isActive = true;
    }
}
