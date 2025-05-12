package com.alexcruceat.pricecomparatormarket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class PriceComparatorMarketApplicationTests {

    @Container
    private static final MariaDBContainer<?> mariaDBContainer =
            new MariaDBContainer<>(DockerImageName.parse("mariadb:10.11"))
                    .withDatabaseName("test_price_comparator_db")
                    .withUsername("test_comparator_user")
                    .withPassword("test_123456");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDBContainer::getUsername);
        registry.add("spring.datasource.password", mariaDBContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MariaDBDialect");

        System.out.println("Testcontainer MariaDB JDBC URL: " + mariaDBContainer.getJdbcUrl());
        System.out.println("Testcontainer MariaDB Username: " + mariaDBContainer.getUsername());

    }

    @Test
    void contextLoads() {
        System.out.println("Context loaded successfully with Testcontainers!");
    }

}