package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a Product.
 * Contains basic product information including its category and brand.
 */
@Data
@NoArgsConstructor
@Schema(description = "Represents a product with its basic details.")
public class ProductDTO {

    @Schema(description = "Unique identifier of the product.", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Name of the product.", example = "Lapte Zuzu 1.5% Grăsime", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Category of the product.", requiredMode = Schema.RequiredMode.REQUIRED)
    private CategoryDTO category;

    @Schema(description = "Brand of the product.", requiredMode = Schema.RequiredMode.REQUIRED)
    private BrandDTO brand;
}
