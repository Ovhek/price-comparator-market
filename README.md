# Price Comparator – Market API

## Overview

The Price Comparator – Market API is a backend system designed to ingest product price and discount data from CSV files for multiple supermarket chains (e.g., Lidl, Kaufland, Profi). It stores this information and exposes RESTful API endpoints, allowing users and potentially a frontend application to:

*   Compare prices of everyday grocery items.
*   Track price changes over time for products, categories, and brands.
*   Find the best current deals and newly added discounts.
*   Analyze product value based on price per standard unit.
*   Optimize shopping baskets for cost savings.
*   Set up and receive custom price alerts.

This project was developed as part of a coding challenge, emphasizing robust backend design, clean code, and practical feature implementation.

## Core Features Implemented

*   **Data Ingestion:** Parses product price and discount data from CSV files, identifying store and date from filenames.
*   **Product Listing & Details:** Provides endpoints to list products with filtering (name, category, brand, store availability) and retrieve detailed information for individual products.
*   **Value-per-Unit Analysis:** Calculates and exposes price per standard unit (e.g., per KG/Litre) to help identify best buys.
*   **Discount Tracking:**
    *   **Best Discounts:** Lists products with the highest current percentage discounts.
    *   **New Discounts:** Lists discounts recently added to the system.
*   **Price History & Trends:**
    *   Tracks historical prices for individual products.
    *   Aggregates and presents price trends for product categories and brands.
    *   Data is filterable by store and date range.
*   **Custom Price Alerts:**
    *   Allows users to set target prices for specific products (optionally in specific stores).
    *   A scheduled task periodically checks if alert conditions are met.
    *   Endpoints to create, view, and deactivate alerts.
*   **Shopping Basket Optimization:** Optimizes a user's list of desired products to find the most cost-effective purchasing plan, potentially splitting items across multiple stores.

## Technical Stack

*   **Language:** Java 21
*   **Framework:** Spring Boot 3.4.5
*   **Database:** MariaDB
*   **ORM:** Hibernate/JPA (Spring Data JPA)
*   **Build Tool:** Apache Maven (with Maven Wrapper)
*   **Containerization:** Docker, Docker Compose
*   **CSV Processing:** FastCSV
*   **API Documentation:** Springdoc OpenAPI (Swagger UI)
*   **Testing:** JUnit 5, Mockito, Testcontainers

## Project Structure

The project follows a standard Spring Boot multi-module structure:

*   `com.alexcruceat.pricecomparatormarket`
    *   `config`: Contains Spring configuration classes, including `AppProperties` for custom application settings, and `OpenApiConfig` for Swagger documentation setup.
    *   `controller.api.v1`: Houses REST API controllers that handle incoming HTTP requests and delegate to service layer.
    *   `dto`: Data Transfer Objects used for API request payloads and response bodies, ensuring a clean separation between API contracts and internal domain models.
    *   `exception`: Defines custom exceptions (e.g., `ResourceNotFoundException`, `CsvProcessingException`) and a `GlobalApiExceptionHandler` for standardized error responses.
    *   `mapper`: MapStruct interfaces for automated, type-safe mapping between DTOs and JPA entities.
    *   `model`: JPA entities representing the core domain concepts like `Product`, `Store`, `PriceEntry`, `Discount`, `UserPriceAlert`, etc. These are mapped to database tables.
    *   `repository`: Spring Data JPA repository interfaces for database operations on entities.
    *   `service`: Defines interfaces for the business logic layer.
        *   `service.dto`: DTOs specific to the service layer, primarily for CSV row representations (`ProductPriceCsvRow`, `DiscountCsvRow`).
        *   `service.impl`: Concrete implementations of the service interfaces, containing the core business logic.
        *   `service.scheduling`: Contains scheduled tasks, such as the `PriceAlertCheckerScheduler` for checking price alerts.
        *   `service.specification`: JPA Specifications (`ProductSpecification`) for building dynamic database queries based on filter criteria.
    *   `util`: Utility classes providing common functionalities like `ApiConstants` for API path management, `CsvFilenameParser` for interpreting CSV filenames, and `UnitConverterUtil` for normalizing product quantities and prices.

## Setup and Installation

### Prerequisites

