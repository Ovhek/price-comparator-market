package com.alexcruceat.pricecomparatormarket.controller.api.v1;

import com.alexcruceat.pricecomparatormarket.dto.PriceAlertDTO;
import com.alexcruceat.pricecomparatormarket.dto.PriceAlertRequestDTO;
import com.alexcruceat.pricecomparatormarket.service.PriceAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user price alerts.
 * Allows users to create, view, and deactivate their price alerts.
 */
@RestController
@RequestMapping("/api/v1/price-alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Price Alerts API", description = "Endpoints for managing user price alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    /**
     * Creates a new price alert for a user.
     *
     * @param request DTO containing the details for the new alert.
     * @return A {@link ResponseEntity} containing the created {@link PriceAlertDTO} and HTTP status 201 (Created).
     */
    @Operation(summary = "Create a new price alert",
            description = "Allows a user to set up a new price alert for a specific product, target price, and optionally a specific store.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Price alert created successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceAlertDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., missing fields, product not found, duplicate active alert).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.alexcruceat.pricecomparatormarket.exception.GlobalApiExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or Store specified in the request not found.")
    })
    @PostMapping
    public ResponseEntity<PriceAlertDTO> createPriceAlert(
            @Valid @RequestBody PriceAlertRequestDTO request) {
        log.info("Received request to create price alert: {}", request);
        PriceAlertDTO createdAlert = priceAlertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlert);
    }

    /**
     * Retrieves all active price alerts for a specific user.
     *
     * @param userId The ID of the user whose active alerts are to be retrieved.
     * @return A {@link ResponseEntity} containing a list of {@link PriceAlertDTO}s.
     */
    @Operation(summary = "Get active price alerts for a user",
            description = "Retrieves a list of all currently active price alerts set by the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active alerts.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PriceAlertDTO.class))))
    })
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<PriceAlertDTO>> getActiveUserAlerts(
            @Parameter(description = "ID of the user.", required = true, example = "user123")
            @PathVariable String userId) {
        log.info("Received request to get active alerts for user ID: {}", userId);
        List<PriceAlertDTO> alerts = priceAlertService.getActiveAlertsForUser(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Retrieves all price alerts (both active and inactive) for a specific user.
     *
     * @param userId The ID of the user whose alerts are to be retrieved.
     * @return A {@link ResponseEntity} containing a list of {@link PriceAlertDTO}s.
     */
    @Operation(summary = "Get all price alerts for a user",
            description = "Retrieves a list of all price alerts (active and inactive) set by the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all alerts for the user.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PriceAlertDTO.class))))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PriceAlertDTO>> getAllUserAlerts(
            @Parameter(description = "ID of the user.", required = true, example = "user123")
            @PathVariable String userId) {
        log.info("Received request to get all alerts for user ID: {}", userId);
        List<PriceAlertDTO> alerts = priceAlertService.getAllAlertsForUser(userId);
        return ResponseEntity.ok(alerts);
    }


    /**
     * Deactivates a specific price alert.
     * Once deactivated, it will no longer be checked against current prices.
     *
     * @param alertId The ID of the price alert to deactivate.
     * @param userId  The ID of the user attempting to deactivate the alert (for basic authorization).
     *                In a real system, this would come from an authenticated principal.
     * @return A {@link ResponseEntity} with HTTP status 204 (No Content) if successful.
     */
    @Operation(summary = "Deactivate a price alert",
            description = "Marks a specific price alert as inactive. Requires the user ID who owns the alert.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Price alert deactivated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., user not authorized)."),
            @ApiResponse(responseCode = "404", description = "Price alert not found with the specified ID.")
    })
    @PutMapping("/{alertId}/deactivate") // Using PUT for state change, could also be DELETE if it means permanent removal
    public ResponseEntity<Void> deactivatePriceAlert(
            @Parameter(description = "ID of the price alert to deactivate.", required = true, example = "1")
            @PathVariable Long alertId,
            @Parameter(description = "ID of the user who owns the alert. For this demo, pass it as a query param.", required = true, example = "user123")
            @RequestParam String userId) {
        log.info("Received request to deactivate alert ID: {} for user ID: {}", alertId, userId);
        priceAlertService.deactivateAlert(alertId, userId);
        return ResponseEntity.noContent().build();
    }
}
