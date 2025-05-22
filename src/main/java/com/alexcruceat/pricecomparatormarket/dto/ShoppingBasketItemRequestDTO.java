package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a single item in a user's shopping basket request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An item in the shopping basket with its desired quantity.")
public class ShoppingBasketItemRequestDTO {

    /**
     * The unique identifier of the product desired.
     */
    @NotNull(message = "Product ID cannot be null.")
    @Schema(description = "ID of the product to add to the basket.", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long productId;

    /**
     * The desired quantity of this product.
     * Using BigDecimal to allow for fractional quantities if applicable (e.g., 0.5 kg).
     * For items sold by piece, this would typically be an integer.
     */
    @NotNull(message = "Desired quantity cannot be null.")
    @DecimalMin(value = "0.001", message = "Desired quantity must be at 0.001.")
    @Schema(description = "Desired quantity of the product. For items sold by piece, this is an integer. " +
            "For items by weight/volume, this could be fractional based on a standard unit (e.g., 1.5 for 1.5 KG).",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private BigDecimal desiredQuantity; // e.g., 2 pieces, or 1.5 for 1.5kg

}
