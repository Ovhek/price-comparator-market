package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a user's shopping basket request for optimization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request containing a list of products for shopping basket optimization.")
public class ShoppingBasketRequestDTO {

    @NotBlank(message = "User ID cannot be blank.")
    @Schema(description = "Identifier for the user making the request.", example = "user123")
    private String userId;

    /**
     * List of items in the shopping basket.
     * Must not be empty and items themselves will be validated.
     */
    @NotEmpty(message = "Shopping basket items cannot be empty.")
    @Size(min = 1, message = "Basket must contain at least one item.")
    @Valid
    @Schema(description = "List of products and their desired quantities for the basket.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ShoppingBasketItemRequestDTO> items;
}
