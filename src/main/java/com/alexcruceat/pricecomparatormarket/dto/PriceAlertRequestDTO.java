package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a new price alert.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new price alert for a product.")
public class PriceAlertRequestDTO {

    @NotBlank(message = "User ID cannot be blank.")
    @Schema(description = "Identifier of the user setting the alert.", requiredMode = Schema.RequiredMode.REQUIRED, example = "user123")
    private String userId;

    @NotNull(message = "Product ID cannot be null.")
    @Schema(description = "ID of the product to monitor.", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @NotNull(message = "Target price cannot be null.")
    @DecimalMin(value = "0.01", message = "Target price must be greater than 0.00.")
    @Digits(integer = 10, fraction = 2, message = "Target price format is invalid.")
    @Schema(description = "The price at or below which the user wants to be alerted.", requiredMode = Schema.RequiredMode.REQUIRED, example = "49.99")
    private BigDecimal targetPrice;

    @Schema(description = "Optional ID of a specific store to monitor. If omitted, any store will be monitored.", example = "5", nullable = true)
    private Long storeId;
}
