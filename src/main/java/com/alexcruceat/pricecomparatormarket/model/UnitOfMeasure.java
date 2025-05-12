package com.alexcruceat.pricecomparatormarket.model;

/**
 * Represents the unit of measure for a product's package quantity.
 * This enum includes common units found in grocery items.
 */
public enum UnitOfMeasure {
    /** Liters, typically for liquids. */
    L,
    /** Milliliters, typically for smaller liquid quantities. */
    ML,
    /** Kilograms, typically for weight. */
    KG,
    /** Grams, typically for smaller weights. */
    G,
    /** Bucata (Piece/Unit), for items sold individually. */
    BUCATA,
    /** Role, typically for paper products. */
    ROLE,
    /** Unknown or not applicable unit. */
    UNKNOWN;

    /**
     * Attempts to parse a string value into a {@link UnitOfMeasure} enum constant.
     * This method is case-insensitive and handles common variations.
     *
     * @param value The string representation of the unit (e.g., "l", "kg", "buc").
     * @return The corresponding {@link UnitOfMeasure} constant.
     * @throws IllegalArgumentException if the string cannot be parsed into a known unit.
     */
    public static UnitOfMeasure fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN;
        }
        String upperValue = value.trim().toUpperCase();
        return switch (upperValue) {
            case "L", "LITRU", "LITRI" -> L;
            case "ML", "MILILITRI" -> ML;
            case "KG", "KILOGRAM", "KILOGRAME" -> KG;
            case "G", "GRAM", "GRAME" -> G;
            case "BUC", "BUCATA", "BUCATI", "UNIT", "UNITS" -> BUCATA;
            case "ROLE", "ROLA" -> ROLE;
            default -> UNKNOWN;
        };
    }
}