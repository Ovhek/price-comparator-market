package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.dto.ProductValueDTO;
import com.alexcruceat.pricecomparatormarket.mapper.PriceEntryMapper;
import com.alexcruceat.pricecomparatormarket.mapper.ProductMapper;
import com.alexcruceat.pricecomparatormarket.mapper.StoreMapper;
import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.ProductRepository;
import com.alexcruceat.pricecomparatormarket.service.PriceEntryService;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import com.alexcruceat.pricecomparatormarket.service.StoreService;
import com.alexcruceat.pricecomparatormarket.service.specification.ProductSpecification;
import com.alexcruceat.pricecomparatormarket.util.UnitConverterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link ProductService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PriceEntryService priceEntryService;
    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;

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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductValueDTO> findProductsWithValueAnalysis(
            String name, Long categoryId, Long brandId, Optional<Long> storeIdOpt,
            LocalDate referenceDate, Pageable pageable) {

        Assert.notNull(referenceDate, "Reference date cannot be null.");
        Assert.notNull(pageable, "Pageable cannot be null.");

        // 1. Create specification for initial product filtering
        Specification<Product> productSpec = ProductSpecification.byCriteria(name, categoryId, brandId, null);

        // 2. Fetch ALL products matching the specification.
        // We don't use pageable here for fetching products because sorting might be on DTO-calculated fields.
        List<Product> allMatchingProducts = productRepository.findAll(productSpec);

        List<ProductValueDTO> productValueDTOs = new ArrayList<>();

        // 3. For each product, find relevant price entries and create DTOs
        for (Product product : allMatchingProducts) {
            List<PriceEntry> relevantPriceEntries = new ArrayList<>();
            if (storeIdOpt.isPresent()) {
                Optional<PriceEntry> entryOpt = priceEntryService
                        .findFirstByProduct_IdAndStore_IdAndEntryDateLessThanEqualOrderByEntryDateDesc(
                                product.getId(), storeIdOpt.get(), referenceDate);
                entryOpt.ifPresent(relevantPriceEntries::add);
            } else {
                // If no specific store, try to get price on referenceDate from any store
                List<PriceEntry> entriesOnDate = priceEntryService.findByProductIdAndEntryDate(
                        product.getId(), referenceDate);
                if (!entriesOnDate.isEmpty()) {
                    relevantPriceEntries.addAll(entriesOnDate);
                } else {
                    // If no price on exact referenceDate, find latest before it for each store
                    List<PriceEntry> latestPerStore = priceEntryService.findLatestPriceEntriesPerStoreForProduct(
                            product.getId(), referenceDate);
                    if (!latestPerStore.isEmpty()) {
                        relevantPriceEntries.addAll(latestPerStore);
                    } else {
                        // Fallback: If still no entries, try the absolute latest for the product from any store
                        Optional<PriceEntry> latestAnyStore = priceEntryService
                                .findFirstByProductAndEntryDateLessThanEqualOrderByEntryDateDesc(product, referenceDate);
                        latestAnyStore.ifPresent(relevantPriceEntries::add);
                    }
                }
            }

            if (relevantPriceEntries.isEmpty()) {
                log.debug("No relevant price entry found for product ID {} (Name: '{}') around date {} (Store filter: {}). Adding with no price info.",
                        product.getId(), product.getName(), referenceDate, storeIdOpt.map(String::valueOf).orElse("Any"));
                productValueDTOs.add(ProductValueDTO.builder()
                        .product(productMapper.toDTO(product))
                        .normalizable(false)
                        .build());
            } else {
                for (PriceEntry entry : relevantPriceEntries) {
                    UnitConverterUtil.PricePerStandardUnit ppsu = UnitConverterUtil.calculatePricePerStandardUnit(
                            entry.getPrice(), entry.getPackageQuantity(), entry.getPackageUnit()
                    );

                    ProductValueDTO.ProductValueDTOBuilder builder = ProductValueDTO.builder()
                            .product(productMapper.toDTO(product))
                            .store(storeMapper.toDTO(entry.getStore()))
                            .currentPrice(entry.getPrice())
                            .currency(entry.getCurrency())
                            .packageQuantity(entry.getPackageQuantity())
                            .packageUnit(entry.getPackageUnit());

                    if (ppsu != null) {
                        builder.pricePerStandardUnit(ppsu.getPrice())
                                .standardUnit(ppsu.getUnit())
                                .normalizable(true);
                    } else {
                        builder.normalizable(false);
                    }
                    productValueDTOs.add(builder.build());
                }
            }
        }

        // 4. Sort the collected ProductValueDTOs
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Comparator<ProductValueDTO> finalComparator = buildProductValueDTOComparator(sort);
            if (finalComparator != null) {
                productValueDTOs.sort(finalComparator);
            }
        }

        // 5. Manual Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productValueDTOs.size());

        List<ProductValueDTO> pageContent;
        if (start > end || start >= productValueDTOs.size()) { // Check if start is out of bounds
            pageContent = List.of();
        } else {
            pageContent = productValueDTOs.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, productValueDTOs.size());
    }

    /**
     * Builds a comparator for {@link ProductValueDTO} based on a {@link Sort} object.
     * Supports sorting by "pricePerStandardUnit", "product.name", and "currentPrice".
     *
     * @param sort The {@link Sort} object specifying sorting criteria.
     * @return A {@link Comparator<ProductValueDTO>} or null if no supported sort properties are found.
     */
    private Comparator<ProductValueDTO> buildProductValueDTOComparator(Sort sort) {
        Comparator<ProductValueDTO> finalComparator = null;

        for (Sort.Order order : sort) {
            Comparator<ProductValueDTO> currentComparator = null;
            switch (order.getProperty()) {
                case "pricePerStandardUnit":
                    // Products with no pricePerStandardUnit (not normalizable or no price) should go last
                    currentComparator = Comparator.comparing(
                            ProductValueDTO::getPricePerStandardUnit,
                            Comparator.nullsLast(BigDecimal::compareTo)
                    );
                    break;
                case "product.name":
                    currentComparator = Comparator.comparing(
                            dto -> dto.getProduct().getName(),
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    );
                    break;
                case "currentPrice":
                    currentComparator = Comparator.comparing(
                            ProductValueDTO::getCurrentPrice,
                            Comparator.nullsLast(BigDecimal::compareTo)
                    );
                    break;
                // Add more cases for other sortable properties of ProductValueDTO if needed
                // e.g., "store.name", "product.brand.name"
                default:
                    log.warn("Unsupported sort property for ProductValueDTO: {}", order.getProperty());
                    break;
            }

            if (currentComparator != null) {
                if (order.isDescending()) {
                    currentComparator = currentComparator.reversed();
                }
                if (finalComparator == null) {
                    finalComparator = currentComparator;
                } else {
                    finalComparator = finalComparator.thenComparing(currentComparator);
                }
            }
        }
        return finalComparator;
    }
}
