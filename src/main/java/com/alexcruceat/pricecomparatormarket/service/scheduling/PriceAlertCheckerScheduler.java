package com.alexcruceat.pricecomparatormarket.service.scheduling;

import com.alexcruceat.pricecomparatormarket.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for periodically checking and processing triggered price alerts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceAlertCheckerScheduler {

    private final PriceAlertService priceAlertService;

    /**
     * Periodically checks for triggered price alerts.
     * The schedule can be configured via application properties.
     * Example: Runs daily at 2 AM.
     * It's assumed that product prices are updated before this task runs (e.g., daily CSV ingestion).
     */
    @Scheduled(cron = "${app.price-alert.check.cron:0 0 2 * * ?}") // Default: Daily at 2 AM
    public void checkPriceAlerts() {
        log.info("Starting scheduled task: CheckPriceAlerts");
        try {
            priceAlertService.findAndProcessTriggeredAlerts();
            log.info("Finished scheduled task: CheckPriceAlerts successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled CheckPriceAlerts task:", e);
        }
    }
}
