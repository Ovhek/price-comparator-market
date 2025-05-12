package com.alexcruceat.pricecomparatormarket.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * Uses {@link OpenAPIDefinition} to define global API information such as title, version,
 * contact details, license, and server URLs.
 * This configuration is picked up by the springdoc-openapi library to generate
 * the API specification and Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Price Comparator Market API",
                version = "v1.0",
                description = "This API provides endpoints for managing and querying product prices " +
                        "and discounts from various supermarkets. It allows users to compare prices, " +
                        "find best deals, and track price history.",
                contact = @Contact(
                        name = "Alexandru Ioan Cruceat",
                        email = "alexandrucruceat@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8080"
                )
        }
)
public class OpenApiConfig {

}
