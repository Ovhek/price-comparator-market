package com.alexcruceat.pricecomparatormarket.dto;

import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a product to be bought from a specific store as part of an optimized shopping list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Details of a product within an optimized shopping list for a specific store.")
public class OptimizedShoppingItemDTO {

    @Schema(description = "ID of the product.", example = "101")
    private Long productId;

    @Schema(description = "Name of the product.", example = "Lapte UHT Zuzu")
    private String productName;

    @Schema(description = "Quantity of this product to buy from this store.", example = "2")
    private BigDecimal quantityToBuy; // Could be different from desired if split or package size differs

    @Schema(description = "The unit of the quantityToBuy (e.g., BUCATA, KG).", example = "BUCATA")
    private String unit;

    @Schema(description = "Price per unit of quantityToBuy at this store.", example = "5.99")
    private BigDecimal pricePerUnitAtStore; // Price of one piece, or one KG etc.

    @Schema(description = "Total cost for this item (quantityToBuy * pricePerUnitAtStore) at this store.", example = "11.98")
    private BigDecimal totalItemCostAtStore;

    @Schema(description = "Original package quantity of the item as sold in store.", example = "1")
    private BigDecimal storePackageQuantity;

    @Schema(description = "Original package unit of the item as sold in store.", example = "L")
    private String storePackageUnit;

    @Schema(description = "Indicates if this item has an active discount applied at this store.", example = "true")
    private boolean discounted;

    @Schema(description = "Discount percentage if applicable.", example = "10", nullable = true)
    private Integer discountPercentage;
}
