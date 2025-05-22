package com.alexcruceat.pricecomparatormarket.config;

/**
 * Utility class for storing API path constants and other frequently used string literals.
 * This helps in maintaining consistency and eases refactoring of paths or common values.
 */
public final class ApiConstants {

    private ApiConstants() {
    }

    // API Version Prefix
    public static final String API_V1_PREFIX = "/api/v1";

    // Base Endpoints
    public static final String HEALTH_ENDPOINT = API_V1_PREFIX + "/health";
    public static final String PRODUCTS_ENDPOINT = API_V1_PREFIX + "/products";
    public static final String PRICE_HISTORY_ENDPOINT = API_V1_PREFIX + "/price-history";
    public static final String DISCOUNTS_ENDPOINT = API_V1_PREFIX + "/discounts";
    public static final String PRICE_ALERTS_ENDPOINT = API_V1_PREFIX + "/price-alerts";
    public static final String SHOPPING_BASKETS_ENDPOINT = API_V1_PREFIX + "/shopping-baskets";

    // Subpaths for Products
    public static final String PRODUCT_ID_PATH_VARIABLE = "/{id}";
    public static final String VALUE_ANALYSIS_SUBPATH = "/value-analysis";

    // Subpaths for Price History
    public static final String HISTORY_PRODUCT_SUBPATH = "/product/{productId}";
    public static final String HISTORY_CATEGORY_SUBPATH = "/category/{categoryId}";
    public static final String HISTORY_BRAND_SUBPATH = "/brand/{brandId}";

    // Subpaths for Discounts
    public static final String BEST_DISCOUNTS_SUBPATH = "/best";
    public static final String NEW_DISCOUNTS_SUBPATH = "/new";

    // Subpaths for Price Alerts
    public static final String ALERTS_USER_SUBPATH = "/user/{userId}";
    public static final String ALERTS_USER_ACTIVE_SUBPATH = ALERTS_USER_SUBPATH + "/active";
    public static final String ALERTS_ID_SUBPATH = "/{alertId}";
    public static final String ALERTS_DEACTIVATE_SUBPATH = ALERTS_ID_SUBPATH + "/deactivate";

    // Subpaths for Shopping Baskets
    public static final String BASKETS_OPTIMIZE_SUBPATH = "/optimize";

    // Common Sort Parameters
    public static final String SORT_BY_NAME_ASC = "name,asc";
    public static final String SORT_BY_PRICE_PER_STANDARD_UNIT_ASC = "pricePerStandardUnit,asc";
    public static final String SORT_BY_DISCOUNT_PERCENTAGE_DESC = "percentage,desc";
    public static final String SORT_BY_RECORDED_AT_DATE_DESC = "recordedAtDate,desc";
}
