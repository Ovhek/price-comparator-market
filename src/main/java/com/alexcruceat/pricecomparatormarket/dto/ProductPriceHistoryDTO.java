package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the price history for a product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contains details of a product and its price history.")
public class ProductPriceHistoryDTO {

    @Schema(description = "Details of the product.")
    private ProductDTO product;

    @Schema(description = "List of price points over time.")
    private List<PricePointDTO> pricePoints;

}