package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a user's price alert.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Details of a configured price alert.")
public class PriceAlertDTO {

    @Schema(description = "Unique ID of the price alert.", example = "1")
    private Long id;

    @Schema(description = "User who set the alert.", example = "user77")
    private String userId;

    @Schema(description = "Details of the product being monitored.")
    private ProductDTO product;

    @Schema(description = "Target price for the alert.", example = "49.99")
    private BigDecimal targetPrice;

    @Schema(description = "Details of the specific store being monitored, if any.", nullable = true)
    private StoreDTO store;

    @Schema(description = "Whether the alert is currently active.", example = "true")
    private Boolean isActive;

    @Schema(description = "When the alert was created.", example = "2024-01-10T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the user was last notified for this alert (if triggered).", example = "2024-01-15T14:30:00", nullable = true)
    private LocalDateTime notifiedAt;

    @Schema(description = "The price at which the alert was triggered.", example = "48.50", nullable = true)
    private BigDecimal triggeredPrice;

    @Schema(description = "Name of the store where the triggering price was found.", example = "Lidl", nullable = true)
    private String triggeredStoreName;
}