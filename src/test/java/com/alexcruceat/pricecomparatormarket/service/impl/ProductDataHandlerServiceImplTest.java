package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.service.BrandService;
import com.alexcruceat.pricecomparatormarket.service.CategoryService;
import com.alexcruceat.pricecomparatormarket.service.PriceEntryService;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import com.alexcruceat.pricecomparatormarket.service.dto.ProductPriceCsvRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProductDataHandlerServiceImpl}.
 * Mocks dependent services (BrandService, CategoryService, ProductService, PriceEntryService)
 * to verify the orchestration logic of processing product price data.
 */
@ExtendWith(MockitoExtension.class)
class ProductDataHandlerServiceImplTest {

    @Mock
    private ProductService productServiceMock;
    @Mock
    private PriceEntryService priceEntryServiceMock;
    @Mock
    private BrandService brandServiceMock;
    @Mock
    private CategoryService categoryServiceMock;

    @InjectMocks
    private ProductDataHandlerServiceImpl productDataHandlerService;

    private ProductPriceCsvRow testCsvRow;
    private Store testStore;
    private LocalDate testEntryDate;
    private Brand mockBrand;
    private Category mockCategory;
    private Product mockProduct;

    /**
     * Sets up common mock objects and test data before each test.
     */
    @BeforeEach
    void setUp() {
        testCsvRow = ProductPriceCsvRow.builder()
                .productId("CSV_P001")
                .productName("Lapte Bun")
                .productCategory("Lactate")
                .brand("Brand Bun")
                .packageQuantity(BigDecimal.ONE)
                .packageUnit("L")
                .price(new BigDecimal("5.99"))
                .currency("RON")
                .build();

        testStore = new Store("Magazin Test");
        testStore.setId(1L);
        testEntryDate = LocalDate.of(2024, 3, 15);

        mockBrand = new Brand("Brand Bun"); mockBrand.setId(10L);
        mockCategory = new Category("Lactate"); mockCategory.setId(20L);
        mockProduct = new Product("Lapte Bun", mockCategory, mockBrand); mockProduct.setId(30L);
    }

    /**
     * Tests that {@code processAndSaveProductPrice} correctly calls dependent services
     * to find/create brand, category, product, and then save/update the price entry.
     */
    @Test
    @DisplayName("processAndSaveProductPrice: successfully processes a valid CSV row when product exists and category matches")
    void processAndSaveProductPrice_validRow_callsServicesCorrectly() {    // Given: Mock behavior of dependent services
        when(brandServiceMock.findOrCreateBrand("Brand Bun")).thenReturn(mockBrand);
        when(categoryServiceMock.findOrCreateCategory("Lactate")).thenReturn(mockCategory);

        // Scenario: Product does not exist initially, so it will be created
        when(productServiceMock.findByNameIgnoreCaseAndBrand(testCsvRow.getProductName(), mockBrand))
                .thenReturn(Optional.of(mockProduct));


        // When
        productDataHandlerService.processAndSaveProductPrice(testCsvRow, testStore, testEntryDate);

        // Then: Verify interactions
        verify(brandServiceMock).findOrCreateBrand("Brand Bun");
        verify(categoryServiceMock).findOrCreateCategory("Lactate");
        verify(productServiceMock).findByNameIgnoreCaseAndBrand(testCsvRow.getProductName(), mockBrand);

        verify(productServiceMock, never()).save(any(Product.class));

        verify(priceEntryServiceMock).saveOrUpdatePriceEntry(
                eq(mockProduct),
                eq(testStore),
                eq(testEntryDate),
                eq("CSV_P001"),
                eq(new BigDecimal("5.99")),
                eq("RON"),
                eq(BigDecimal.ONE),
                eq(UnitOfMeasure.L)
        );
    }

    /**
     * Tests the handling of an unknown package unit from the CSV row.
     * Verifies that UnitOfMeasure.UNKNOWN is passed to the price entry service
     * and that a warning is logged (implicitly, by checking the passed enum).
     */
    @Test
    @DisplayName("processAndSaveProductPrice: handles unknown package unit")
    void processAndSaveProductPrice_handlesUnknownPackageUnit() {
        // Given
        ProductPriceCsvRow rowWithUnknownUnit = ProductPriceCsvRow.builder()
                .productId("CSV_P002")
                .productName("Produs Necunoscut")
                .productCategory("Diverse")
                .brand("Brand X")
                .packageQuantity(BigDecimal.TEN)
                .packageUnit("XYZ") // Unknown unit
                .price(new BigDecimal("1.00"))
                .currency("RON")
                .build();

        Brand brandX = new Brand("Brand X"); brandX.setId(11L);
        Category diverseCat = new Category("Diverse"); diverseCat.setId(21L);
        Product mockProductX = new Product("Produs Necunoscut", diverseCat, brandX); mockProductX.setId(31L);

        when(brandServiceMock.findOrCreateBrand("Brand X")).thenReturn(brandX);
        when(categoryServiceMock.findOrCreateCategory("Diverse")).thenReturn(diverseCat);
        when(productServiceMock.findByNameIgnoreCaseAndBrand(rowWithUnknownUnit.getProductName(), brandX))
                .thenReturn(Optional.empty());

        when(productServiceMock.save(any(Product.class))).thenReturn(mockProductX);

        // When
        productDataHandlerService.processAndSaveProductPrice(rowWithUnknownUnit, testStore, testEntryDate);

        // Then
        ArgumentCaptor<Product> productSaveCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productServiceMock, atLeastOnce()).save(productSaveCaptor.capture());

        verify(priceEntryServiceMock).saveOrUpdatePriceEntry(
                eq(mockProductX),
                eq(testStore),
                eq(testEntryDate),
                eq(rowWithUnknownUnit.getProductId()),
                eq(rowWithUnknownUnit.getPrice()),
                eq(rowWithUnknownUnit.getCurrency()),
                eq(rowWithUnknownUnit.getPackageQuantity()),
                eq(UnitOfMeasure.UNKNOWN)
        );
    }

}