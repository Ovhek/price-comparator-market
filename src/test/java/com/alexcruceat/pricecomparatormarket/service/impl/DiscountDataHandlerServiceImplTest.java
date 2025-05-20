package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.service.BrandService;
import com.alexcruceat.pricecomparatormarket.service.DiscountService;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import com.alexcruceat.pricecomparatormarket.service.dto.DiscountCsvRow;
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
 * Unit tests for {@link DiscountDataHandlerServiceImpl}.
 * Mocks dependent services (BrandService, ProductService, DiscountService)
 * to verify the orchestration logic of processing discount data.
 */
@ExtendWith(MockitoExtension.class)
class DiscountDataHandlerServiceImplTest {

    @Mock
    private ProductService productServiceMock;
    @Mock
    private DiscountService discountServiceMock; // This is the service whose methods we are verifying
    @Mock
    private BrandService brandServiceMock;

    @InjectMocks
    private DiscountDataHandlerServiceImpl discountDataHandlerService;

    private DiscountCsvRow testCsvRow;
    private Store testStore;
    private LocalDate testRecordedAtDate;
    private Brand mockBrand;
    private Product mockProduct;
    private Discount mockExistingDiscount;
    private Discount mockSavedDiscount;


    /**
     * Sets up common mock objects and test data before each test.
     */
    @BeforeEach
    void setUp() {
        testCsvRow = DiscountCsvRow.builder()
                .productId("CSV_D001")
                .productName("Lapte Discountat")
                .brand("Brand Discount")
                .packageQuantity(BigDecimal.ONE)
                .packageUnit("L")
                .productCategory("Lactate") // Category from discount CSV
                .fromDate(LocalDate.of(2024, 3, 1))
                .toDate(LocalDate.of(2024, 3, 10))
                .percentageOfDiscount(15)
                .build();

        testStore = new Store("Magazin Discount");
        testStore.setId(1L);
        testRecordedAtDate = LocalDate.of(2024, 3, 1);

        mockBrand = new Brand("Brand Discount"); mockBrand.setId(10L);
        Category mockCategory = new Category("Lactate"); mockCategory.setId(20L);
        mockProduct = new Product("Lapte Discountat", mockCategory, mockBrand); mockProduct.setId(30L);

        mockExistingDiscount = new Discount(mockProduct, testStore, 10, testCsvRow.getFromDate(), testCsvRow.getToDate().minusDays(1), testRecordedAtDate.minusDays(1), testCsvRow.getPackageQuantity(), UnitOfMeasure.L);
        mockExistingDiscount.setId(50L);

        mockSavedDiscount = new Discount(mockProduct, testStore, testCsvRow.getPercentageOfDiscount(), testCsvRow.getFromDate(), testCsvRow.getToDate(), testRecordedAtDate, testCsvRow.getPackageQuantity(), UnitOfMeasure.L);
        mockSavedDiscount.setId(51L); // Simulate ID after save
    }

    /**
     * Tests that {@code processAndSaveDiscount} correctly calls dependent services
     * when the product for the discount is found and a new discount is created.
     * It assumes the actual DiscountDataHandlerServiceImpl calls findDiscountByNaturalKey and then save.
     */
    @Test
    @DisplayName("processAndSaveDiscount: product found, new discount, calls find and save on DiscountService")
    void processAndSaveDiscount_productFound_newDiscount_callsFindAndSave() {
        // Given
        when(brandServiceMock.findOrCreateBrand("Brand Discount")).thenReturn(mockBrand);
        when(productServiceMock.findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand))
                .thenReturn(Optional.of(mockProduct));

        // Simulate DiscountService: findDiscountByNaturalKey returns empty (so a new one will be saved)
        when(discountServiceMock.findDiscountByNaturalKey(
                mockProduct,
                testStore,
                testCsvRow.getFromDate(),
                testCsvRow.getPackageQuantity(),
                UnitOfMeasure.L
        )).thenReturn(Optional.empty());

        when(discountServiceMock.save(any(Discount.class))).thenReturn(mockSavedDiscount);


        // When
        discountDataHandlerService.processAndSaveDiscount(testCsvRow, testStore, testRecordedAtDate);

