package com.alexcruceat.pricecomparatormarket.dto;

import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import com.alexcruceat.pricecomparatormarket.util.UnitConverterUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing a product with its value-per-unit calculation.
 * This helps users compare products of different sizes by a common metric (e.g., price per KG/Litre).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Represents a product along with its calculated value per standard unit.")
public class ProductValueDTO {

    @Schema(description = "Basic details of the product.")
    private ProductDTO product;

    @Schema(description = "Details of the store where this price and value calculation applies.")
    private StoreDTO store;

    @Schema(description = "The actual selling price of the product for the given package.", example = "9.99")
    private BigDecimal currentPrice;

    @Schema(description = "The currency of the current price.", example = "RON")
    private String currency;

    @Schema(description = "The quantity in the product's package.", example = "0.75")
    private BigDecimal packageQuantity;

    @Schema(description = "The unit of measure for the product's package.", example = "L")
    private UnitOfMeasure packageUnit;

    // Calculated Value Per Standard Unit
    @Schema(description = "The calculated price per standard unit (e.g., per KG or per Litre).", example = "13.32", nullable = true)
    private BigDecimal pricePerStandardUnit;

    @Schema(description = "The standard unit for the calculated price (KG or L).", example = "L", nullable = true)
    private UnitConverterUtil.BaseUnit standardUnit; // KG or L

    @Schema(description = "Indicates if this product could be normalized for value comparison.", example = "true")
    private boolean normalizable;
}