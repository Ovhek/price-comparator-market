package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Category;
import com.alexcruceat.pricecomparatormarket.model.Product;
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
class ProductRepositoryIT extends AbstractIntegrationTest {    @Autowired
private ProductRepository productRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Brand testBrand;
    private Category testCategory;
    private Product product1;

    @BeforeEach
    void setUp() {
        // Ensure foreign key constraints are met by saving dependencies first
        testBrand = brandRepository.saveAndFlush(new Brand("Pilos Test"));
        testCategory = categoryRepository.saveAndFlush(new Category("Dairy Test"));
        product1 = new Product("Iaurt Test", testCategory, testBrand);
    }

    @Test
    @DisplayName("Save and retrieve product with associations")
    void whenSaveProduct_thenCanBeRetrievedWithAssociations() {
        Product savedProduct = productRepository.saveAndFlush(product1);
        entityManager.clear();

        Optional<Product> foundOpt = productRepository.findById(savedProduct.getId());

        assertThat(foundOpt).isPresent();
        Product found = foundOpt.get();
        assertThat(found.getName()).isEqualTo(product1.getName());
        assertThat(found.getBrand().getId()).isEqualTo(testBrand.getId());
        assertThat(found.getCategory().getId()).isEqualTo(testCategory.getId());
    }

    @Test
    @DisplayName("Find product by name (case-insensitive) and brand")
    void whenFindByNameIgnoreCaseAndBrand_thenReturnProduct() {
        Product savedProduct = productRepository.saveAndFlush(product1);
        entityManager.clear();

        Brand persistedTestBrand = brandRepository.findById(testBrand.getId()).orElseThrow();

        Optional<Product> foundOpt = productRepository.findByNameIgnoreCaseAndBrand(
                savedProduct.getName().toLowerCase(),
                persistedTestBrand
        );
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getName()).isEqualTo(savedProduct.getName());

        Brand otherBrand = new Brand("Other Brand");
        Brand persistedOtherBrand = brandRepository.saveAndFlush(otherBrand);
        entityManager.clear();

        Optional<Product> notFoundOpt = productRepository.findByNameIgnoreCaseAndBrand(
                savedProduct.getName().toLowerCase(),
                persistedOtherBrand
        );
        assertThat(notFoundOpt).isNotPresent();
    }

    @Test
    @DisplayName("Saving product with null name throws ConstraintViolationException")
    void whenSaveProductWithNullName_thenThrowException() {
        Product p = new Product(null, testCategory, testBrand);
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            productRepository.saveAndFlush(p);
        });
    }

    @Test
    @DisplayName("Saving product with null category throws ConstraintViolationException")
    void whenSaveProductWithNullCategory_thenThrowException() {
        Product p = new Product("Valid Name", null, testBrand);
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            productRepository.saveAndFlush(p); // @NotNull on category field
        });
    }
    @Test
    @DisplayName("Saving product with null brand throws ConstraintViolationException")
    void whenSaveProductWithNullBrand_thenThrowException() {
        Product p = new Product("Valid Name", testCategory, null);
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            productRepository.saveAndFlush(p); // @NotNull on brand field
        });
    }


    @Test
    @DisplayName("Saving products with duplicate name and brand throws DataIntegrityViolationException")
    void whenSaveProductWithDuplicateNameAndBrand_thenThrowException() {
        productRepository.saveAndFlush(product1); // Save "Iaurt Test" with testBrand

        Product duplicateProduct = new Product("Iaurt Test", testCategory, testBrand);
        // Should violate uk_product_name_brand
        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.saveAndFlush(duplicateProduct);
        });
    }

    @Test
    @DisplayName("Saving products with same name but different brand is allowed")
    void whenSaveProductWithSameNameDifferentBrand_thenAllowed() {
        productRepository.saveAndFlush(product1);

        Brand anotherBrand = brandRepository.saveAndFlush(new Brand("Zuzu Test"));
        Product productWithDifferentBrand = new Product("Iaurt Test", testCategory, anotherBrand);

        Product savedProduct = productRepository.saveAndFlush(productWithDifferentBrand);
        assertThat(savedProduct.getId()).isNotNull();
    }
}