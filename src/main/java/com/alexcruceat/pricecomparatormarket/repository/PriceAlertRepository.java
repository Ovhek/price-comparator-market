package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.model.Product;
import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.model.UserPriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserPriceAlert} entities.
 */
@Repository
public interface PriceAlertRepository extends JpaRepository<UserPriceAlert, Long> {

    /**
     * Finds all active price alerts for a given user, ordered by creation date descending.
     *
     * @param userId The ID of the user.
     * @return A list of active {@link UserPriceAlert}s.
     */
    List<UserPriceAlert> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(String userId);

    /**
     * Finds all price alerts (both active and inactive) for a given user, ordered by creation date descending.
     *
     * @param userId The ID of the user.
     * @return A list of all {@link UserPriceAlert}s for the user.
     */
    List<UserPriceAlert> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Finds all currently active price alerts in the system.
     * Used by the alert checking mechanism.
     *
     * @return A list of all active {@link UserPriceAlert}s.
     */
    List<UserPriceAlert> findAllByIsActiveTrue();

    /**
     * Finds an active alert for a specific user, product, optional store, and target price.
     * Used to prevent duplicate active alerts with the exact same criteria.
     *
     * @param userId      The user's ID.
     * @param product     The product.
     * @param store       The store (can be null if alert is for any store).
     * @param targetPrice The target price.
     * @return An {@link Optional} containing the existing active alert if found.
     */
    Optional<UserPriceAlert> findByUserIdAndProductAndStoreAndTargetPriceAndIsActiveTrue(
            String userId, Product product, Store store, BigDecimal targetPrice
    );
}
