package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import com.alexcruceat.pricecomparatormarket.model.Brand;
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
class BrandRepositoryIT extends AbstractIntegrationTest {
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Brand testBrand1;

    @BeforeEach
    void setUp() {
        testBrand1 = new Brand("Zuzu Test");
    }

    @Test
    @DisplayName("Save and retrieve brand by ID")
    void whenSaveBrand_thenCanBeRetrievedById() {
        Brand savedBrand = brandRepository.save(testBrand1);
        entityManager.flush();
        entityManager.clear();

        Optional<Brand> foundOpt = brandRepository.findById(savedBrand.getId());

        assertThat(foundOpt).isPresent();
        Brand found = foundOpt.get();
        assertThat(found.getName()).isEqualTo(testBrand1.getName());
        assertThat(found.getId()).isNotNull();
    }

    @Test
    @DisplayName("Find brand by name (case-insensitive)")
    void whenFindByNameIgnoreCase_thenReturnCorrectBrand() {
        brandRepository.save(testBrand1);
        entityManager.flush();
        entityManager.clear();

        Optional<Brand> foundOpt = brandRepository.findByNameIgnoreCase("zuzu test");

        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getName()).isEqualTo(testBrand1.getName());
    }

    @Test
    @DisplayName("Saving brand with null name throws ConstraintViolationException")
    void whenSaveBrandWithNullName_thenThrowException() {
        Brand brandWithNullName = new Brand();
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            brandRepository.saveAndFlush(brandWithNullName);
        });
    }

    @Test
    @DisplayName("Saving brands with duplicate names throws DataIntegrityViolationException")
    void whenSaveBrandsWithDuplicateName_thenThrowException() {
        brandRepository.saveAndFlush(testBrand1);
        Brand duplicateBrand = new Brand("Zuzu Test");
        assertThrows(DataIntegrityViolationException.class, () -> {
            brandRepository.saveAndFlush(duplicateBrand);
        });
    }
}