package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.DiscountRepository;
import com.alexcruceat.pricecomparatormarket.service.BrandService;
import com.alexcruceat.pricecomparatormarket.service.DiscountDataHandlerService;
import com.alexcruceat.pricecomparatormarket.service.DiscountService;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import com.alexcruceat.pricecomparatormarket.service.dto.DiscountCsvRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Implementation of {@link DiscountDataHandlerService}.
 * Handles the business logic for creating or updating Discount entities
 * based on parsed CSV row data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountDataHandlerServiceImpl implements DiscountDataHandlerService {

    private final ProductService productService;
    private final DiscountService discountService;
    private final BrandService brandService;

    /**
     * {@inheritDoc}
     * This method operates within a transaction, typically initiated by the caller.
     */
    @Override
    @Transactional
    public void processAndSaveDiscount(DiscountCsvRow discountData, Store store, LocalDate recordedAtDate) {
        Brand brand = brandService.findOrCreateBrand(discountData.getBrand());

        Optional<Product> productOpt = productService.findByNameIgnoreCaseAndBrand(discountData.getProductName(), brand);

        if (productOpt.isEmpty()) {
            log.warn("Product not found for discount: Name='{}', Brand='{}' (Store context: {}). Skipping discount.",
                    discountData.getProductName(), brand.getName(), store.getName());
            return;
        }
        Product product = productOpt.get();
        UnitOfMeasure unit = UnitOfMeasure.fromString(discountData.getPackageUnit());

        if (unit == UnitOfMeasure.UNKNOWN &&
                discountData.getPackageUnit() != null &&
                !discountData.getPackageUnit().trim().isEmpty() &&
                !discountData.getPackageUnit().equalsIgnoreCase("unknown")) {
            log.warn("Discount for Product '{}' (Store: {}): Unknown package unit '{}' encountered. Defaulting to UNKNOWN.",
                    discountData.getProductName(), store.getName(), discountData.getPackageUnit());
        }

        Optional<Discount> existingDiscountOpt = discountService.findDiscountByNaturalKey(
                product, store, discountData.getFromDate(), discountData.getPackageQuantity(), unit
        );

        if (existingDiscountOpt.isPresent()) {
            Discount existingDiscount = existingDiscountOpt.get();
            // Check if the new record is more recent or different
            if (!existingDiscount.getPercentage().equals(discountData.getPercentageOfDiscount()) ||
                    !existingDiscount.getToDate().equals(discountData.getToDate()) ||
                    recordedAtDate.isAfter(existingDiscount.getRecordedAtDate())) {
                log.debug("Updating existing Discount for Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}. Old %: {}, New %: {}. Old ToDate: {}, New ToDate: {}",
                        product.getId(), store.getName(), discountData.getFromDate(),
                        discountData.getPackageQuantity(), unit,
                        existingDiscount.getPercentage(), discountData.getPercentageOfDiscount(),
                        existingDiscount.getToDate(), discountData.getToDate());

                existingDiscount.setPercentage(discountData.getPercentageOfDiscount());
                existingDiscount.setToDate(discountData.getToDate());
                existingDiscount.setRecordedAtDate(recordedAtDate);
                discountService.save(existingDiscount);
            } else {
                log.debug("Skipping update for existing Discount as new data is not different or newer. Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}",
                        product.getId(), store.getName(), discountData.getFromDate(),
                        discountData.getPackageQuantity(), unit);
            }
        } else {
            Discount newDiscount = new Discount(
                    product,
                    store,
                    discountData.getPercentageOfDiscount(),
                    discountData.getFromDate(),
                    discountData.getToDate(),
                    recordedAtDate,
                    discountData.getPackageQuantity(),
                    unit
            );
            log.debug("Creating new Discount for Product ID: {}, Store: {}, FromDate: {}, Pkg: {} {}, %: {}",
                    product.getId(), store.getName(), newDiscount.getFromDate(),
                    newDiscount.getPackageQuantity(), newDiscount.getPackageUnit(), newDiscount.getPercentage());
            discountService.save(newDiscount);
        }
    }
}
