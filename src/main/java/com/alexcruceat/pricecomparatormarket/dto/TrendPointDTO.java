package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single data point representing a price or an aggregated value on a specific date.")
@Builder
public class TrendPointDTO {
    @Schema(description = "The date of this point.", example = "2024-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Schema(description = "The price value. For individual products, this is the actual price. For aggregated trends, this is the calculated average/metric.", example = "9.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal value;

    @Schema(description = "Description of the value's unit (e.g., 'RON', 'per KG', 'per L').", example = "RON", nullable = true)
    private String valueUnitDescription;

    @Schema(description = "Name of the store where this price was recorded (only for individual product history if store-specific).", example = "Lidl", nullable = true)
    private String storeName;
}
