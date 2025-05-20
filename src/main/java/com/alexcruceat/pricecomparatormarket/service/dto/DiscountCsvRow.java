package com.alexcruceat.pricecomparatormarket.service.dto;


import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single row of data from a Discount CSV file.
 * Fields correspond to the columns in the CSV.
 */
@Value
@Builder(toBuilder = true)
public class DiscountCsvRow {
    String productId;
    String productName;
    String brand;
    BigDecimal packageQuantity;
    String packageUnit;
    String productCategory;
    LocalDate fromDate;
    LocalDate toDate;
    Integer percentageOfDiscount;
}
