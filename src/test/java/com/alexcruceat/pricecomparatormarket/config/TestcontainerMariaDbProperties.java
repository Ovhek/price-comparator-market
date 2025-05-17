package com.alexcruceat.pricecomparatormarket.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class TestcontainerMariaDbProperties {

    /**
     * Docker image name and tag for MariaDB.
     * Example: "mariadb:10.11"
     */
    @NotBlank
    private String image = "mariadb:10.11";

    /**
     * Database name to be created within the container.
     */
    @NotBlank
    private String dbName = "test_price_comparator_db";

    /**
     * Username for the database.
     */
    @NotBlank
    private String user = "comparator_user";

    /**
     * Password for the database.
     */
    @NotBlank
    private String password = "123456";

}
