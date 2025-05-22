package com.alexcruceat.pricecomparatormarket.service.impl;

import com.alexcruceat.pricecomparatormarket.model.*;
import com.alexcruceat.pricecomparatormarket.repository.PriceEntryRepository;
import com.alexcruceat.pricecomparatormarket.repository.ProductRepository;
import com.alexcruceat.pricecomparatormarket.dto.*;
import com.alexcruceat.pricecomparatormarket.mapper.ProductMapper;
import com.alexcruceat.pricecomparatormarket.mapper.StoreMapper;
import com.alexcruceat.pricecomparatormarket.service.DiscountService;
import com.alexcruceat.pricecomparatormarket.service.ShoppingBasketService;
import com.alexcruceat.pricecomparatormarket.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of {@link ShoppingBasketService}.
 * Optimizes a user's shopping basket by finding the cheapest options for each item,
 * potentially splitting the purchase across multiple stores.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingBasketServiceImpl implements ShoppingBasketService {

    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final DiscountService discountService; // To find active discounts for products
    private final StoreService storeService;

    // Helper class to store pricing info for an item at a specific store
    @Value
    private static class PricedItemAtStore {
        Product product;
        Store store;
        BigDecimal quantity;
        UnitOfMeasure originalProductUnit;
        BigDecimal originalProductPackageQuantity;
        BigDecimal pricePerSellingUnit;
        BigDecimal effectivePriceForDesiredQuantity;
        boolean isDiscounted;
        Integer discountPercentage;
    }


    /**
     * {@inheritDoc}
     * For each item in the basket, it finds the store offering the lowest price (after discounts) for that item.
     */
    @Override
    @Transactional(readOnly = true) // This operation primarily reads data
    public BasketOptimizationResponseDTO optimizeShoppingBasket(ShoppingBasketRequestDTO request) {
        log.info("Optimizing shopping basket for userId: {}", request.getUserId());
        LocalDate today = LocalDate.now(); // Reference date for current prices and active discounts

        List<Long> unfulfillableProductIds = new ArrayList<>();
        Map<Long, List<PricedItemAtStore>> productOffers = new HashMap<>();
        BigDecimal sumOfHighestPricesBaseline = BigDecimal.ZERO;

        // 1. For each item in the user's basket, find all its current prices across all stores
        for (ShoppingBasketItemRequestDTO itemRequest : request.getItems()) {
            Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
            if (productOpt.isEmpty()) {
                log.warn("Product with ID {} not found in basket for user {}. Skipping.", itemRequest.getProductId(), request.getUserId());
                unfulfillableProductIds.add(itemRequest.getProductId());
                continue;
            }
            Product product = productOpt.get();
            List<PricedItemAtStore> offersForItem = new ArrayList<>();
            BigDecimal highestPriceForItem = BigDecimal.ZERO; // Highest price found for this item across stores

            // Find all current price entries for this product today (or latest available)
            List<PriceEntry> currentPriceEntries = priceEntryRepository.findLatestPriceEntriesPerStoreForProduct(product.getId(), today);
            if (currentPriceEntries.isEmpty()) {
                // Fallback: if no price today, try any latest price for the product
                priceEntryRepository.findFirstByProductAndEntryDateLessThanEqualOrderByEntryDateDesc(product, today)
                        .ifPresent(currentPriceEntries::add);
            }


            if (currentPriceEntries.isEmpty()) {
                log.warn("No current price entries found for product ID {} ({}). Cannot fulfill.", product.getId(), product.getName());
                unfulfillableProductIds.add(product.getId());
                continue;
            }

            for (PriceEntry entry : currentPriceEntries) {
                BigDecimal originalPricePerSellingUnit = entry.getPrice();
                BigDecimal effectivePricePerSellingUnit = originalPricePerSellingUnit;
                boolean isDiscounted = false;
                Integer discountPercentage = null;

                // Check for active discounts for this product at this store for this package
                List<Discount> activeDiscounts = discountService.findActiveDiscountsByProductStoreAndPackage(
                        product, entry.getStore(), entry.getPackageQuantity(), entry.getPackageUnit(), today
                );

                if (!activeDiscounts.isEmpty()) {
                    // Assuming only one discount applies or picking the best one if multiple (e.g. highest percentage)
                    Discount bestDiscount = activeDiscounts.stream()
                            .max(Comparator.comparing(Discount::getPercentage))
                            .orElse(null); // Should not be null if list is not empty

                    isDiscounted = true;
                    discountPercentage = bestDiscount.getPercentage();
                    BigDecimal multiplier = BigDecimal.ONE.subtract(
                            BigDecimal.valueOf(discountPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    );
                    effectivePricePerSellingUnit = originalPricePerSellingUnit.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
                }

                // Handle desired quantity vs package quantity
                // e.g., if product is 0.5KG bread, and user wants 2, it means 2 * 0.5KG bread.
                // Current PricedItemAtStore assumes desiredQuantity is # of packages.
                BigDecimal totalCostForDesiredQuantity = effectivePricePerSellingUnit.multiply(itemRequest.getDesiredQuantity()).setScale(2, RoundingMode.HALF_UP);

                offersForItem.add(new PricedItemAtStore(
                        product, entry.getStore(), itemRequest.getDesiredQuantity(),
                        entry.getPackageUnit(), entry.getPackageQuantity(),
                        originalPricePerSellingUnit, // Store the original price per selling unit
                        totalCostForDesiredQuantity,
                        isDiscounted, discountPercentage
                ));

                // For potentialSavings baseline
                BigDecimal baselinePriceForDesiredQuantity = originalPricePerSellingUnit.multiply(itemRequest.getDesiredQuantity()).setScale(2, RoundingMode.HALF_UP);
                if (baselinePriceForDesiredQuantity.compareTo(highestPriceForItem) > 0) {
                    highestPriceForItem = baselinePriceForDesiredQuantity;
                }
            }

            productOffers.put(product.getId(), offersForItem);
            sumOfHighestPricesBaseline = sumOfHighestPricesBaseline.add(highestPriceForItem);
        }

        // 2. Greedy Optimization: For each product, pick the cheapest offer.
        Map<Long, List<OptimizedShoppingItemDTO>> itemsByStore = new HashMap<>(); // StoreId -> List of items for that store

        for (Map.Entry<Long, List<PricedItemAtStore>> entry : productOffers.entrySet()) {
            // Long productId = entry.getKey(); // Not used directly here
            List<PricedItemAtStore> offers = entry.getValue();

            if (offers.isEmpty()) continue;

            // Find the cheapest offer for this product
            PricedItemAtStore cheapestOffer = offers.stream()
                    .min(Comparator.comparing(PricedItemAtStore::getEffectivePriceForDesiredQuantity))
                    .orElse(null); // Should not be null if offers is not empty

            OptimizedShoppingItemDTO optimizedItem = OptimizedShoppingItemDTO.builder()
                    .productId(cheapestOffer.getProduct().getId())
                    .productName(cheapestOffer.getProduct().getName())
                    .quantityToBuy(cheapestOffer.getQuantity())
                    .unit(cheapestOffer.getOriginalProductUnit().name())
                    .pricePerUnitAtStore(cheapestOffer.getEffectivePriceForDesiredQuantity().divide(cheapestOffer.getQuantity(), 2, RoundingMode.HALF_UP))
                    .totalItemCostAtStore(cheapestOffer.getEffectivePriceForDesiredQuantity())
                    .storePackageQuantity(cheapestOffer.getOriginalProductPackageQuantity())
                    .storePackageUnit(cheapestOffer.getOriginalProductUnit().name())
                    .discounted(cheapestOffer.isDiscounted())
                    .discountPercentage(cheapestOffer.getDiscountPercentage())
                    .build();

            itemsByStore.computeIfAbsent(cheapestOffer.getStore().getId(), k -> new ArrayList<>()).add(optimizedItem);
        }

        // 3. Construct the response
        List<OptimizedStoreShoppingListDTO> storeShoppingLists = new ArrayList<>();
        BigDecimal overallMinimumCost = BigDecimal.ZERO;

        for (Map.Entry<Long, List<OptimizedShoppingItemDTO>> storeEntry : itemsByStore.entrySet()) {
            Long storeId = storeEntry.getKey();
            List<OptimizedShoppingItemDTO> storeItems = storeEntry.getValue();
            // We need the Store object again to get its name

            Store store = storeService.findStoreById(storeId).orElse(
                    storeItems.getFirst().getProductId() != null ?
                            productOffers.get(storeItems.getFirst().getProductId()).stream()
                                    .filter(offer -> offer.getStore().getId().equals(storeId))
                                    .findFirst().map(PricedItemAtStore::getStore).orElse(null)
                            : null
            );

            BigDecimal totalCostForStore = storeItems.stream()
                    .map(OptimizedShoppingItemDTO::getTotalItemCostAtStore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            overallMinimumCost = overallMinimumCost.add(totalCostForStore);

            storeShoppingLists.add(OptimizedStoreShoppingListDTO.builder()
                    .storeId(storeId)
                    .storeName(store != null ? store.getName() : "Store ID " + storeId) // Fallback if store object not easily available
                    .itemsToBuy(storeItems)
                    .totalCostForStore(totalCostForStore)
                    .numberOfProductsFromBasket(storeItems.size())
                    .build());
        }

        // Sort store lists by store name for consistent output
        storeShoppingLists.sort(Comparator.comparing(OptimizedStoreShoppingListDTO::getStoreName, String.CASE_INSENSITIVE_ORDER));


        BigDecimal potentialSavings = sumOfHighestPricesBaseline.subtract(overallMinimumCost).max(BigDecimal.ZERO);

        return BasketOptimizationResponseDTO.builder()
                .userId(request.getUserId())
                .storeShoppingLists(storeShoppingLists)
                .overallMinimumCost(overallMinimumCost)
                .totalDistinctProductsInBasket(request.getItems().size())
                .unfulfillableProductCount(unfulfillableProductIds.size())
                .unfulfillableProductIds(unfulfillableProductIds.isEmpty() ? null : unfulfillableProductIds)
                .potentialSavings(potentialSavings)
                .build();
    }
}
