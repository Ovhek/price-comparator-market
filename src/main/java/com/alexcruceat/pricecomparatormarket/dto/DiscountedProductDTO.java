package com.alexcruceat.pricecomparatormarket.dto;

import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a product that is currently discounted.
 * Includes product details, store information, discount percentage,
 * original price, and calculated discounted price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Represents a product with its current discount information.")
public class DiscountedProductDTO {

    @Schema(description = "Basic details of the discounted product.")
    private ProductDTO product;

    @Schema(description = "Details of the store offering the discount.")
    private StoreDTO store;

    @Schema(description = "The discount percentage being applied.", example = "25")
    private Integer discountPercentage;

    @Schema(description = "The original price of the product before discount at this store for the discounted package.", example = "100.00")
    private BigDecimal originalPrice;

    @Schema(description = "The calculated price after applying the discount.", example = "75.00")
    private BigDecimal discountedPrice;

    @Schema(description = "The package quantity to which this discount and prices apply.", example = "1")
    private BigDecimal packageQuantity;

    @Schema(description = "The package unit for this discount.", example = "KG")
    private UnitOfMeasure packageUnit;

    @Schema(description = "The date until which the discount is valid.", example = "2024-12-31")
    private LocalDate discountEndDate;
}
