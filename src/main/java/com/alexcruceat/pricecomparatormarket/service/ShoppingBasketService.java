package com.alexcruceat.pricecomparatormarket.service;
import com.alexcruceat.pricecomparatormarket.dto.BasketOptimizationResponseDTO;
import com.alexcruceat.pricecomparatormarket.dto.ShoppingBasketRequestDTO;

/**
 * Service for optimizing a user's shopping basket to find the most cost-effective
 * way to purchase all items, potentially across multiple stores.
 */
public interface ShoppingBasketService {

    /**
     * Analyzes a shopping basket request and returns an optimized plan.
     * The optimization aims to minimize the total cost for all items.
     *
     * @param request The user's shopping basket containing desired products and quantities.
     * @return A {@link BasketOptimizationResponseDTO} detailing the optimized shopping lists per store
     *         and overall cost information.
     */
    BasketOptimizationResponseDTO optimizeShoppingBasket(ShoppingBasketRequestDTO request);
}
