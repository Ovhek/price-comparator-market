package com.alexcruceat.pricecomparatormarket;

import com.alexcruceat.pricecomparatormarket.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Price Comparator Market application.
 * This class initializes and runs the Spring Boot application.
 * It also enables the loading of custom application properties defined in {@link AppProperties}.
 */
@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
@EnableScheduling
public class PriceComparatorMarketApplication {

    /**
     * The main method that serves as the entry point for the Java application.
     * It delegates to Spring Boot's {@link SpringApplication#run} method to launch the application.
     *
     * @param args command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(PriceComparatorMarketApplication.class, args);
    }

}
