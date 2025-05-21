package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.dto.PriceTrendResponseDTO;
import com.alexcruceat.pricecomparatormarket.util.UnitConverterUtil;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service interface for aggregating and retrieving price history data.
 * <p>
 * Provides methods for retrieving price trends of individual products, product categories, and brands,
 * optionally filtered by store and date range.
 * </p>
 */
public interface PriceHistoryAggregationService {

    /**
     * Retrieves the historical price data for a specific product.
     *
     * @param productId     The ID of the product.
     * @param storeIdOpt    Optional ID of the store to filter prices.
     * @param startDateOpt  Optional start date (inclusive) for filtering the price history.
     * @param endDateOpt    Optional end date (inclusive) for filtering the price history.
     * @return A {@link PriceTrendResponseDTO} containing the historical price trend for the product.
     */
    PriceTrendResponseDTO getIndividualProductHistory(Long productId, Optional<Long> storeIdOpt,
                                                      Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt);

    /**
     * Retrieves the aggregated price trend for a given product category.
     *
     * @param categoryId    The ID of the category.
     * @param baseUnit      The unit (KG or L) used to normalize the price.
     * @param storeIdOpt    Optional ID of the store to filter prices.
     * @param startDateOpt  Optional start date (inclusive) for filtering the trend.
     * @param endDateOpt    Optional end date (inclusive) for filtering the trend.
     * @return A {@link PriceTrendResponseDTO} containing the average price trend for the category.
     */
    PriceTrendResponseDTO getAggregatedCategoryTrend(Long categoryId, UnitConverterUtil.BaseUnit baseUnit,
                                                     Optional<Long> storeIdOpt,
                                                     Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt);

    /**
     * Retrieves the aggregated price trend for a specific brand.
     *
     * @param brandId       The ID of the brand.
     * @param baseUnit      The unit (KG or L) used to normalize the price.
     * @param storeIdOpt    Optional ID of the store to filter prices.
     * @param startDateOpt  Optional start date (inclusive) for filtering the trend.
     * @param endDateOpt    Optional end date (inclusive) for filtering the trend.
     * @return A {@link PriceTrendResponseDTO} containing the average price trend for the brand.
     */
    PriceTrendResponseDTO getAggregatedBrandTrend(Long brandId, UnitConverterUtil.BaseUnit baseUnit,
                                                  Optional<Long> storeIdOpt,
                                                  Optional<LocalDate> startDateOpt, Optional<LocalDate> endDateOpt);

}
