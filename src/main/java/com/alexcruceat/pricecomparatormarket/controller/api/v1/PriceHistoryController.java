package com.alexcruceat.pricecomparatormarket.controller.api.v1;
import com.alexcruceat.pricecomparatormarket.dto.PriceTrendResponseDTO;
import com.alexcruceat.pricecomparatormarket.service.PriceHistoryAggregationService;
import com.alexcruceat.pricecomparatormarket.util.UnitConverterUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

import static com.alexcruceat.pricecomparatormarket.config.ApiConstants.*;

/**
 * REST controller for accessing price history and trends of products, categories, and brands.
 * <p>
 * Provides endpoints to retrieve historical price data and aggregated price trends,
 * optionally filtered by store and/or date range.
 * </p>
 */
@RestController
@RequestMapping(PRICE_HISTORY_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Price History API", description = "Endpoints for retrieving product, category, or brand price trends and history.")
public class PriceHistoryController {

    private final PriceHistoryAggregationService priceHistoryService; // New unified service

    /**
     * Retrieves the price history for an individual product.
     *
     * @param productId The ID of the product to retrieve price history for. Must not be null.
     * @param storeId   (Optional) The ID of the store to filter prices.
     * @param startDate (Optional) The start date (inclusive) for filtering price history.
     * @param endDate   (Optional) The end date (inclusive) for filtering price history.
     * @return A {@link ResponseEntity} containing the {@link PriceTrendResponseDTO} for the product.
     */
    @Operation(summary = "Get price history for an individual product",
            description = "Retrieves a list of actual price points over time for a given product. " +
                    "Can be filtered by store ID and a date range.")
    @ApiResponses(value = { /* ... (similar to previous individual product history) ... */
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product price history.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceTrendResponseDTO.class)))
    })
    @GetMapping(HISTORY_PRODUCT_SUBPATH)
    public ResponseEntity<PriceTrendResponseDTO> getIndividualProductPriceHistory(
            @Parameter(description = "ID of the product.", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(description = "Optional ID of the store.") @RequestParam(required = false) Optional<Long> storeId,
            @Parameter(description = "Optional start date (yyyy-MM-dd).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @Parameter(description = "Optional end date (yyyy-MM-dd).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate) {
        log.info("Request for individual product price history: productId={}, storeId={}, startDate={}, endDate={}",
                productId, storeId, startDate, endDate);
        PriceTrendResponseDTO history = priceHistoryService.getIndividualProductHistory(productId, storeId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves the aggregated price trend for a specific category.
     *
     * @param categoryId The ID of the category to retrieve the trend for. Must not be null.
     * @param baseUnit   The base unit (KG or L) for price normalization.
     * @param storeId    (Optional) The ID of the store to filter prices.
     * @param startDate  (Optional) The start date (inclusive) for filtering the trend.
     * @param endDate    (Optional) The end date (inclusive) for filtering the trend.
     * @return A {@link ResponseEntity} containing the {@link PriceTrendResponseDTO} for the category.
     */
    @Operation(summary = "Get aggregated price trend for a category",
            description = "Calculates and returns the average price trend per standard unit (KG or L) " +
                    "for products within a specified category. Can be filtered by store and date range.")
    @ApiResponses(value = { /* ... (similar to previous category trend) ... */
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category price trend.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceTrendResponseDTO.class)))
    })
    @GetMapping(HISTORY_CATEGORY_SUBPATH)
    public ResponseEntity<PriceTrendResponseDTO> getCategoryPriceTrend(
            @Parameter(description = "ID of the category.", required = true, example = "1")
            @PathVariable Long categoryId,
            @Parameter(description = "Base unit for price normalization (KG or L).", required = true, example = "KG")
            @RequestParam UnitConverterUtil.BaseUnit baseUnit,
            @Parameter(description = "Optional ID of the store.") @RequestParam(required = false) Optional<Long> storeId,
            @Parameter(description = "Optional start date (yyyy-MM-dd).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @Parameter(description = "Optional end date (yyyy-MM-dd).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate) {
        log.info("Request for category price trend: categoryId={}, baseUnit={}, storeId={}, startDate={}, endDate={}",
                categoryId, baseUnit, storeId, startDate, endDate);
        PriceTrendResponseDTO trend = priceHistoryService.getAggregatedCategoryTrend(categoryId, baseUnit, storeId, startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    /**
     * Retrieves the aggregated price trend for a specific brand.
     *
     * @param brandId   The ID of the brand to retrieve the trend for. Must not be null.
     * @param baseUnit  The base unit (KG or L) for price normalization.
     * @param storeId   (Optional) The ID of the store to filter prices.
     * @param startDate (Optional) The start date (inclusive) for filtering the trend.
     * @param endDate   (Optional) The end date (inclusive) for filtering the trend.
     * @return A {@link ResponseEntity} containing the {@link PriceTrendResponseDTO} for the brand.
     */
    @Operation(summary = "Get aggregated price trend for a brand",
            description = "Calculates and returns the average price trend per standard unit (KG or L) " +
                    "for products under a specified brand. Can be filtered by store and date range.")
    @ApiResponses(value = { /* ... (similar to previous brand trend) ... */
            @ApiResponse(responseCode = "200", description = "Successfully retrieved brand price trend.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceTrendResponseDTO.class)))
    })
    @GetMapping(HISTORY_BRAND_SUBPATH)
    public ResponseEntity<PriceTrendResponseDTO> getBrandPriceTrend(
            @Parameter(description = "ID of the brand.", required = true, example = "5")
            @PathVariable Long brandId,
            @Parameter(description = "Base unit for price normalization (KG or L).", required = true, example = "L")
            @RequestParam UnitConverterUtil.BaseUnit baseUnit,
            @Parameter(description = "Optional ID of the store.") @RequestParam(required = false) Optional<Long> storeId,
            @Parameter(description = "Optional start date (yyyy-MM-dd).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @Parameter(description = "Optional end date (yyyy-MM-dd).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate) {
        log.info("Request for brand price trend: brandId={}, baseUnit={}, storeId={}, startDate={}, endDate={}",
                brandId, baseUnit, storeId, startDate, endDate);
        PriceTrendResponseDTO trend = priceHistoryService.getAggregatedBrandTrend(brandId, baseUnit, storeId, startDate, endDate);
        return ResponseEntity.ok(trend);
    }
}
