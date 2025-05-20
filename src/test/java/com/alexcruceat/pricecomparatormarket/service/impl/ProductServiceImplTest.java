package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Category;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductServiceImpl}.
 * Mocks {@link ProductRepository} to test product-related business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepositoryMock;

    @InjectMocks
    private ProductServiceImpl productService;

    private Brand testBrand;
    private Category testCategory;
    private Category anotherCategory;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        testBrand = new Brand("TestBrand");
        testBrand.setId(1L);
        testCategory = new Category("TestCategory");
        testCategory.setId(1L);
        anotherCategory = new Category("AnotherCategory");
        anotherCategory.setId(2L);
    }

    /**
     * Tests findOrCreateProductAndUpdateCategory when the product already exists and its category matches the provided one.
     * Expects the existing product to be returned and no save operation to be called.
     */
    @Test
    @DisplayName("findOrCreateProduct: product exists, category matches, returns existing")
    void findOrCreateProduct_productExists_categoryMatches_returnsExisting() {
        Product existingProduct = new Product("TestProduct", testCategory, testBrand);
        existingProduct.setId(1L);
        when(productRepositoryMock.findByNameIgnoreCaseAndBrand("TestProduct", testBrand)).thenReturn(Optional.of(existingProduct));

        Product result = productService.findOrCreateProductAndUpdateCategory("TestProduct", testBrand, testCategory);

        assertThat(result).isEqualTo(existingProduct);
        verify(productRepositoryMock, never()).save(any(Product.class));
    }

    /**
     * Tests findOrCreateProductAndUpdateCategory when the product already exists but its category differs.
     * Expects the product's category to be updated, the product saved, and the updated product returned.
     */
    @Test
    @DisplayName("findOrCreateProduct: product exists, category differs, updates and returns product")
    void findOrCreateProduct_productExists_categoryDiffers_updatesAndReturnsProduct() {
        Product existingProduct = new Product("TestProduct", testCategory, testBrand);
        existingProduct.setId(1L);

        when(productRepositoryMock.findByNameIgnoreCaseAndBrand("TestProduct", testBrand)).thenReturn(Optional.of(existingProduct));
        // When save is called for update, return the modified product
        when(productRepositoryMock.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.findOrCreateProductAndUpdateCategory("TestProduct", testBrand, anotherCategory);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCategory()).isEqualTo(anotherCategory); // Category should be updated
        verify(productRepositoryMock).save(existingProduct); // Verify save was called
    }

    /**
     * Tests findOrCreateProductAndUpdateCategory when the product does not exist.
     * Expects a new product to be created with the provided details, saved, and returned.
     */
    @Test
    @DisplayName("findOrCreateProduct: product not exists, creates and returns new product")
    void findOrCreateProduct_productNotExists_createsAndReturnsNewProduct() {
        String productName = "NewProduct";
        Product savedProduct = new Product(productName, testCategory, testBrand);
        savedProduct.setId(2L); // Simulate ID after save

        when(productRepositoryMock.findByNameIgnoreCaseAndBrand(productName, testBrand)).thenReturn(Optional.empty());
        when(productRepositoryMock.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertThat(p.getName()).isEqualTo(productName);
            assertThat(p.getBrand()).isEqualTo(testBrand);
            assertThat(p.getCategory()).isEqualTo(testCategory);
            p.setId(2L); // Simulate ID generation
            return p;
        });

        Product result = productService.findOrCreateProductAndUpdateCategory(productName, testBrand, testCategory);

        assertThat(result.getName()).isEqualTo(productName);
        assertThat(result.getBrand()).isEqualTo(testBrand);
        assertThat(result.getCategory()).isEqualTo(testCategory);
        assertThat(result.getId()).isEqualTo(2L);
        verify(productRepositoryMock).save(any(Product.class));
    }

    /**
     * Tests that findById returns an Optional containing the product if found.
     */
    @Test
    @DisplayName("findById: product exists, returns optional of product")
    void findById_productExists_returnsOptionalOfProduct() {
        Product product = new Product("TestProduct", testCategory, testBrand);
        product.setId(1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findById(1L);

        assertThat(result).isPresent().contains(product);
    }

    /**
     * Tests that findById returns an empty Optional if the product is not found.
     */
    @Test
    @DisplayName("findById: product not exists, returns empty optional")
    void findById_productNotExists_returnsEmptyOptional() {
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findById(1L);

        assertThat(result).isNotPresent();
    }

    /**
     * Tests that findByNameIgnoreCaseAndBrand returns product when found.
     */
    @Test
    @DisplayName("findByNameIgnoreCaseAndBrand: returns product when found")
    void findByNameIgnoreCaseAndBrand_returnsProduct_whenFound() {
        Product product = new Product("Test Product", testCategory, testBrand);
        when(productRepositoryMock.findByNameIgnoreCaseAndBrand("test product", testBrand)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findByNameIgnoreCaseAndBrand("test product", testBrand);

        assertThat(result).isPresent().contains(product);
    }


    /**
     * Tests that saving a product delegates to the repository's save method.
     */
    @Test
    @DisplayName("save: delegates to repository save")
    void save_delegatesToRepositorySave() {
        Product productToSave = new Product("ToSave", testCategory, testBrand);
        Product savedProduct = new Product("ToSave", testCategory, testBrand);
        savedProduct.setId(1L);
        when(productRepositoryMock.save(productToSave)).thenReturn(savedProduct);

        Product result = productService.save(productToSave);

        assertThat(result).isEqualTo(savedProduct);
        verify(productRepositoryMock).save(productToSave);
    }

    /**
     * Tests that findAll with Pageable delegates to the repository and returns a Page.
     */
    @Test
    @DisplayName("findAll with Pageable: delegates to repository and returns page")
    void findAll_withPageable_delegatesAndReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = Collections.singletonList(new Product("PagedProduct", testCategory, testBrand));
        Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());
        when(productRepositoryMock.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productService.findAll(pageable);

        assertThat(result).isEqualTo(productPage);
        verify(productRepositoryMock).findAll(pageable);
    }

    /**
     * Tests that findAll with Specification and Pageable delegates to the repository.
     */
    @Test
    @DisplayName("findAll with Specification and Pageable: delegates to repository")
    void findAll_withSpecAndPageable_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Product> spec = (root, query, cb) -> cb.equal(root.get("name"), "Test");
        List<Product> productList = Collections.emptyList();
        Page<Product> productPage = new PageImpl<>(productList, pageable, 0);
        when(productRepositoryMock.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        Page<Product> result = productService.findAll(spec, pageable);

        assertThat(result).isEqualTo(productPage);
        verify(productRepositoryMock).findAll(spec, pageable);
    }

    /**
     * Tests that deleteById delegates to repository when product exists.
     */
    @Test
    @DisplayName("deleteById: delegates to repository when product exists")
    void deleteById_delegatesToRepository_whenProductExists() {
        long productId = 1L;
        when(productRepositoryMock.existsById(productId)).thenReturn(true);
        doNothing().when(productRepositoryMock).deleteById(productId);

        productService.deleteById(productId);

        verify(productRepositoryMock).existsById(productId);
        verify(productRepositoryMock).deleteById(productId);
    }

    /**
     * Tests that deleteById does not call repository delete when product does not exist.
     */
    @Test
    @DisplayName("deleteById: does nothing if product does not exist")
    void deleteById_doesNothing_ifProductNotExists() {
        long productId = 1L;
        when(productRepositoryMock.existsById(productId)).thenReturn(false);

        productService.deleteById(productId);

        verify(productRepositoryMock).existsById(productId);
        verify(productRepositoryMock, never()).deleteById(productId);
    }

    /**
     * Tests argument validation for findOrCreateProductAndUpdateCategory.
     */
    @Test
    @DisplayName("findOrCreateProduct: throws IllegalArgumentException for null arguments")
    void findOrCreateProduct_nullArguments_throwsException() {
        assertThatThrownBy(() -> productService.findOrCreateProductAndUpdateCategory(null, testBrand, testCategory))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> productService.findOrCreateProductAndUpdateCategory("Name", null, testCategory))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> productService.findOrCreateProductAndUpdateCategory("Name", testBrand, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
