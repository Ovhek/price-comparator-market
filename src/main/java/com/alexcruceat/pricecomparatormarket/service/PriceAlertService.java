package com.alexcruceat.pricecomparatormarket.service;

import com.alexcruceat.pricecomparatormarket.dto.PriceAlertDTO;
import com.alexcruceat.pricecomparatormarket.dto.PriceAlertRequestDTO;
import com.alexcruceat.pricecomparatormarket.model.UserPriceAlert; // For internal use

import java.util.List;

/**
 * Service interface for managing user price alerts.
 * Provides functionality to create, retrieve, deactivate, and check alerts.
 */
public interface PriceAlertService {

    /**
     * Creates a new price alert based on the user's request.
     *
     * @param request DTO containing the details for the new alert.
     * @return The created {@link PriceAlertDTO}.
     * @throws com.alexcruceat.pricecomparatormarket.exception.ResourceNotFoundException if the product or store (if specified) does not exist.
     * @throws com.alexcruceat.pricecomparatormarket.exception.InvalidInputException if an alert for the exact same product/store/target already exists and is active for the user.
     */
    PriceAlertDTO createAlert(PriceAlertRequestDTO request);

    /**
     * Retrieves all active price alerts for a given user.
     *
     * @param userId The ID of the user whose alerts are to be retrieved.
     * @return A list of {@link PriceAlertDTO}s.
     */
    List<PriceAlertDTO> getActiveAlertsForUser(String userId);

    /**
     * Retrieves all price alerts (active and inactive) for a given user.
     *
     * @param userId The ID of the user whose alerts are to be retrieved.
     * @return A list of {@link PriceAlertDTO}s.
     */
    List<PriceAlertDTO> getAllAlertsForUser(String userId);


    /**
     * Deactivates a specific price alert.
     *
     * @param alertId The ID of the alert to deactivate.
     * @param userId  The ID of the user who owns the alert (for authorization, though not strictly enforced here).
     * @throws com.alexcruceat.pricecomparatormarket.exception.ResourceNotFoundException if the alert is not found.
     * @throws com.alexcruceat.pricecomparatormarket.exception.InvalidInputException if the user is not authorized to deactivate (simplified check).
     */
    void deactivateAlert(Long alertId, String userId);

    /**
     * Finds all active alerts that have been met based on current product prices.
     * This method is typically called by a scheduled task.
     * For each triggered alert, it should ideally:
     * 1. Mark the alert as inactive (or ready for notification).
     * 2. Record the triggered price and store.
     * 3. (Out of scope for this challenge) Trigger an actual notification to the user.
     *
     * @return A list of {@link UserPriceAlert} entities that have been triggered and updated.
     */
    List<UserPriceAlert> findAndProcessTriggeredAlerts();
}
