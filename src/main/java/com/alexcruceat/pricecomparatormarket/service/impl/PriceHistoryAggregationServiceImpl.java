package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.PriceEntryRepository; // For direct optimized queries
import com.alexcruceat.pricecomparatormarket.dto.*;
import com.alexcruceat.pricecomparatormarket.exception.InvalidInputException;
import com.alexcruceat.pricecomparatormarket.exception.ResourceNotFoundException;
import com.alexcruceat.pricecomparatormarket.mapper.BrandMapper;
import com.alexcruceat.pricecomparatormarket.mapper.CategoryMapper;
import com.alexcruceat.pricecomparatormarket.mapper.PriceEntryMapper;
import com.alexcruceat.pricecomparatormarket.mapper.ProductMapper;
import com.alexcruceat.pricecomparatormarket.service.*;
import com.alexcruceat.pricecomparatormarket.util.UnitConverterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Implementation of {@link PriceHistoryAggregationService}.
 * Provides methods to fetch and aggregate price data for individual products,
 * categories, or brands, returning a unified {@link PriceTrendResponseDTO}.
 */
public class PriceHistoryAggregationServiceImpl implements PriceHistoryAggregationService {

    private final ProductService productService; // Used to fetch Product details
    private final CategoryService categoryService; // Used to fetch Category details
    private final BrandService brandService;       // Used to fetch Brand details
    private final StoreService storeService;

    private final PriceEntryRepository priceEntryRepository; // For direct, optimized queries

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final PriceEntryMapper priceEntryMapper; // For converting PriceEntry to TrendPointDTO

    private static final long DEFAULT_INDIVIDUAL_HISTORY_DAYS = 365; // 1 year
    private static final long DEFAULT_AGGREGATED_TREND_DAYS = 90;  // 3 months

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PriceTrendResponseDTO getIndividualProductHistory(Long productId, Optional<Long> storeIdOpt,
                                                             Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt) {
        log.debug("Fetching individual price history for productId: {}, storeId: {}, startDate: {}, endDate: {}",
                productId, storeIdOpt, startDateOpt, endDateOpt);

        Product product = productService.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        LocalDate endDate = endDateOpt.orElse(LocalDate.now());
        LocalDate startDate = startDateOpt.orElse(endDate.minusDays(DEFAULT_INDIVIDUAL_HISTORY_DAYS - 1));
        validateDateRange(startDate, endDate, "individual product history");

        List<PriceEntry> priceEntries = priceEntryRepository.findIndividualProductHistory(
                productId, storeIdOpt.orElse(null), startDate, endDate
        );

        List<TrendPointDTO> trendPoints = priceEntryMapper.toTrendPointDTOList(priceEntries);

        Map<String, String> filters = buildFilterMap(
                "productId", productId.toString(),
                storeIdOpt, startDateOpt, endDateOpt, Optional.empty()
        );

        return PriceTrendResponseDTO.builder()
                .entityType("PRODUCT")
                .entityId(product.getId())
                .entityName(product.getName())
                .entityDetails(productMapper.toDTO(product))
                .trendPoints(trendPoints)
                .filtersApplied(filters)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PriceTrendResponseDTO getAggregatedCategoryTrend(Long categoryId, UnitConverterUtil.BaseUnit baseUnit,
                                                            Optional<Long> storeIdOpt,
                                                            Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt) {
        log.debug("Fetching aggregated category trend for categoryId: {}, baseUnit: {}, storeId: {}, startDate: {}, endDate: {}",
                categoryId, baseUnit, storeIdOpt, startDateOpt, endDateOpt);

        Category category = categoryService.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));


        LocalDate endDate = endDateOpt.orElse(LocalDate.now());
        LocalDate startDate = startDateOpt.orElse(endDate.minusDays(DEFAULT_AGGREGATED_TREND_DAYS - 1));
        validateDateRange(startDate, endDate, "category trend");

        List<PriceEntry> entries = priceEntryRepository.findPriceEntriesForCategoryTrend(
                categoryId, storeIdOpt.orElse(null), startDate, endDate
        );

        List<TrendPointDTO> trendPoints = calculateAggregatedTrendPoints(entries, baseUnit, "Category: " + category.getName());
        Map<String, String> filters = buildFilterMap(
                "categoryId", categoryId.toString(),
                storeIdOpt, startDateOpt, endDateOpt, Optional.of(baseUnit)
        );

