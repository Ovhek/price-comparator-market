package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a Product Category.
 */
@Data
@NoArgsConstructor
@Schema(description = "Represents a product category.")
public class CategoryDTO {

    @Schema(description = "Unique identifier of the category.", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Name of the category.", example = "Lactate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
