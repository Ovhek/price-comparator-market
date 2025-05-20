package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Category;
import com.alexcruceat.pricecomparatormarket.repository.CategoryRepository;
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
 * Unit tests for {@link CategoryServiceImpl}.
 * Verifies the logic for finding or creating categories, mocking repository interactions.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepositoryMock;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    /**
     * Tests that an existing category is returned if found by its name (case-insensitive).
     * Verifies that the repository's save method is not called.
     */
    @Test
    @DisplayName("findOrCreateCategory: when category exists, returns existing category")
    void findOrCreateCategory_whenCategoryExists_returnsExistingCategory() {
        // Given
        String categoryName = "Lactate";
        String searchName = "lactate"; // Test case-insensitivity
        Category existingCategory = new Category(categoryName);
        existingCategory.setId(1L); // Simulate persisted entity

        when(categoryRepositoryMock.findByNameIgnoreCase(searchName)).thenReturn(Optional.of(existingCategory));

        // When
        Category result = categoryService.findOrCreateCategory(searchName);

        // Then
        assertThat(result).isEqualTo(existingCategory);
        assertThat(result.getName()).isEqualTo(categoryName);
        verify(categoryRepositoryMock).findByNameIgnoreCase(searchName);
        verify(categoryRepositoryMock, never()).save(any(Category.class));
    }

    /**
     * Tests that a new category is created, saved, and returned if it does not exist by name.
     * Verifies that the repository's save method is called with the correct category name.
     */
    @Test
    @DisplayName("findOrCreateCategory: when category not exists, creates and returns new category")
    void findOrCreateCategory_whenCategoryNotExists_createsAndReturnsNewCategory() {
        // Given
        String categoryName = "Panificatie";
        Long expectedId = 2L;

        // This is what the repository's save method will be mocked to return
        Category categoryReturnedBySave = new Category(categoryName);
        categoryReturnedBySave.setId(expectedId);

        when(categoryRepositoryMock.findByNameIgnoreCase(categoryName)).thenReturn(Optional.empty());

        when(categoryRepositoryMock.save(any(Category.class))).thenAnswer(invocation -> {
            Category catArg = invocation.getArgument(0);
            assertThat(catArg.getName()).isEqualTo(categoryName); // Verify argument to save
            // Simulate ID generation and return a representation of the saved entity
            Category returnedFromMock = new Category(catArg.getName());
            returnedFromMock.setId(expectedId); // Use the defined expectedId
            if (catArg.getCreatedAt() != null) returnedFromMock.setCreatedAt(catArg.getCreatedAt());
            if (catArg.getUpdatedAt() != null) returnedFromMock.setUpdatedAt(catArg.getUpdatedAt());
            return returnedFromMock;
        });

        // When
        Category result = categoryService.findOrCreateCategory(categoryName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(categoryName);
        assertThat(result.getId()).isEqualTo(expectedId);
        verify(categoryRepositoryMock).findByNameIgnoreCase(categoryName);
        verify(categoryRepositoryMock).save(any(Category.class)); // Could also use ArgumentCaptor here
    }

    /**
     * Tests that findOrCreateCategory trims whitespace from the input name
     * before searching or creating.
     */
    @Test
    @DisplayName("findOrCreateCategory: trims input name before processing")
    void findOrCreateCategory_trimsInputName() {
        // Given
        String categoryNameWithSpace = "  Fructe  ";
        String trimmedCategoryName = "Fructe";
        Category existingCategory = new Category(trimmedCategoryName);
        existingCategory.setId(3L);

        when(categoryRepositoryMock.findByNameIgnoreCase(trimmedCategoryName)).thenReturn(Optional.of(existingCategory));

        // When
        Category result = categoryService.findOrCreateCategory(categoryNameWithSpace);

        // Then
        assertThat(result).isEqualTo(existingCategory);
        verify(categoryRepositoryMock).findByNameIgnoreCase(trimmedCategoryName); // Verify trimmed name was used
        verify(categoryRepositoryMock, never()).save(any(Category.class));
    }


    /**
     * Tests that an {@link IllegalArgumentException} is thrown if a blank name is provided.
     */
    @Test
    @DisplayName("findOrCreateCategory: with blank name, throws IllegalArgumentException")
    void findOrCreateCategory_withBlankName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> categoryService.findOrCreateCategory("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name must not be null or blank");
    }

    /**
     * Tests that an {@link IllegalArgumentException} is thrown if a null name is provided.
     */
    @Test
    @DisplayName("findOrCreateCategory: with null name, throws IllegalArgumentException")
    void findOrCreateCategory_withNullName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> categoryService.findOrCreateCategory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name must not be null or blank");
    }
}