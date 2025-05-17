package com.alexcruceat.pricecomparatormarket;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PriceComparatorMarketApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        System.out.println("Context loaded successfully with Testcontainers!");
    }

}