package com.alexcruceat.pricecomparatormarket.service.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Represents a single row of data from a Product Price CSV file.
 * Fields correspond to the columns in the CSV.
 */
@Value
@Builder
public class ProductPriceCsvRow {
    String productId;
    String productName;
    String productCategory;
    String brand;
    BigDecimal packageQuantity;
    String packageUnit;
    BigDecimal price;
    String currency;
}