package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a Product Brand.
 */
@Data
@NoArgsConstructor
@Schema(description = "Represents a product brand.")
public class BrandDTO {

    @Schema(description = "Unique identifier of the brand.", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Name of the brand.", example = "Zuzu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