*   Java JDK 21 or later.
*   Apache Maven 3.6+ (or use the included Maven Wrapper `./mvnw`).
*   Docker and Docker Compose.

### Building the Application

1.  Clone the repository:
    ```bash
    git clone <repository-url>
    cd price-comparator-market
    ```
2.  Build the application using the Maven Wrapper:
    *   On Linux/macOS:
        ```bash
        ./mvnw clean package
        ```
    *   On Windows:
        ```bash
        .\mvnw.cmd clean package
        ```
    This will compile the code, run tests, and create an executable JAR file in the `target/` directory.

### Running the Application

**Using Docker Compose (Recommended):**

This is the easiest way to run the application along with its MariaDB database.

1.  Ensure Docker Desktop (or Docker Engine with Docker Compose plugin) is running.
2.  From the project root directory, run:
    ```bash
    docker-compose up --build
    ```
    *   `--build`: Forces Docker to rebuild the application image if there are code changes.
    *   This command will:
        *   Pull/build the MariaDB image.
        *   Start a MariaDB container named `price-comparator-db`.
        *   Build the Spring Boot application Docker image using the provided `Dockerfile`.
        *   Start the application container named `price-comparator-app`.
        *   The application will be accessible at `http://localhost:8080`.
        *   The Swagger UI for API documentation will be available at `http://localhost:8080/swagger-ui.html`.
        *   The MariaDB database will be accessible on `localhost:3307` (mapped from container port 3306).

**Stopping the Application (Docker Compose):**

*   Press `Ctrl+C` in the terminal where `docker-compose up` is running.
*   To remove the containers and network (but preserve the database volume):
    ```bash
    docker-compose down
    ```
*   To remove containers, network, AND the database volume (data will be lost):
    ```bash
    docker-compose down -v
    ```

## Configuration

The application uses Spring Boot's externalized configuration mechanism.

*   **`application.properties` (in `src/main/resources`):** Contains default configurations.
*   **`application-dev.properties` (in `src/main/resources`):** Contains configurations specific to the `dev` profile, primarily used when running with Docker Compose. These are often overridden by environment variables in `docker-compose.yml`.
*   **`application-test.properties` (in `src/test/resources`):** Contains configurations for the `test` profile, used during integration tests with Testcontainers.

**Key Configurable Properties (primarily via `docker-compose.yml` environment variables for `dev` profile or `AppProperties.java`):**

*   `SPRING_PROFILES_ACTIVE`: Sets the active Spring profile (e.g., `dev`).
*   `SPRING_DATASOURCE_URL`: JDBC URL for the database (e.g., `jdbc:mariadb://mariadb:3306/price_comparator_db` for Docker).
*   `SPRING_DATASOURCE_USERNAME`: Database username.
*   `SPRING_DATASOURCE_PASSWORD`: Database password.
*   `APP_CSV_INPUT_PATH` (via `app.csv.input-path`): Directory where the application looks for new CSV files to ingest (e.g., `/data/data-input` inside the Docker container, mapped to `./data/data-input` on the host).
*   `APP_CSV_PROCESSED_PATH` (via `app.csv.processed-path`): Directory where successfully processed CSV files are moved (e.g., `/data/processed` inside the Docker container, mapped to `./data/processed` on the host).
*   `app.csv.ingestion.cron`: Cron expression for the CSV ingestion scheduler. Default: `0 0 */1 * * ?` (every hour).
*   `app.price-alert.check.cron`: Cron expression for the price alert checking scheduler. Default: `0 0 2 * * ?` (daily at 2 AM).

## Database Schema

The application uses a relational schema in MariaDB, managed by Hibernate/JPA. Key entities include:

*   **`Store`**: Represents a supermarket chain (e.g., Lidl, Kaufland).
    *   `id` (PK), `name` (UNIQUE), `created_at`, `updated_at`.
*   **`Brand`**: Represents a product brand (e.g., Zuzu, Pilos).
    *   `id` (PK), `name` (UNIQUE), `created_at`, `updated_at`.
*   **`Category`**: Represents a product category (e.g., Lactate, Panificatie).
    *   `id` (PK), `name` (UNIQUE), `created_at`, `updated_at`.
