package com.alexcruceat.pricecomparatormarket.controller.api.v1;

import com.alexcruceat.pricecomparatormarket.dto.BasketOptimizationResponseDTO;
import com.alexcruceat.pricecomparatormarket.dto.ShoppingBasketRequestDTO;
import com.alexcruceat.pricecomparatormarket.service.ShoppingBasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alexcruceat.pricecomparatormarket.config.ApiConstants.*;

/**
 * REST Controller for shopping basket operations, primarily for optimization.
 */
@RestController
@RequestMapping(SHOPPING_BASKETS_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Basket API", description = "Endpoints for shopping basket optimization")
public class ShoppingBasketController {

    private final ShoppingBasketService shoppingBasketService;

    /**
     * Optimizes a given shopping basket to find the most cost-effective way to purchase items,
     * potentially suggesting purchases across multiple stores.
     *
     * @param basketRequest The user's shopping basket, including user ID and list of desired items.
     * @return A {@link ResponseEntity} containing the {@link BasketOptimizationResponseDTO}
     *         with the optimized shopping plan.
     */
    @Operation(summary = "Optimize a shopping basket",
            description = "Analyzes the provided shopping basket and returns a plan suggesting " +
                    "where to buy each item to minimize overall cost. Includes potential savings.")
    @ApiResponse(responseCode = "200", description = "Shopping basket optimized successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BasketOptimizationResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid basket request (e.g., empty items, invalid product IDs).",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = com.alexcruceat.pricecomparatormarket.exception.GlobalApiExceptionHandler.ErrorResponse.class)))
    @PostMapping(BASKETS_OPTIMIZE_SUBPATH)
    public ResponseEntity<BasketOptimizationResponseDTO> optimizeBasket(
            @Valid @RequestBody ShoppingBasketRequestDTO basketRequest) {
        log.info("Received request to optimize shopping basket for userId: {}", basketRequest.getUserId());
        BasketOptimizationResponseDTO optimizationResult = shoppingBasketService.optimizeShoppingBasket(basketRequest);
        return ResponseEntity.ok(optimizationResult);
    }
}
