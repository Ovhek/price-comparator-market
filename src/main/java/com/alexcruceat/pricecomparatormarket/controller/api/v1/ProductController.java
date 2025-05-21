package com.alexcruceat.pricecomparatormarket.controller.api.v1;


import com.alexcruceat.pricecomparatormarket.dto.PageResponseDTO;
import com.alexcruceat.pricecomparatormarket.dto.ProductDTO;
import com.alexcruceat.pricecomparatormarket.dto.ProductValueDTO;
import com.alexcruceat.pricecomparatormarket.exception.ResourceNotFoundException;
import com.alexcruceat.pricecomparatormarket.mapper.ProductMapper;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import com.alexcruceat.pricecomparatormarket.service.specification.ProductSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * REST Controller for querying product information.
 * Provides endpoints for listing products with filtering, pagination, and sorting,
 * as well as retrieving details for a single product.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products API", description = "Endpoints for accessing product information")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * Retrieves a paginated list of products based on specified filter criteria.
     * Supports filtering by product name (partial, case-insensitive), category ID, brand ID,
     * and store ID (indicating products available in that store).
     * Pagination and sorting are handled via the {@link Pageable} parameter.
     *
     * @param name       Optional filter for product name.
     * @param categoryId Optional filter for the ID of the product's category.
     * @param brandId    Optional filter for the ID of the product's brand.
     * @param storeId    Optional filter for the ID of a store; lists products available in this store.
     * @param pageable   Spring Data {@link Pageable} object for pagination (page, size) and sorting (sort).
     *                   Defaults (size 20, sort by name ascending) are applied if not specified by the client.
     * @return A {@link ResponseEntity} containing a {@link PageResponseDTO} of {@link ProductDTO}s.
     *         Returns HTTP 200 (OK) with the paginated list of products.
     */
    @Operation(summary = "List products with filters, pagination, and sorting",
            description = "Returns a paginated list of products. Filters can be applied for name (partial match, case-insensitive), " +
                    "category ID, brand ID, and store ID (products available in the specified store). " +
                    "Default page size is 20, sorted by product name ascending.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of products.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseDTOProductWrapper.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters (e.g., invalid pageable format or filter values).",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = com.alexcruceat.pricecomparatormarket.exception.GlobalApiExceptionHandler.ErrorResponse.class))) // Adjust if ErrorResponse path is different
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductDTO>> listProducts(
            @Parameter(description = "Filter by product name (partial, case-insensitive match). Example: 'lapte'")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by category ID. Example: 1")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Filter by brand ID. Example: 5")
            @RequestParam(required = false) Long brandId,

            @Parameter(description = "Filter by store ID (products available in this store). Example: 2")
            @RequestParam(required = false) Long storeId,

            @ParameterObject
            @PageableDefault(size = 20, sort = "name") // Default size and sort order
            Pageable pageable) {

        log.info("Received request to list products. Filters: name='{}', categoryId={}, brandId={}, storeId={}. Pageable: {}",
                name, categoryId, brandId, storeId, pageable);

        Specification<Product> spec = ProductSpecification.byCriteria(name, categoryId, brandId, storeId);
        Page<Product> productPage = productService.findAll(spec, pageable);
        Page<ProductDTO> productDtoPage = productPage.map(productMapper::toDTO);
        PageResponseDTO<ProductDTO> response = new PageResponseDTO<>(productDtoPage);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the details of a specific product by its ID.
     *
     * @param id The unique identifier of the product to retrieve.
     * @return A {@link ResponseEntity} containing the {@link ProductDTO} if found.
     *         Returns HTTP 200 (OK) with the product details.
     *         Returns HTTP 404 (Not Found) if no product exists with the given ID.
     */
    @Operation(summary = "Get product details by ID",
            description = "Retrieves all available details for a specific product identified by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product details.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductDTO.class)))
    @ApiResponse(responseCode = "404", description = "Product not found with the specified ID.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = com.alexcruceat.pricecomparatormarket.exception.GlobalApiExceptionHandler.ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Unique ID of the product to retrieve.", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Received request to get product by ID: {}", id);

        return productService.findById(id)
                .map(productMapper::toDTO) // Convert Product entity to ProductDTO
                .map(ResponseEntity::ok)    // If found, wrap DTO in ResponseEntity.ok()
                .orElseThrow(() -> {        // If not found, throw ResourceNotFoundException
                    log.warn("Product not found with ID: {}", id);
                    return new ResourceNotFoundException("Product not found with ID: " + id);
                });
    }

    /**
     * Retrieves a paginated list of products enriched with value-per-unit analysis.
     * This helps identify best buys by normalizing prices to a standard unit (e.g., per KG/Litre).
     *
     * @param name        Optional filter for product name.
     * @param categoryId  Optional filter for category ID.
     * @param brandId     Optional filter for brand ID.
     * @param storeId  Optional filter for store ID. If provided, analysis is for prices in this store.
     * @param referenceDate Optional. The date to consider for current prices. Defaults to today.
     * @param pageable    Pagination and sorting parameters.
     * @return A {@link ResponseEntity} containing a {@link PageResponseDTO} of {@link ProductValueDTO}s.
     */
    @Operation(summary = "List products with value-per-unit analysis",
            description = "Returns a paginated list of products, each enriched with its price per standard unit (e.g., per KG/Litre). " +
                    "Useful for comparing value across different package sizes. Filters can be applied.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products with value analysis.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseDTOProductValueWrapper.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters.")
    @GetMapping("/value-analysis")
    public ResponseEntity<PageResponseDTO<ProductValueDTO>> listProductsWithValueAnalysis(
            @Parameter(description = "Filter by product name (partial, case-insensitive match).")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by category ID.")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by brand ID.")
            @RequestParam(required = false) Long brandId,
            @Parameter(description = "Optional: Filter by store ID for price context.")
            @RequestParam(required = false) Optional<Long> storeId,
            @Parameter(description = "Reference date for current prices (yyyy-MM-dd). Defaults to today.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> referenceDate,
            @ParameterObject @PageableDefault(size = 20, sort = "pricePerStandardUnit,asc") Pageable pageable) {

        LocalDate effectiveReferenceDate = referenceDate.orElse(LocalDate.now());
        log.info("Request for products with value analysis. Filters: name='{}', categoryId={}, brandId={}, storeId={}, refDate={}. Pageable: {}",
                name, categoryId, brandId, storeId, effectiveReferenceDate, pageable);

        Page<ProductValueDTO> productValuePage = productService.findProductsWithValueAnalysis(
                name, categoryId, brandId, storeId, effectiveReferenceDate, pageable
        );
        PageResponseDTO<ProductValueDTO> response = new PageResponseDTO<>(productValuePage);
        return ResponseEntity.ok(response);
    }

    /**
     * Inner static class used as a workaround for Swagger/OpenAPI documentation
     * to correctly represent the generic type {@code PageResponseDTO<ProductDTO>}.
     * This helps in generating a clear and accurate API specification.
     */
    @Schema(name = "ProductPageResponse", description = "Paginated response containing a list of products and pagination details.")
    private static class PageResponseDTOProductWrapper extends PageResponseDTO<ProductDTO> {}

    /**
     * Inner static class for Swagger documentation of PageResponseDTO<ProductValueDTO>.
     */
    @Schema(name = "ProductValuePageResponse", description = "Paginated response containing a list of products value and pagination details.")
    private static class PageResponseDTOProductValueWrapper extends PageResponseDTO<ProductValueDTO> {}

}
