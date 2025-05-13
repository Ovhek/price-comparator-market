package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a Store.
 * Used for API responses.
 */
@Data
@NoArgsConstructor
@Schema(description = "Represents a supermarket store or chain.")
public class StoreDTO {

    @Schema(description = "Unique identifier of the store.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Name of the store.", example = "Lidl", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
