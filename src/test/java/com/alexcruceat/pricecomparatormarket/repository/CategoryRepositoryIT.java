package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import com.alexcruceat.pricecomparatormarket.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category testCategory1;

    @BeforeEach
    void setUp() {
        testCategory1 = new Category("Lactate Test");
    }

    @Test
    @DisplayName("Save and retrieve category by ID")
    void whenSaveCategory_thenCanBeRetrievedById() {
        Category savedCategory = categoryRepository.save(testCategory1);
        entityManager.flush();
        entityManager.clear();

        Optional<Category> foundOpt = categoryRepository.findById(savedCategory.getId());

        assertThat(foundOpt).isPresent();
        Category found = foundOpt.get();
        assertThat(found.getName()).isEqualTo(testCategory1.getName());
        assertThat(found.getId()).isNotNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Find category by name (case-insensitive)")
    void whenFindByNameIgnoreCase_thenReturnCorrectCategory() {
        categoryRepository.save(testCategory1);
        entityManager.flush();
        entityManager.clear();

        Optional<Category> foundOpt = categoryRepository.findByNameIgnoreCase("lactate test");

        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getName()).isEqualTo(testCategory1.getName());
    }

    @Test
    @DisplayName("Saving category with null name throws ConstraintViolationException")
    void whenSaveCategoryWithNullName_thenThrowException() {
        Category categoryWithNullName = new Category(); // Name is null
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            categoryRepository.saveAndFlush(categoryWithNullName);
        });
    }

    @Test
    @DisplayName("Saving categories with duplicate names throws DataIntegrityViolationException")
    void whenSaveCategoriesWithDuplicateName_thenThrowException() {
        categoryRepository.saveAndFlush(testCategory1);
        Category duplicateCategory = new Category("Lactate Test");
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.saveAndFlush(duplicateCategory);
        });
    }
}