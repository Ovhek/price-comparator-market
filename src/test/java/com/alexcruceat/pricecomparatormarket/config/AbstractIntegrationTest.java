package com.alexcruceat.pricecomparatormarket.config;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Properties;

/**
 * Base Class for Test Configuration
 */
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static TestcontainerMariaDbProperties mariaDbProps;

    protected static final MariaDBContainer<?> mariaDBContainer;

    static {
        try {
            Properties loadedProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-test.properties"));
            ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(loadedProps);
            Binder binder = new Binder(propertySource);
            mariaDbProps = binder.bind("testcontainer.mariadb", Bindable.of(TestcontainerMariaDbProperties.class))
                    .orElseGet(TestcontainerMariaDbProperties::new);
        } catch (IOException e) {
            System.err.println("Failed to load application-test.properties for Testcontainer configuration. Using defaults.");
            mariaDbProps = new TestcontainerMariaDbProperties();
            System.err.println("Using default Testcontainer MariaDB properties: image=" + mariaDbProps.getImage() +
                    ", dbName=" + mariaDbProps.getDbName() + ", user=" + mariaDbProps.getUser());
        }

        mariaDBContainer = new MariaDBContainer<>(DockerImageName.parse(mariaDbProps.getImage()))
                .withDatabaseName(mariaDbProps.getDbName())
                .withUsername(mariaDbProps.getUser())
                .withPassword(mariaDbProps.getPassword());

        mariaDBContainer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(mariaDBContainer::stop));
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDBContainer::getUsername);
        registry.add("spring.datasource.password", mariaDBContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MariaDBDialect");
    }
}
