package com.alexcruceat.pricecomparatormarket.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the application, prefixed with "app".
 * These properties are typically defined in {@code application.properties} or {@code application.yml}.
 * This class uses Spring's {@link ConfigurationProperties} mechanism for type-safe configuration.
 * Validation annotations (e.g., {@link NotBlank}) are used to ensure properties are correctly set.
 */
@Getter
@ConfigurationProperties(prefix = "app")
@Validated // Enables validation of the properties
public class AppProperties {

    private final Csv csv = new Csv();

    /**
     * Holds configuration settings related to CSV file processing.
     */
    @Setter
    @Getter
    public static class Csv {
        /**
         * The input path (directory) where CSV files are expected to be found.
         * This path must be configured and cannot be blank.
         */
        @NotBlank(message = "CSV input path cannot be blank")
        private String inputPath;

        /**
         * The output path (directory) where CSV files are expected to be moved.
         * This path must be configured and cannot be blank.
         */
        @NotBlank(message = "CSV processed path cannot be blank")
        private String processedPath;
    }
}