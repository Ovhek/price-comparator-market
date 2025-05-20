package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.repository.BrandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BrandServiceImpl}.
 * Verifies the logic for finding or creating brands, mocking repository interactions.
 */
@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepositoryMock;

    @InjectMocks
    private BrandServiceImpl brandService;

    /**
     * Tests that an existing brand is returned if found by its name (case-insensitive).
     * Verifies that the repository's save method is not called.
     */
    @Test
    @DisplayName("findOrCreateBrand: when brand exists, returns existing brand")
    void findOrCreateBrand_whenBrandExists_returnsExistingBrand() {
        // Given
        String brandName = "Zuzu";
        String searchName = "zuzu"; // Test case-insensitivity
        Brand existingBrand = new Brand(brandName);
        existingBrand.setId(1L);

        when(brandRepositoryMock.findByNameIgnoreCase(searchName)).thenReturn(Optional.of(existingBrand));

        // When
        Brand result = brandService.findOrCreateBrand(searchName);

        // Then
        assertThat(result).isEqualTo(existingBrand);
        assertThat(result.getName()).isEqualTo(brandName);
        verify(brandRepositoryMock).findByNameIgnoreCase(searchName);
        verify(brandRepositoryMock, never()).save(any(Brand.class));
    }

    /**
     * Tests that a new brand is created, saved, and returned if it does not exist by name.
     * Verifies that the repository's save method is called with the correct brand name.
     */
    @Test
    @DisplayName("findOrCreateBrand: when brand not exists, creates and returns new brand")
    void findOrCreateBrand_whenBrandNotExists_createsAndReturnsNewBrand() {
        // Given
        String brandName = "Pilos";
        Brand savedBrand = new Brand(brandName);
        savedBrand.setId(2L); // Simulate ID after save

        when(brandRepositoryMock.findByNameIgnoreCase(brandName)).thenReturn(Optional.empty());
        when(brandRepositoryMock.save(any(Brand.class))).thenAnswer(invocation -> {
            Brand b = invocation.getArgument(0);
            assertThat(b.getName()).isEqualTo(brandName);
            b.setId(2L); // Simulate ID generation
            return b;
        });

        // When
        Brand result = brandService.findOrCreateBrand(brandName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(brandName);
        assertThat(result.getId()).isEqualTo(2L);
        verify(brandRepositoryMock).findByNameIgnoreCase(brandName);
        verify(brandRepositoryMock).save(any(Brand.class));
    }

    /**
     * Tests that findOrCreateBrand trims whitespace from the input name
     * before searching or creating.
     */
    @Test
    @DisplayName("findOrCreateBrand: trims input name before processing")
    void findOrCreateBrand_trimsInputName() {
        // Given
        String brandNameWithSpace = "  Napolact  ";
        String trimmedBrandName = "Napolact";
        Brand existingBrand = new Brand(trimmedBrandName);
        existingBrand.setId(3L);

        when(brandRepositoryMock.findByNameIgnoreCase(trimmedBrandName)).thenReturn(Optional.of(existingBrand));

        // When
        Brand result = brandService.findOrCreateBrand(brandNameWithSpace);

        // Then
        assertThat(result).isEqualTo(existingBrand);
        verify(brandRepositoryMock).findByNameIgnoreCase(trimmedBrandName);
        verify(brandRepositoryMock, never()).save(any(Brand.class));
    }

    /**
     * Tests that an {@link IllegalArgumentException} is thrown if a blank name is provided.
     */
    @Test
    @DisplayName("findOrCreateBrand: with blank name, throws IllegalArgumentException")
    void findOrCreateBrand_withBlankName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> brandService.findOrCreateBrand("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Brand name must not be null or blank");
    }

    /**
     * Tests that an {@link IllegalArgumentException} is thrown if a null name is provided.
     */
    @Test
    @DisplayName("findOrCreateBrand: with null name, throws IllegalArgumentException")
    void findOrCreateBrand_withNullName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> brandService.findOrCreateBrand(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Brand name must not be null or blank");
    }
}