        return PriceTrendResponseDTO.builder()
                .entityType("CATEGORY")
                .entityId(category.getId())
                .entityName(category.getName())
                .entityDetails(categoryMapper.toDTO(category))
                .trendPoints(trendPoints)
                .filtersApplied(filters)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PriceTrendResponseDTO getAggregatedBrandTrend(Long brandId, UnitConverterUtil.BaseUnit baseUnit,
                                                         Optional<Long> storeIdOpt,
                                                         Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt) {
        log.debug("Fetching aggregated brand trend for brandId: {}, baseUnit: {}, storeId: {}, startDate: {}, endDate: {}",
                brandId, baseUnit, storeIdOpt, startDateOpt, endDateOpt);

        Brand brand = brandService.findById(brandId).orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + brandId));

        LocalDate endDate = endDateOpt.orElse(LocalDate.now());
        LocalDate startDate = startDateOpt.orElse(endDate.minusDays(DEFAULT_AGGREGATED_TREND_DAYS - 1));
        validateDateRange(startDate, endDate, "brand trend");

        List<PriceEntry> entries = priceEntryRepository.findPriceEntriesForBrandTrend(
                brandId, storeIdOpt.orElse(null), startDate, endDate
        );

        List<TrendPointDTO> trendPoints = calculateAggregatedTrendPoints(entries, baseUnit, "Brand: " + brand.getName());
        Map<String, String> filters = buildFilterMap(
                "brandId", brandId.toString(),
                storeIdOpt, startDateOpt, endDateOpt, Optional.of(baseUnit)
        );

        return PriceTrendResponseDTO.builder()
                .entityType("BRAND")
                .entityId(brand.getId())
                .entityName(brand.getName())
                .entityDetails(brandMapper.toDTO(brand))
                .trendPoints(trendPoints)
                .filtersApplied(filters)
                .build();
    }

    /**
     * Calculates aggregated trend points (daily average price per target base unit)
     * from a list of price entries.
     *
     * @param entries        The list of {@link PriceEntry} to aggregate.
     * @param targetBaseUnit The {@link UnitConverterUtil.BaseUnit} (KG or L) for normalization and aggregation.
     * @param contextForLog  A string describing the context (e.g., category/brand name) for logging.
     * @return A list of {@link TrendPointDTO} representing the daily aggregated trend.
     */
    private List<TrendPointDTO> calculateAggregatedTrendPoints(List<PriceEntry> entries,
                                                               UnitConverterUtil.BaseUnit targetBaseUnit,
                                                               String contextForLog) {
        if (entries.isEmpty()) {
            log.info("No price entries found for {} to calculate trend.", contextForLog);
            return List.of();
        }

        Map<LocalDate, List<UnitConverterUtil.PricePerStandardUnit>> pricesByDateAndUnit =
                entries.stream()
                        .map(entry -> {
                            UnitConverterUtil.PricePerStandardUnit ppsu = UnitConverterUtil.calculatePricePerStandardUnit(
                                    entry.getPrice(), entry.getPackageQuantity(), entry.getPackageUnit());
                            if (ppsu != null && ppsu.getUnit() == targetBaseUnit) {
                                return new DatedPricePerStandardUnit(entry.getEntryDate(), ppsu);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                DatedPricePerStandardUnit::getDate,
                                Collectors.mapping(DatedPricePerStandardUnit::getPricePerStandardUnit, Collectors.toList())
                        ));

        return pricesByDateAndUnit.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<UnitConverterUtil.PricePerStandardUnit> priceList = entry.getValue();
                    if (priceList.isEmpty()) return null; // Should not happen if filter(Objects::nonNull) worked

                    BigDecimal sumOfPrices = priceList.stream()
                            .map(UnitConverterUtil.PricePerStandardUnit::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal averagePrice = sumOfPrices.divide(BigDecimal.valueOf(priceList.size()), 2, RoundingMode.HALF_UP);
                    return TrendPointDTO.builder()
                            .date(date)
                            .value(averagePrice)
                            .valueUnitDescription("per " + targetBaseUnit.name())
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TrendPointDTO::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Validates that the start date is not after the end date.
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @param context   Description of the operation for the error message.
     * @throws InvalidInputException if startDate is after endDate.
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate, String context) {
        if (startDate.isAfter(endDate)) {
            String errorMessage = String.format("Invalid date range for %s: start date (%s) cannot be after end date (%s).",
                    context, startDate, endDate);
            log.warn(errorMessage);
            throw new InvalidInputException(errorMessage);
        }
    }

    /**
     * Builds a map of applied filters for inclusion in the {@link PriceTrendResponseDTO}.
     *
     * @param entityFilterType Type of the main entity filter (e.g., "productId", "categoryId").
     * @param entityFilterId   ID value for the main entity filter.
     * @param storeIdOpt       Optional store ID filter.
     * @param startDateOpt     Optional start date filter.
     * @param endDateOpt       Optional end date filter.
     * @param baseUnitOpt      Optional base unit filter (for aggregated trends).
     * @return A map containing string representations of the applied filters.
     */
    private Map<String, String> buildFilterMap(String entityFilterType, String entityFilterId,
                                               Optional<Long> storeIdOpt,
                                               Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt,
                                               Optional<UnitConverterUtil.BaseUnit> baseUnitOpt) {
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put(entityFilterType, entityFilterId);
        storeIdOpt.ifPresent(id -> filters.put("storeId", id.toString()));
        startDateOpt.ifPresent(date -> filters.put("startDate", date.toString()));
        endDateOpt.ifPresent(date -> filters.put("endDate", date.toString()));
        baseUnitOpt.ifPresent(bu -> filters.put("baseUnit", bu.name()));
        return filters; // Return even if empty, DTO handles nullability display
    }

    // Helper inner class for streaming in calculateAggregatedTrendPoints
    @lombok.Value
    private static class DatedPricePerStandardUnit {
        LocalDate date;
        UnitConverterUtil.PricePerStandardUnit pricePerStandardUnit;
    }
}
