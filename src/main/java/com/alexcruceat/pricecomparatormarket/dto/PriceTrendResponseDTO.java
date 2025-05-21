package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a price trend or history.
 * Can be used for an individual product's history or an aggregated trend
 * for a category or brand.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Contains details about the queried entity (product, category, or brand) " +
        "and its associated list of price/trend points.")
public class PriceTrendResponseDTO {

    @Schema(description = "Type of the entity for which the trend is generated (e.g., 'PRODUCT', 'CATEGORY', 'BRAND').",
            example = "PRODUCT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entityType; // PRODUCT, CATEGORY, BRAND

    @Schema(description = "ID of the primary entity (productId, categoryId, or brandId).",
            example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long entityId;

    @Schema(description = "Name or identifier of the entity (e.g., product name, category name, brand name).",
            example = "Lapte UHT Zuzu 1.5%", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entityName;

    /**
     * For PRODUCT entityType, this can contain more detailed product information.
     * For CATEGORY/BRAND, this might be null or contain minimal info about the category/brand itself.
     */
    @Schema(description = "Optional: Detailed DTO of the primary entity if applicable (e.g., ProductDTO for entityType PRODUCT).",
            nullable = true)
    private Object entityDetails; // Could be ProductDTO, CategoryDTO, BrandDTO, or null

    @Schema(description = "List of price or trend points over time. " +
            "For individual products, these are actual prices. " +
            "For categories/brands, these are aggregated (e.g., average) prices.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TrendPointDTO> trendPoints;

    @Schema(description = "Filters applied to generate this trend/history (e.g., storeId, dateRange, baseUnit for aggregation).",
            nullable = true)
    private Map<String, String> filtersApplied;
}
