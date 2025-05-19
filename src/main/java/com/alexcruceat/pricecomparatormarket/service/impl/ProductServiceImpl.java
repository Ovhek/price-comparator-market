package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.Brand;
import com.alexcruceat.pricecomparatormarket.model.Category;
import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.repository.ProductRepository;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Implementation of {@link ProductService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Product findOrCreateProductAndUpdateCategory(String name, Brand brand, Category category) {
        Assert.hasText(name, "Product name must not be null or blank.");
        Assert.notNull(brand, "Brand must not be null.");
        Assert.notNull(category, "Category must not be null.");

        String trimmedName = name.trim();

        Product product = productRepository.findByNameIgnoreCaseAndBrand(trimmedName, brand)
                .orElseGet(() -> {
                    log.info("Product not found, creating new product: Name='{}', Brand='{}', Category='{}'",
                            trimmedName, brand.getName(), category.getName());
                    Product newProduct = new Product(trimmedName, category, brand);
                    return productRepository.save(newProduct);
                });

        // If product exists, check and update its category if different from provided
        // This assumes the provided category (e.g., from a CSV) is the most current master data for the product.
        if (product.getId() != null && !product.getCategory().getId().equals(category.getId())) {
            log.warn("Product '{}' (ID:{}) found with category '{}' (ID:{}) but current context indicates category '{}' (ID:{}). Updating product's category.",
                    product.getName(), product.getId(),
                    product.getCategory().getName(), product.getCategory().getId(),
                    category.getName(), category.getId());
            product.setCategory(category);
            product = productRepository.save(product); // Save and re-assign to get managed instance
        }
        return product;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Product> findById(Long id) {
        Assert.notNull(id, "Product ID must not be null.");
        return productRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<Product> findByNameIgnoreCaseAndBrand(String name, Brand brand) {
        Assert.hasText(name, "Product name must not be null or blank.");
        Assert.notNull(brand, "Brand must not be null.");
        return productRepository.findByNameIgnoreCaseAndBrand(name.trim(), brand);
    }


    /**
     * Saves a given product. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param product must not be {@literal null}.
     * @return the saved product will never be {@literal null}.
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Product save(Product product) {
        Assert.notNull(product, "Product to save must not be null.");
        log.debug("Saving product: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Product> findAll(Specification<Product> spec, Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null.");
        log.debug("Finding all products with specification and pageable: {}", pageable);
        return productRepository.findAll(spec, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<Product> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null.");
        log.debug("Finding all products with pageable: {}", pageable);
        return productRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        Assert.notNull(id, "Product ID for deletion must not be null.");
        log.info("Deleting product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent product with ID: {}", id);
            return;
        }
        productRepository.deleteById(id);
        log.info("Successfully deleted product with ID: {}", id);
    }
}
