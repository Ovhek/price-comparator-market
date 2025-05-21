package com.alexcruceat.pricecomparatormarket.controller.api.v1;

import com.alexcruceat.pricecomparatormarket.dto.DiscountedProductDTO;
import com.alexcruceat.pricecomparatormarket.dto.PageResponseDTO;
import com.alexcruceat.pricecomparatormarket.service.DiscountService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

/**
 * REST Controller for accessing discount information.
 */
@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discounts API", description = "Endpoints for finding product discounts")
public class DiscountController {

    private final DiscountService discountService;

    /**
     * Retrieves a paginated list of the best currently active discounts.
     * "Best" is determined by the highest discount percentage.
     *
     * @param referenceDateOptional Optional. The date to consider as "current" for active discounts.
     *                              Defaults to today if not provided. Format: yyyy-MM-dd.
     * @param pageable              Pagination and sorting parameters. Default sort is by discount percentage descending.
     * @return A {@link ResponseEntity} containing a {@link PageResponseDTO} of {@link DiscountedProductDTO}s.
     */
    @Operation(summary = "List best active discounts",
            description = "Returns a paginated list of products with the highest current discount percentages, " +
                    "active on the reference date (defaults to today).")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of best discounts.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseDTODiscountedProductWrapper.class)))
    @GetMapping("/best")
    public ResponseEntity<PageResponseDTO<DiscountedProductDTO>> getBestActiveDiscounts(
            @Parameter(description = "Reference date to check for active discounts (yyyy-MM-dd). Defaults to today.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> referenceDateOptional,
            @ParameterObject @PageableDefault(size = 10, sort = "percentage,desc") Pageable pageable) {

        LocalDate referenceDate = referenceDateOptional.orElse(LocalDate.now());
        log.info("Request for best active discounts for date: {}, pageable: {}", referenceDate, pageable);

        Page<DiscountedProductDTO> discountedProductPage = discountService.findBestActiveDiscounts(referenceDate, pageable);
        PageResponseDTO<DiscountedProductDTO> response = new PageResponseDTO<>(discountedProductPage);

        return ResponseEntity.ok(response);
    }

    /**
     * Inner static class for Swagger documentation of PageResponseDTO<DiscountedProductDTO>.
     */
    @Schema(name = "DiscountedProductPageResponse")
    private static class PageResponseDTODiscountedProductWrapper extends PageResponseDTO<DiscountedProductDTO> {}


}
