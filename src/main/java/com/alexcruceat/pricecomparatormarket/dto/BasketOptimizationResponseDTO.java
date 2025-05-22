package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO representing the result of a shopping basket optimization.
 * Includes lists of items per store and overall cost summaries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "The result of the shopping basket optimization, potentially splitting items across stores.")
public class BasketOptimizationResponseDTO {

    @Schema(description = "Identifier of the user for whom this basket was optimized.", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @Schema(description = "List of optimized shopping lists, one per store involved in the optimal solution.")
    private List<OptimizedStoreShoppingListDTO> storeShoppingLists;

    @Schema(description = "The overall minimum total cost to acquire all items in the basket.", example = "78.90")
    private BigDecimal overallMinimumCost;

    @Schema(description = "Total number of distinct products in the original basket.", example = "5")
    private int totalDistinctProductsInBasket;

    @Schema(description = "Number of distinct products that could not be found or priced.", example = "0")
    private int unfulfillableProductCount;

    @Schema(description = "List of product IDs from the original basket that could not be found or priced.", nullable = true)
    private List<Long> unfulfillableProductIds;

    // Information about savings if compared to a baseline (e.g., buying all from one store)
    @Schema(description = "Potential savings compared to buying all items from a single (most expensive common) store.", example = "12.50", nullable = true)
    private BigDecimal potentialSavings;
}
