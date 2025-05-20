package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.service.*;
import com.alexcruceat.pricecomparatormarket.service.dto.ProductPriceCsvRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Implementation of {@link ProductDataHandlerService}.
 * Handles the business logic for creating or updating Product and PriceEntry entities
 * based on parsed CSV row data.
 * Brand and Category entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDataHandlerServiceImpl implements ProductDataHandlerService {

    private final ProductService productService;
    private final PriceEntryService priceEntryService;
    private final BrandService brandService;
    private final CategoryService categoryService;

    /**
     * {@inheritDoc}
     * This method operates within a transaction, typically initiated by the caller.
     */
    @Override
    @Transactional
    public void processAndSaveProductPrice(ProductPriceCsvRow priceData, Store store, LocalDate entryDate) {
        Brand brand = brandService.findOrCreateBrand(priceData.getBrand());
        Category category = categoryService.findOrCreateCategory(priceData.getProductCategory());

        Product product = productService.findByNameIgnoreCaseAndBrand(priceData.getProductName(), brand)
                .orElseGet(() -> {
                    log.debug("Creating new product: Name='{}', Brand='{}', Category='{}'",
                            priceData.getProductName(), brand.getName(), category.getName());
                    Product newProduct = new Product(priceData.getProductName(), category, brand);
                    return productService.save(newProduct);
                });

        // Ensure product's category is up-to-date if it was found but CSV has different info
        // (assuming CSV category is more current for the product's master data)
        if (!product.getCategory().getId().equals(category.getId())) {
            log.warn("Product '{}' (ID:{}) found with category '{}' (ID:{}) but CSV indicates category '{}' (ID:{}). Updating product's category.",
                    product.getName(), product.getId(),
                    product.getCategory().getName(), product.getCategory().getId(),
                    category.getName(), category.getId());
            product.setCategory(category);
            product = productService.save(product); // Save and re-assign
        }

        UnitOfMeasure unit = UnitOfMeasure.fromString(priceData.getPackageUnit());

        if (unit == UnitOfMeasure.UNKNOWN &&
                priceData.getPackageUnit() != null &&
                !priceData.getPackageUnit().trim().isEmpty() &&
                !priceData.getPackageUnit().equalsIgnoreCase("unknown")) {
            log.warn("Product '{}' (Store: {}): Unknown package unit '{}' encountered for price entry. Defaulting to UNKNOWN.",
                    priceData.getProductName(), store.getName(), priceData.getPackageUnit());
        }

        priceEntryService.saveOrUpdatePriceEntry(
                product,
                store,
                entryDate,
                priceData.getProductId(),
                priceData.getPrice(),
                priceData.getCurrency(),
                priceData.getPackageQuantity(),
                unit
        );
    }
}