        // Then
        verify(brandServiceMock).findOrCreateBrand("Brand Discount");
        verify(productServiceMock).findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand);

        // Verify interaction with DiscountService
        verify(discountServiceMock).findDiscountByNaturalKey(
                eq(mockProduct),
                eq(testStore),
                eq(testCsvRow.getFromDate()),
                eq(testCsvRow.getPackageQuantity()),
                eq(UnitOfMeasure.L)
        );

        ArgumentCaptor<Discount> discountCaptor = ArgumentCaptor.forClass(Discount.class);
        verify(discountServiceMock).save(discountCaptor.capture()); // Verify save was called

        Discount discountToSave = discountCaptor.getValue();
        assertThat(discountToSave.getProduct()).isEqualTo(mockProduct);
        assertThat(discountToSave.getStore()).isEqualTo(testStore);
        assertThat(discountToSave.getPercentage()).isEqualTo(testCsvRow.getPercentageOfDiscount());
        assertThat(discountToSave.getFromDate()).isEqualTo(testCsvRow.getFromDate());
        assertThat(discountToSave.getToDate()).isEqualTo(testCsvRow.getToDate());
        assertThat(discountToSave.getRecordedAtDate()).isEqualTo(testRecordedAtDate);
        assertThat(discountToSave.getPackageQuantity()).isEqualTo(testCsvRow.getPackageQuantity());
        assertThat(discountToSave.getPackageUnit()).isEqualTo(UnitOfMeasure.L);

    }


    /**
     * Tests that {@code processAndSaveDiscount} correctly calls dependent services
     * when the product for the discount is found and an existing discount is updated.
     * This test assumes your DiscountDataHandlerServiceImpl calls findDiscountByNaturalKey,
     * then if a discount is found and needs updating, it calls an update method or save again.
     * For simplicity, let's assume it calls save() and the save method handles updates.
     */
    @Test
    @DisplayName("processAndSaveDiscount: product found, existing discount updated, calls find and save on DiscountService")
    void processAndSaveDiscount_productFound_existingDiscountUpdated_callsFindAndSave() {
        // Given
        when(brandServiceMock.findOrCreateBrand("Brand Discount")).thenReturn(mockBrand);
        when(productServiceMock.findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand))
                .thenReturn(Optional.of(mockProduct));

        // Simulate DiscountService: findDiscountByNaturalKey returns an existing discount
        // that is older, so it should be updated.
        Discount olderExistingDiscount = new Discount(mockProduct, testStore, 10,
                testCsvRow.getFromDate(), testCsvRow.getToDate().minusDays(5), // Older toDate
                testRecordedAtDate.minusDays(5), // Older recordedAtDate
                testCsvRow.getPackageQuantity(), UnitOfMeasure.L);
        olderExistingDiscount.setId(50L);

        when(discountServiceMock.findDiscountByNaturalKey(
                mockProduct,
                testStore,
                testCsvRow.getFromDate(),
                testCsvRow.getPackageQuantity(),
                UnitOfMeasure.L
        )).thenReturn(Optional.of(olderExistingDiscount));

        // Simulate DiscountService: save (which handles update) returns the updated discount
        when(discountServiceMock.save(any(Discount.class))).thenAnswer(invocation -> {
            Discount arg = invocation.getArgument(0);
            // Simulate that the save method updates the fields of the passed object (or returns a new one with updated fields)
            assertThat(arg.getId()).isEqualTo(olderExistingDiscount.getId()); // ID should be the same for update
            arg.setPercentage(testCsvRow.getPercentageOfDiscount());
            arg.setToDate(testCsvRow.getToDate());
            arg.setRecordedAtDate(testRecordedAtDate);
            return arg;
        });

        // When
        discountDataHandlerService.processAndSaveDiscount(testCsvRow, testStore, testRecordedAtDate);

        // Then
        verify(brandServiceMock).findOrCreateBrand("Brand Discount");
        verify(productServiceMock).findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand);

        verify(discountServiceMock).findDiscountByNaturalKey(
                eq(mockProduct),
                eq(testStore),
                eq(testCsvRow.getFromDate()),
                eq(testCsvRow.getPackageQuantity()),
                eq(UnitOfMeasure.L)
        );

        ArgumentCaptor<Discount> discountCaptor = ArgumentCaptor.forClass(Discount.class);
        verify(discountServiceMock).save(discountCaptor.capture()); // Verify save (for update) was called

        Discount discountToUpdate = discountCaptor.getValue();
        assertThat(discountToUpdate.getId()).isEqualTo(olderExistingDiscount.getId());
        assertThat(discountToUpdate.getPercentage()).isEqualTo(testCsvRow.getPercentageOfDiscount());
        assertThat(discountToUpdate.getToDate()).isEqualTo(testCsvRow.getToDate());
        assertThat(discountToUpdate.getRecordedAtDate()).isEqualTo(testRecordedAtDate);
    }


    /**
     * Tests that {@code processAndSaveDiscount} skips processing and does not call
     * discountService save/update methods if the product for the discount is not found.
     */
    @Test
    @DisplayName("processAndSaveDiscount: product not found, skips discount processing")
    void processAndSaveDiscount_productNotFound_skipsProcessing() {
        // Given
        when(brandServiceMock.findOrCreateBrand("Brand Discount")).thenReturn(mockBrand);
        when(productServiceMock.findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand))
                .thenReturn(Optional.empty()); // Product not found

        // When
        discountDataHandlerService.processAndSaveDiscount(testCsvRow, testStore, testRecordedAtDate);

        // Then
        verify(brandServiceMock).findOrCreateBrand("Brand Discount");
        verify(productServiceMock).findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand);

        // Crucially, no interaction with findDiscountByNaturalKey or save/update on discountServiceMock
        verify(discountServiceMock, never()).findDiscountByNaturalKey(any(), any(), any(), any(), any());
        verify(discountServiceMock, never()).save(any(Discount.class));
        // verify(discountServiceMock, never()).update(any(Discount.class)); // if you have a separate update
    }

    /**
     * Tests handling of an unknown package unit from the discount CSV row.
     * Assumes it finds an existing discount to update, to also test the UNKNOWN unit propagation.
     */
    @Test
    @DisplayName("processAndSaveDiscount: handles unknown package unit and updates existing")
    void processAndSaveDiscount_handlesUnknownPackageUnit() {
        // Given
        DiscountCsvRow rowWithUnknownUnit = testCsvRow.toBuilder().packageUnit("XYZ").build();
        // mockProduct and mockBrand are already set up

        when(brandServiceMock.findOrCreateBrand("Brand Discount")).thenReturn(mockBrand);
        when(productServiceMock.findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand))
                .thenReturn(Optional.of(mockProduct));

        // Simulate DiscountService: findDiscountByNaturalKey returns an existing discount with UNKNOWN unit
        // Note: The lookup key for findDiscountByNaturalKey would now use UNKNOWN
        Discount existingDiscountWithUnknownUnit = new Discount(mockProduct, testStore, 10,
                rowWithUnknownUnit.getFromDate(), rowWithUnknownUnit.getToDate().minusDays(5),
                testRecordedAtDate.minusDays(5), // Older recordedAtDate
                rowWithUnknownUnit.getPackageQuantity(), UnitOfMeasure.UNKNOWN); // Existing has UNKNOWN
        existingDiscountWithUnknownUnit.setId(60L);


        when(discountServiceMock.findDiscountByNaturalKey(
                mockProduct,
                testStore,
                rowWithUnknownUnit.getFromDate(),
                rowWithUnknownUnit.getPackageQuantity(),
                UnitOfMeasure.UNKNOWN // Lookup key uses UNKNOWN
        )).thenReturn(Optional.of(existingDiscountWithUnknownUnit));

        // Simulate DiscountService: save (which handles update)
        when(discountServiceMock.save(any(Discount.class))).thenAnswer(invocation -> {
            Discount arg = invocation.getArgument(0);
            assertThat(arg.getId()).isEqualTo(existingDiscountWithUnknownUnit.getId());
            arg.setPercentage(rowWithUnknownUnit.getPercentageOfDiscount());
            arg.setToDate(rowWithUnknownUnit.getToDate());
            arg.setRecordedAtDate(testRecordedAtDate); // Updated recordedAtDate
            // Package unit remains UNKNOWN
            return arg;
        });

        // When
        discountDataHandlerService.processAndSaveDiscount(rowWithUnknownUnit, testStore, testRecordedAtDate);

        // Then
        verify(brandServiceMock).findOrCreateBrand("Brand Discount");
        verify(productServiceMock).findByNameIgnoreCaseAndBrand("Lapte Discountat", mockBrand);

        verify(discountServiceMock).findDiscountByNaturalKey(
                eq(mockProduct),
                eq(testStore),
                eq(rowWithUnknownUnit.getFromDate()),
                eq(rowWithUnknownUnit.getPackageQuantity()),
                eq(UnitOfMeasure.UNKNOWN) // Verify UNKNOWN was used in the lookup
        );

        ArgumentCaptor<Discount> discountCaptor = ArgumentCaptor.forClass(Discount.class);
        verify(discountServiceMock).save(discountCaptor.capture());

        Discount savedOrUpdatedDiscount = discountCaptor.getValue();
        assertThat(savedOrUpdatedDiscount.getProduct()).isEqualTo(mockProduct);
        assertThat(savedOrUpdatedDiscount.getStore()).isEqualTo(testStore);
        assertThat(savedOrUpdatedDiscount.getPackageUnit()).isEqualTo(UnitOfMeasure.UNKNOWN); // Verify UNKNOWN is on the saved object
        assertThat(savedOrUpdatedDiscount.getPercentage()).isEqualTo(rowWithUnknownUnit.getPercentageOfDiscount());
        assertThat(savedOrUpdatedDiscount.getId()).isEqualTo(existingDiscountWithUnknownUnit.getId());
    }
}