*   **`Product`**: The core product item.
    *   `id` (PK), `name`, `category_id` (FK to Category), `brand_id` (FK to Brand), `created_at`, `updated_at`.
    *   Unique constraint on (`name`, `brand_id`).
*   **`PriceEntry`**: Records the price of a product at a specific store on a given date for a particular package configuration.
    *   `id` (PK), `product_id` (FK to Product), `store_id` (FK to Store), `entry_date`, `price`, `currency`, `package_quantity`, `package_unit`, `store_product_id` (store's internal ID for the product from CSV), `created_at`, `updated_at`.
    *   Unique constraint on (`product_id`, `store_id`, `entry_date`).
*   **`Discount`**: Represents a discount offered for a product at a store.
    *   `id` (PK), `product_id` (FK to Product), `store_id` (FK to Store), `percentage`, `from_date`, `to_date`, `recorded_at_date` (date from CSV filename), `package_quantity`, `package_unit`, `created_at`, `updated_at`.
    *   Unique constraint on (`product_id`, `store_id`, `from_date`, `package_quantity`, `package_unit`).
*   **`UserPriceAlert`**: Stores user-defined price alerts.
    *   `id` (PK), `user_id` (String), `product_id` (FK to Product), `target_price`, `store_id` (FK to Store, nullable), `is_active`, `notified_at`, `triggered_price`, `triggered_store_id` (FK to Store, nullable), `created_at`, `updated_at`.

**Key Relationships (Simplified ERD Description):**

The notation `A --(Many-to-One)--> B` means many instances of entity `A` can be related to one instance of entity `B`. For example, many `Product` records can point to one `Brand` record. "Optional" indicates that the foreign key on entity `A` referencing entity `B` can be null.

*   `Product` --(Many-to-One)--> `Brand`
*   `Product` --(Many-to-One)--> `Category`
*   `PriceEntry` --(Many-to-One)--> `Product`
*   `PriceEntry` --(Many-to-One)--> `Store`
*   `Discount` --(Many-to-One)--> `Product`
*   `Discount` --(Many-to-One)--> `Store`
*   `UserPriceAlert` --(Many-to-One)--> `Product`
*   `UserPriceAlert` --(Many-to-One, Optional via `store_id`)--> `Store` (A specific store can be optionally targeted for the alert)
*   `UserPriceAlert` --(Many-to-One, Optional via `triggered_store_id`)--> `Store` (The store where the alert condition was met is optionally recorded)
## Data Ingestion Process

The application automatically ingests product price and discount data from CSV files placed in a configured input directory.

1.  **Discovery:**
    *   A scheduled task (`CsvDataIngestionServiceImpl`) periodically scans the directory specified by `app.csv.input-path` (default: `data/data-input` relative to the project root when run via Docker Compose).
    *   It looks for files ending with `.csv`.
    *   The filename is parsed by `CsvFilenameParser` to determine the `storeName`, `date` (entry date for prices, recorded date for discounts), and `fileType` (product price or discount).
        *   Product Price CSV Naming: `storename_yyyy-MM-dd.csv` (e.g., `lidl_2025-05-01.csv`)
        *   Discount CSV Naming: `storename_discounts_yyyy-MM-dd.csv` (e.g., `lidl_discounts_2025-05-01.csv`)

2.  **Parsing:**
    *   The `CsvFileReaderService` reads each identified CSV file.
    *   It skips the header row and parses each subsequent row into either a `ProductPriceCsvRow` or `DiscountCsvRow` DTO.
    *   Basic validation (field count, data type conversion) is performed at this stage. Rows with parsing errors are logged and skipped.

3.  **Processing & Persistence:**
    *   The `CsvDataIngestionServiceImpl` orchestrates the processing of each file. Each file is processed within its own new database transaction.
    *   **For Product Price CSVs:**
        *   `ProductDataHandlerService` is invoked for each `ProductPriceCsvRow`.
        *   It finds or creates `Store`, `Brand`, and `Category` entities based on the CSV data.
        *   It then finds or creates the `Product` entity. If an existing product is found but its category differs from the CSV, the product's category is updated.
        *   Finally, it saves or updates a `PriceEntry` for the product, store, date, and package details from the CSV.
    *   **For Discount CSVs:**
        *   `DiscountDataHandlerService` is invoked for each `DiscountCsvRow`.
        *   It finds the corresponding `Product` (based on name and brand from the discount CSV) and `Store`.
        *   It then saves or updates a `Discount` entity. Updates occur if an existing discount for the same product, store, start date, and package details is found, and the new CSV record is more recent (based on `recordedAtDate`) or has different discount details.

4.  **File Archival:**
    *   After a CSV file is successfully processed (i.e., its dedicated transaction completes without a critical error), it is moved from the input directory (`data/data-input`) to the processed directory, as configured by `app.csv.processed-path` (default: `data/processed`).
    *   If a critical error occurs during the processing of a specific file (e.g., an unrecoverable issue that causes its transaction to roll back), that file is **not** moved and remains in the input directory for investigation.

## API Endpoints Documentation (Swagger UI)

Comprehensive and interactive API documentation is generated by Springdoc OpenAPI and is available at:

*   **Swagger UI:** `http://localhost:8080/swagger-ui.html`
*   **OpenAPI Spec (JSON):** `http://localhost:8080/v3/api-docs`

Below are examples for a few key API endpoints.

**(Note: For brevity, only a selection is detailed here. Refer to Swagger UI for all endpoints.)**

---

### 1. List Products

*   **Endpoint:** `GET /api/v1/products`
*   **Description:** Retrieves a paginated list of products. Supports filtering by name, category ID, brand ID, and store ID. Sorting and pagination are supported.
*   **Query Parameters:**
    *   `name` (String, optional): Filter by product name (partial, case-insensitive). Example: `lapte`
    *   `categoryId` (Long, optional): Filter by category ID. Example: `1`
    *   `brandId` (Long, optional): Filter by brand ID. Example: `5`
    *   `storeId` (Long, optional): Filter by store ID (products available in this store). Example: `2`
    *   `page` (int, optional, default: 0): Page number (0-indexed).
    *   `size` (int, optional, default: 20): Number of items per page.
    *   `sort` (String, optional, default: `name,asc`): Sorting criteria (e.g., `name,desc`, `category.name,asc`).
*   **Successful Response (200 OK):**
    ```json
    {
      "content": [
        {
          "id": 1,
          "name": "Lapte Zuzu 1.5% Grăsime",
          "category": {
            "id": 1,
            "name": "Lactate"
          },
          "brand": {
            "id": 1,
            "name": "Zuzu"
          }
        }
        // ... more products
      ],
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 50,
      "totalPages": 3,
      "last": false,
      "first": true,
      "numberOfElements": 20
    }
    ```
*   **Error Responses:**
    *   `400 Bad Request`: Invalid parameter format.
        ```json
        {
          "timestamp": "2024-05-21T18:30:00.123456",
          "status": 400,
          "error": "Bad Request",
          "message": "Validation Failed: page: Page index must not be less than zero",
          "path": "/api/v1/products"
        }
        ```
*   **cURL Example:**
    ```bash
    curl -X GET "http://localhost:8080/api/v1/products?name=lapte&categoryId=1&page=0&size=5&sort=name,desc"
    ```

---

### 2. Get Product Details by ID

*   **Endpoint:** `GET /api/v1/products/{id}`
*   **Description:** Retrieves details for a specific product by its ID.
*   **Path Variable:**
    *   `id` (Long, required): The ID of the product. Example: `1`
*   **Successful Response (200 OK):**
    ```json
    {
      "id": 1,
      "name": "Lapte Zuzu 1.5% Grăsime",
      "category": {
        "id": 1,
        "name": "Lactate"
      },
      "brand": {
        "id": 1,
        "name": "Zuzu"
      }
    }
    ```
*   **Error Responses:**
    *   `404 Not Found`: Product with the given ID not found.
        ```json
        {
          "timestamp": "2024-05-21T18:32:00.123456",
          "status": 404,
          "error": "Not Found",
          "message": "Product not found with ID: 999",
          "path": "/api/v1/products/999"
        }
        ```
*   **cURL Example:**
    ```bash
    curl -X GET "http://localhost:8080/api/v1/products/1"
    ```

---

### 3. Get Best Active Discounts

*   **Endpoint:** `GET /api/v1/discounts/best`
*   **Description:** Retrieves a paginated list of products with the highest currently active discount percentages.
*   **Query Parameters:**
    *   `referenceDateOptional` (String, optional, format: `yyyy-MM-dd`): Date to consider for active discounts. Defaults to today. Example: `2025-05-08`
    *   `page` (int, optional, default: 0): Page number.
    *   `size` (int, optional, default: 10): Number of items per page.
    *   `sort` (String, optional, default: `percentage,desc`): Sorting.
*   **Successful Response (200 OK):**
    ```json
    {
      "content": [
        {
          "product": {
            "id": 38,
            "name": "Detergent lichid",
            "category": { "id": 12, "name": "Produse de menaj" },
            "brand": { "id": 13, "name": "Ariel" }
          },
          "store": { "id": 1, "name": "Kaufland" },
          "discountPercentage": 22,
          "originalPrice": 50.50,
          "discountedPrice": 39.39,
          "packageQuantity": 2.5,
          "packageUnit": "L",
          "discountEndDate": "2025-05-07"
        }
        // ... more discounted products
      ],
      // ... pagination details ...
    }
    ```
*   **cURL Example:**
    ```bash
    curl -X GET "http://localhost:8080/api/v1/discounts/best?referenceDateOptional=2025-05-01&size=5"
    ```

---

### 4. Get Individual Product Price History

*   **Endpoint:** `GET /api/v1/price-history/product/{productId}`
*   **Description:** Retrieves the price history for an individual product, filterable by store and date range.
*   **Path Variable:**
    *   `productId` (Long, required): ID of the product. Example: `1`
*   **Query Parameters:**
    *   `storeId` (Long, optional): Filter by store ID.
    *   `startDate` (String, optional, format: `yyyy-MM-dd`): Start of date range.
    *   `endDate` (String, optional, format: `yyyy-MM-dd`): End of date range.
*   **Successful Response (200 OK):**
    ```json
    {
      "entityType": "PRODUCT",
      "entityId": 1,
      "entityName": "Lapte Zuzu",
      "entityDetails": {
        "id": 1,
        "name": "Lapte Zuzu",
        "category": { "id": 1, "name": "Lactate" },
        "brand": { "id": 1, "name": "Zuzu" }
      },
      "trendPoints": [
        {
          "date": "2025-05-01",
          "value": 10.10,
          "valueUnitDescription": "RON",
          "storeName": "Kaufland"
        },
        {
          "date": "2025-05-08",
          "value": 10.00,
          "valueUnitDescription": "RON",
          "storeName": "Kaufland"
        }
        // ... more price points
      ],
      "filtersApplied": {
        "productId": "1",
        "storeId": "1",
        "startDate": "2025-05-01",
        "endDate": "2025-05-10"
      }
    }
    ```
*   **cURL Example:**
    ```bash
    curl -X GET "http://localhost:8080/api/v1/price-history/product/1?storeId=1&startDate=2025-05-01&endDate=2025-05-10"
    ```

---

### 5. Create Price Alert

*   **Endpoint:** `POST /api/v1/price-alerts`
*   **Description:** Creates a new price alert for a user.
*   **Request Body (application/json):**
    ```json
    {
      "userId": "user123",
      "productId": 1,
      "targetPrice": 9.50,
      "storeId": 2 
    }
    ```
    *   `storeId` is optional.
*   **Successful Response (201 Created):**
    ```json
    {
      "id": 1,
      "userId": "user123",
      "product": {
        "id": 1,
        "name": "Lapte Zuzu",
        "category": { "id": 1, "name": "Lactate" },
        "brand": { "id": 1, "name": "Zuzu" }
      },
      "targetPrice": 9.50,
      "store": { "id": 2, "name": "Lidl" },
      "isActive": true,
      "createdAt": "2024-05-21T19:00:00.000000",
      "notifiedAt": null,
      "triggeredPrice": null,
      "triggeredStoreName": null
    }
    ```
*   **Error Responses:**
    *   `400 Bad Request`: Invalid input (e.g., product ID not found, duplicate alert).
    *   `404 Not Found`: Product or Store (if specified) not found.
*   **cURL Example:**
    ```bash
    curl -X POST "http://localhost:8080/api/v1/price-alerts" \
    -H "Content-Type: application/json" \
    -d '{
          "userId": "user123",
          "productId": 1,
          "targetPrice": 9.50,
          "storeId": 2
        }'
    ```

---

### 6. Optimize Shopping Basket

*   **Endpoint:** `POST /api/v1/shopping-baskets/optimize`
*   **Description:** Analyzes a shopping basket and returns an optimized purchasing plan.
*   **Request Body (application/json):**
    ```json
    {
      "userId": "userX",
      "items": [
        { "productId": 1, "desiredQuantity": 1 },
        { "productId": 20, "desiredQuantity": 2 } 
      ]
    }
    ```
*   **Successful Response (200 OK):**
    ```json
    {
      "userId": "userX",
      "storeShoppingLists": [
        {
          "storeId": 1,
          "storeName": "Kaufland",
          "itemsToBuy": [
            {
              "productId": 20,
              "productName": "Spaghetti nr.5",
              "quantityToBuy": 2,
              "unit": "BUCATA", // Assuming desiredQuantity means # of packages
              "pricePerUnitAtStore": 4.84, // Price after discount
              "totalItemCostAtStore": 9.68,
              "storePackageQuantity": 500,
              "storePackageUnit": "G",
              "discounted": true,
              "discountPercentage": 18
            }
          ],
          "totalCostForStore": 9.68,
          "numberOfProductsFromBasket": 1
        },
        {
          "storeId": 2,
          "storeName": "Lidl",
          "itemsToBuy": [
            {
              "productId": 1,
              "productName": "Lapte Zuzu",
              "quantityToBuy": 1,
              "unit": "BUCATA",
              "pricePerUnitAtStore": 8.91, // Price after discount
              "totalItemCostAtStore": 8.91,
              "storePackageQuantity": 1,
              "storePackageUnit": "L",
              "discounted": true,
              "discountPercentage": 10
            }
          ],
          "totalCostForStore": 8.91,
          "numberOfProductsFromBasket": 1
        }
      ],
      "overallMinimumCost": 18.59,
      "totalDistinctProductsInBasket": 2,
      "unfulfillableProductCount": 0,
      "unfulfillableProductIds": null,
      "potentialSavings": 2.31 
    }
    ```
*   **cURL Example:**
    ```bash
    curl -X POST "http://localhost:8080/api/v1/shopping-baskets/optimize" \
    -H "Content-Type: application/json" \
    -d '{
          "userId": "userX",
          "items": [
            { "productId": 1, "desiredQuantity": 1 },
            { "productId": 20, "desiredQuantity": 2 }
          ]
        }'
    ```

## Implemented Core Business Features

This section details how the core business requirements from the coding challenge are met by the API:

*   **Daily Shopping Basket Monitoring:**
    *   Implemented via the `POST /api/v1/shopping-baskets/optimize` endpoint.
    *   **Logic:** Takes a list of product IDs and desired quantities. For each product, it finds the latest available price in every store. If discounts are active for a product's specific package at a store, the discounted price is used. The system then greedily assigns each product in the basket to the store offering the lowest effective price for it. The response details the items to buy per store and the total costs.
*   **Best Discounts:**
    *   Implemented via the `GET /api/v1/discounts/best` endpoint.
    *   **Logic:** Fetches all discounts active on a given `referenceDate` (defaults to today). For each active discount, it finds the corresponding original price of the product (matching package quantity/unit at that store) on or before the reference date. It then calculates the discounted price. Results are paginated and primarily sorted by discount percentage descending.
*   **New Discounts:**
    *   Implemented via the `GET /api/v1/discounts/new` endpoint.
    *   **Logic:** Fetches discounts whose `recordedAtDate` (derived from the CSV filename) is on or after a given `sinceDate` (defaults to 24 hours ago). It enriches these with original and discounted prices, similar to "Best Discounts". Results are paginated and default sorted by `recordedAtDate` descending.
*   **Dynamic Price History Graphs:**
    *   **Individual Product History:** `GET /api/v1/price-history/product/{productId}`
        *   **Logic:** Retrieves all `PriceEntry` records for the specified product, optionally filtered by `storeId`, and within the `startDate` and `endDate`. Returns a list of price points over time.
    *   **Aggregated Category Trend:** `GET /api/v1/price-history/category/{categoryId}`
        *   **Logic:** Fetches all price entries for products within the given category. It normalizes prices to a standard unit (per KG or per Litre, specified by `baseUnit` parameter) using `UnitConverterUtil`. It then calculates the average normalized price per day. Filterable by `storeId` and date range.
    *   **Aggregated Brand Trend:** `GET /api/v1/price-history/brand/{brandId}`
        *   **Logic:** Similar to category trends, but aggregates prices for products belonging to the specified brand. Filterable by `storeId` and date range.
*   **Product Substitutes & Recommendations (Value per Unit):**
    *   Implemented via the `GET /api/v1/products/value-analysis` endpoint.
    *   **Logic:** For each product matching filter criteria (name, category, brand, optional store), it finds the latest price entry. Using `UnitConverterUtil`, it calculates the price per standard unit (KG or L if applicable). This allows comparison of value even if package sizes differ. Results are paginated and can be sorted by `pricePerStandardUnit`.
*   **Custom Price Alert:**
    *   **Creation:** `POST /api/v1/price-alerts` allows users to define an alert for a `productId`, `targetPrice`, and optionally a `storeId`.
    *   **Retrieval:** `GET /api/v1/price-alerts/user/{userId}/active` (active alerts) and `GET /api/v1/price-alerts/user/{userId}` (all alerts).
    *   **Deactivation:** `PUT /api/v1/price-alerts/{alertId}/deactivate`.
    *   **Automatic Checking:** The `PriceAlertCheckerScheduler` runs a scheduled job (configurable cron, default daily at 2 AM) via `PriceAlertService.findAndProcessTriggeredAlerts()`. This service checks all active alerts. For each alert, it finds the current price(s) of the product (considering the specific store if defined, or all stores otherwise). If a price is found at or below the `targetPrice`, the alert is marked inactive, and details like `triggeredPrice` and `triggeredStore` are recorded. (Actual notification to user is out of scope for this challenge).

## Assumptions Made / Simplifications

*   **Currency:** All prices in input CSVs and all calculations are assumed to be in "RON". The system currently does not support multiple currencies.
*   **Product Matching:** Product identity across different stores or CSV files relies on the combination of `product_name` and `brand` name being identical (case-insensitive for product name). No EAN codes or other universal identifiers are used.
*   **User Identity:** For features like Price Alerts and Shopping Baskets, `userId` is passed as a simple string. No authentication or complex user management system is implemented.
*   **"New Discounts" Definition:** A "new discount" is identified by its `recordedAtDate` (derived from the discount CSV filename, e.g., `kaufland_discounts_2025-05-08.csv` means recorded on 2025-05-08), not necessarily the `fromDate` when the discount becomes active.
*   **Shopping Basket Optimization Strategy:** The optimization uses a greedy approach: for each item in the basket, it picks the store offering the absolute cheapest price for that item *independently* of other items. It does not consider factors like minimizing the number of stores to visit or travel costs.
*   **"Original Price" for Discounts:** When calculating a discounted price, the "original price" is determined by finding the most recent `PriceEntry` for the exact product (same store, package quantity, and unit) on or before the discount's `referenceDate` (usually today or the discount start date). If no such price entry is found, the discount might not be processable for display with its discounted value.
*   **CSV File Encoding:** Input CSV files are expected to be UTF-8 encoded.
*   **Idempotency of CSV Ingestion:**
    *   For `PriceEntry`: If a CSV for a store and date is re-imported, existing price entries for that product/store/date are updated with the new price/package details.
    *   For `Discount`: If a discount CSV is re-imported, an existing discount (same product, store, fromDate, package) is updated if the new record's `recordedAtDate` is newer, or if it's the same `recordedAtDate` but other details (percentage, toDate) have changed.
*   **Error Handling in CSV Ingestion:** Individual malformed rows within a CSV file are logged and skipped, allowing the rest of the file to be processed. However, if a file-level issue occurs (e.g., invalid header, unrecoverable parsing error, or a critical error during data handling that causes the file's transaction to roll back), the entire file's data might not be committed, and the file will not be moved to the "processed" directory.
*   **Historical Discount Data:** The system primarily stores the latest known state of a discount based on the `recordedAtDate` and update logic. A full history of past discount percentages for the exact same product/store/period/package is not explicitly maintained if a newer CSV record updates it.