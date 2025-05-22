package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.PriceAlertRepository; // New Repository
import com.alexcruceat.pricecomparatormarket.dto.PriceAlertDTO;
import com.alexcruceat.pricecomparatormarket.dto.PriceAlertRequestDTO;
import com.alexcruceat.pricecomparatormarket.exception.InvalidInputException;
import com.alexcruceat.pricecomparatormarket.exception.ResourceNotFoundException;
import com.alexcruceat.pricecomparatormarket.mapper.PriceAlertMapper;
import com.alexcruceat.pricecomparatormarket.service.PriceAlertService;
import com.alexcruceat.pricecomparatormarket.service.PriceEntryService;
import com.alexcruceat.pricecomparatormarket.service.ProductService;
import com.alexcruceat.pricecomparatormarket.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link PriceAlertService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductService productService;
    private final StoreService storeService; // To fetch Store if storeId is provided
    private final PriceEntryService priceEntrySer; // To check current prices
    private final PriceAlertMapper priceAlertMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PriceAlertDTO createAlert(PriceAlertRequestDTO request) {
        Assert.notNull(request, "PriceAlertRequestDTO cannot be null.");
        Assert.hasText(request.getUserId(), "User ID cannot be blank.");
        Assert.notNull(request.getProductId(), "Product ID cannot be null.");
        Assert.notNull(request.getTargetPrice(), "Target price cannot be null.");

        Product product = productService.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Store store = null;
        if (request.getStoreId() != null) {
            store = storeService.findStoreById(request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + request.getStoreId()));
        }

        //Check for existing active alert for the same user, product, store, and target price
        Optional<UserPriceAlert> existingAlert = priceAlertRepository
                .findByUserIdAndProductAndStoreAndTargetPriceAndIsActiveTrue(
                        request.getUserId(), product, store, request.getTargetPrice());

        if (existingAlert.isPresent()) {
            log.warn("User {} already has an active alert for product ID {} with target price {} (Store ID: {}).",
                    request.getUserId(), request.getProductId(), request.getTargetPrice(), request.getStoreId());
            throw new InvalidInputException("An active alert with these exact criteria already exists.");
        }

        UserPriceAlert newAlert = new UserPriceAlert(
                request.getUserId(),
                product,
                request.getTargetPrice(),
                store
        );
        // isActive is true by default in constructor

        UserPriceAlert savedAlert = priceAlertRepository.save(newAlert);
        log.info("Created new price alert ID {} for user {}, product ID {}",
                savedAlert.getId(), savedAlert.getUserId(), savedAlert.getProduct().getId());
        return priceAlertMapper.toDTO(savedAlert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PriceAlertDTO> getActiveAlertsForUser(String userId) {
        Assert.hasText(userId, "User ID cannot be blank.");
        List<UserPriceAlert> alerts = priceAlertRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        return priceAlertMapper.toDTOList(alerts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PriceAlertDTO> getAllAlertsForUser(String userId) {
        Assert.hasText(userId, "User ID cannot be blank.");
        List<UserPriceAlert> alerts = priceAlertRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return priceAlertMapper.toDTOList(alerts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deactivateAlert(Long alertId, String userId) {
        Assert.notNull(alertId, "Alert ID cannot be null.");
        Assert.hasText(userId, "User ID cannot be blank for deactivation.");

        UserPriceAlert alert = priceAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Price alert not found with ID: " + alertId));

        // Simple authorization check
        if (!alert.getUserId().equals(userId)) {
            log.warn("User {} attempted to deactivate alert ID {} owned by user {}.", userId, alertId, alert.getUserId());
            throw new InvalidInputException("You are not authorized to deactivate this alert.");
        }

        if (!alert.getIsActive()) {
            log.info("Alert ID {} for user {} is already inactive.", alertId, userId);
            return; // Or throw an exception if trying to deactivate an already inactive one is an error
        }

        alert.setIsActive(false);
        priceAlertRepository.save(alert);
        log.info("Deactivated price alert ID {} for user {}.", alertId, userId);
    }

    /**
     * {@inheritDoc}
     * This method iterates through all active alerts and checks current prices.
     * It's designed to be called by a scheduler.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED) // Each run is a single transaction
    public List<UserPriceAlert> findAndProcessTriggeredAlerts() {
        LocalDate today = LocalDate.now();
        List<UserPriceAlert> activeAlerts = priceAlertRepository.findAllByIsActiveTrue();
        List<UserPriceAlert> triggeredAndUpdatedAlerts = new ArrayList<>();

        log.info("Checking {} active price alerts against current prices for date: {}", activeAlerts.size(), today);

        for (UserPriceAlert alert : activeAlerts) {
            Product product = alert.getProduct();
            List<PriceEntry> currentPriceEntries;

            if (alert.getStore() != null) {
                // Alert is for a specific store
                // Find latest price entry for this product in the specific store on or before today
                Optional<PriceEntry> entryOpt = priceEntrySer
                        .findFirstByProductAndStoreAndEntryDateLessThanEqualOrderByEntryDateDesc(
                                product, alert.getStore(), today);
                currentPriceEntries = entryOpt.map(List::of).orElse(List.of());
            } else {
                // Alert is for any store, find latest price in each store
                currentPriceEntries = priceEntrySer.findLatestPriceEntriesPerStoreForProduct(product.getId(), today);
            }

            PriceEntry bestOfferMeetingAlert = null;
            for (PriceEntry entry : currentPriceEntries) {
                // Check if price is at or below target
                if (entry.getPrice().compareTo(alert.getTargetPrice()) <= 0) {
                    if (bestOfferMeetingAlert == null || entry.getPrice().compareTo(bestOfferMeetingAlert.getPrice()) < 0) {
                        bestOfferMeetingAlert = entry; // Found a price meeting the alert, or a better one
                    }
                }
            }

            if (bestOfferMeetingAlert != null) {
                log.info("Price alert ID {} triggered for user {}! Product: '{}' (ID: {}), Target Price: {}, Found Price: {} at Store: '{}' (ID: {}).",
                        alert.getId(), alert.getUserId(), product.getName(), product.getId(),
                        alert.getTargetPrice(), bestOfferMeetingAlert.getPrice(),
                        bestOfferMeetingAlert.getStore().getName(), bestOfferMeetingAlert.getStore().getId());

                alert.setIsActive(false); // Deactivate alert after triggering
                alert.setNotifiedAt(LocalDateTime.now());
                alert.setTriggeredPrice(bestOfferMeetingAlert.getPrice());
                alert.setTriggeredStore(bestOfferMeetingAlert.getStore());
                priceAlertRepository.save(alert);
                triggeredAndUpdatedAlerts.add(alert);

                // notification job here (email, push, etc.)
            }
        }
        log.info("Processed price alerts. {} alerts were triggered and updated.", triggeredAndUpdatedAlerts.size());
        return triggeredAndUpdatedAlerts;
    }
}
