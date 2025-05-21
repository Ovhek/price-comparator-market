package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contains the aggregated price trend for a category or brand.")
public class AggregatedPriceTrendDTO {
    @Schema(description = "Identifier of the entity for which the trend is generated (e.g., category name or brand name).", example = "Lactate")
    private String entityIdentifier; // e.g., Category Name or Brand Name

    @Schema(description = "Type of the entity (e.g., 'CATEGORY', 'BRAND').", example = "CATEGORY")
    private String entityType;

    @Schema(description = "List of trend points over time.")
    private List<TrendPointDTO> trendPoints;

    @Schema(description = "Optional: Filter criteria applied to generate this trend.", nullable = true)
    private Map<String, String> filtersApplied;
}
