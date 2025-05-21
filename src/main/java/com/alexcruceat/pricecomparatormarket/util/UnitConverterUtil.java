package com.alexcruceat.pricecomparatormarket.util;

import com.alexcruceat.pricecomparatormarket.model.UnitOfMeasure;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for converting package quantities and prices to standard units.
 */
@Slf4j
public final class UnitConverterUtil {

    private static final int DEFAULT_SCALE = 4; // Scale for intermediate calculations
    private static final int PRICE_PER_UNIT_SCALE = 2; // Scale for final price per unit

    private UnitConverterUtil() {}

    /**
     * Normalizes a quantity to a base unit (KG for weight, L for volume).
     *
     * @param quantity The quantity to normalize.
     * @param unit     The unit of the quantity.
     * @return A {@link NormalizedQuantity} containing the quantity in the base unit and the base unit type,
     *         or null if the unit is not normalizable to KG/L (e.g., BUCATA, ROLE).
     */
    public static NormalizedQuantity normalizeQuantity(BigDecimal quantity, UnitOfMeasure unit) {
        if (quantity == null || unit == null) {
            return null;
        }

        return switch (unit) {
            case G ->
                // Convert grams to kilograms (quantity / 1000)
                    new NormalizedQuantity(quantity.divide(BigDecimal.valueOf(1000), DEFAULT_SCALE, RoundingMode.HALF_UP), BaseUnit.KG);
            case KG -> new NormalizedQuantity(quantity, BaseUnit.KG);
            case ML ->
                // Convert milliliters to liters (quantity / 1000)
                    new NormalizedQuantity(quantity.divide(BigDecimal.valueOf(1000), DEFAULT_SCALE, RoundingMode.HALF_UP), BaseUnit.L);
            case L -> new NormalizedQuantity(quantity, BaseUnit.L);
            default -> {
                // These units are not directly comparable by weight/volume in a simple way
                log.trace("Unit {} for quantity {} is not normalizable to KG/L.", unit, quantity);
                yield null;
            }
        };
    }

    /**
     * Calculates the price per standard base unit (e.g., price per KG or price per Litre).
     *
     * @param price           The price of the item.
     * @param packageQuantity The quantity in the package.
     * @param packageUnit     The unit of the package quantity.
     * @return A {@link PricePerStandardUnit} containing the normalized price and its unit,
     *         or null if the item's unit cannot be normalized or inputs are invalid.
     */
    public static PricePerStandardUnit calculatePricePerStandardUnit(BigDecimal price, BigDecimal packageQuantity, UnitOfMeasure packageUnit) {
        if (price == null || packageQuantity == null || packageUnit == null || packageQuantity.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Invalid input for price per standard unit calculation: price={}, quantity={}, unit={}", price, packageQuantity, packageUnit);
            return null;
        }

        NormalizedQuantity normalizedQuantity = normalizeQuantity(packageQuantity, packageUnit);
        if (normalizedQuantity == null) {
            return null; // Cannot normalize this item (e.g., it's sold by piece)
        }

        // pricePerUnit = total_price / normalized_quantity_in_base_unit
        BigDecimal pricePerBaseUnit = price.divide(normalizedQuantity.getQuantity(), PRICE_PER_UNIT_SCALE, RoundingMode.HALF_UP);
        return new PricePerStandardUnit(pricePerBaseUnit, normalizedQuantity.getBaseUnit());
    }

    /**
     * Represents a quantity normalized to a base unit (KG or L).
     */
    @Value
    public static class NormalizedQuantity {
        BigDecimal quantity;
        BaseUnit baseUnit;
    }

    /**
     * Represents a price calculated per standard base unit.
     */
    @Value
    public static class PricePerStandardUnit {
        BigDecimal price;
        BaseUnit unit;

        @Override
        public String toString() {
            return price + " per " + unit;
        }
    }

    /**
     * Defines the base units for normalization.
     */
    public enum BaseUnit {
        KG, L
    }
}
