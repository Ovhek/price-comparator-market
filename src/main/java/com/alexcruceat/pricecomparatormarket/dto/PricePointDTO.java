package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing a single price point in a time series.
 * Used for constructing price history graphs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single data point representing a price on a specific date, potentially for a specific store.")
public class PricePointDTO {

    @Schema(description = "The date of this price point.", example = "2024-01-15")
    private LocalDate date;

    @Schema(description = "The price on this date.", example = "9.99")
    private BigDecimal price;

    @Schema(description = "Name of the store where this price was recorded (optional, context-dependent).", example = "Lidl", nullable = true)
    private String storeName;

}
