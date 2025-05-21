package com.alexcruceat.pricecomparatormarket.service.specification;

import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import com.alexcruceat.pricecomparatormarket.model.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating JPA {@link Specification} objects for {@link Product} entities.
 * Allows for dynamic query building based on filter criteria using string-based attribute names.
 */
public final class ProductSpecification {

    private ProductSpecification() {

    }

    /**
     * Creates a {@link Specification} for {@link Product} entities based on the provided filter criteria.
     *
     * @param name       Optional product name filter. If provided, products whose names contain this string
     *                   (case-insensitive) will be matched.
     * @param categoryId Optional category ID. If provided, only products belonging to this category will be matched.
     * @param brandId    Optional brand ID. If provided, only products belonging to this brand will be matched.
     * @param storeId    Optional store ID. If provided, only products that have at least one price entry
     *                   in the specified store will be matched. This implies a join to PriceEntry and Store entities.
     *                   The query will return distinct products.
     * @return A {@link Specification<Product>} that combines all provided filter criteria with an AND logic.
     *         Returns an empty specification (matching all products) if no criteria are provided.
     */
    public static Specification<Product> byCriteria(String name, Long categoryId, Long brandId, Long storeId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by product name (case-insensitive contains)
            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"
                ));
            }

            // Filter by category ID
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("category").get("id"), // Using strings "category" and "id"
                        categoryId
                ));
            }

            // Filter by brand ID
            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("brand").get("id"), // Using strings "brand" and "id"
                        brandId
                ));
            }

            // Filter by store ID (products available in this store)
            if (storeId != null) {
                assert query != null;
                query.distinct(true);
                Join<Product, PriceEntry> priceEntryJoin = root.join("priceEntries", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                        priceEntryJoin.get("store").get("id"),
                        storeId
                ));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
