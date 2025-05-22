package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing an optimized shopping list for a single store.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A list of items to be purchased from a specific store, with the total cost for that store.")
public class OptimizedStoreShoppingListDTO {

    @Schema(description = "ID of the store.", example = "1")
    private Long storeId;

    @Schema(description = "Name of the store.", example = "Lidl")
    private String storeName;

    @Schema(description = "List of items to buy from this store.")
    private List<OptimizedShoppingItemDTO> itemsToBuy;

    @Schema(description = "Total cost of all items in this list for this store.", example = "45.50")
    private BigDecimal totalCostForStore;

    @Schema(description = "Number of distinct products from the original basket assigned to this store.", example = "3")
    private int numberOfProductsFromBasket;
}
