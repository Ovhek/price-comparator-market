package com.alexcruceat.pricecomparatormarket.repository;

import com.alexcruceat.pricecomparatormarket.config.AbstractIntegrationTest;
import com.alexcruceat.pricecomparatormarket.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StoreRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Store testStore1;
    private Store testStore2;

    @BeforeEach
    void setUp() {
        testStore1 = new Store("Lidl Test");
        testStore2 = new Store("Kaufland Test");
    }

    @Test
    @DisplayName("Save and retrieve store by ID")
    void whenSaveStore_thenCanBeRetrievedById() {
        // Given
        Store savedStore = storeRepository.save(testStore1);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Store> foundStoreOpt = storeRepository.findById(savedStore.getId());

        // Then
        assertThat(foundStoreOpt).isPresent();
        Store foundStore = foundStoreOpt.get();
        assertThat(foundStore.getName()).isEqualTo(testStore1.getName());
        assertThat(foundStore.getId()).isNotNull();
        assertThat(foundStore.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(foundStore.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Find store by name (case-insensitive)")
    void whenFindByNameIgnoreCase_thenReturnCorrectStore() {
        // Given
        storeRepository.save(testStore1);
        storeRepository.save(testStore2);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Store> foundStoreOpt = storeRepository.findByNameIgnoreCase("lidl test");

        // Then
        assertThat(foundStoreOpt).isPresent();
        assertThat(foundStoreOpt.get().getName()).isEqualTo(testStore1.getName());
    }

    @Test
    @DisplayName("Find store by name returns empty if not found")
    void whenFindByNameIgnoreCase_withNonExistentName_thenReturnEmpty() {
        // Given
        storeRepository.save(testStore1);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Store> foundStoreOpt = storeRepository.findByNameIgnoreCase("NonExistent Store");

        // Then
        assertThat(foundStoreOpt).isNotPresent();
    }

    @Test
    @DisplayName("Saving store with null name throws ConstraintViolationException")
    void whenSaveStoreWithNullName_thenThrowException() {
        // Given
        Store storeWithNullName = new Store();

        // When & Then
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            storeRepository.saveAndFlush(storeWithNullName);
        });
    }

    @Test
    @DisplayName("Saving stores with duplicate names throws DataIntegrityViolationException")
    void whenSaveStoresWithDuplicateName_thenThrowException() {
        // Given
        storeRepository.save(testStore1);
        entityManager.flush();

        Store duplicateStore = new Store("Lidl Test");

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            storeRepository.saveAndFlush(duplicateStore);
        });
    }

    @Test
    @DisplayName("Update existing store")
    void whenUpdateStore_thenChangesArePersisted() {
        // Given
        Store savedStore = storeRepository.save(testStore1);
        entityManager.flush();
        entityManager.detach(savedStore);

        // When
        Optional<Store> storeToUpdateOpt = storeRepository.findById(savedStore.getId());
        assertThat(storeToUpdateOpt).isPresent();
        Store storeToUpdate = storeToUpdateOpt.get();
        storeToUpdate.setName("Lidl Updated Test");
        storeRepository.saveAndFlush(storeToUpdate);
        entityManager.clear();

        // Then
        Optional<Store> updatedStoreOpt = storeRepository.findById(savedStore.getId());
        assertThat(updatedStoreOpt).isPresent();
        assertThat(updatedStoreOpt.get().getName()).isEqualTo("Lidl Updated Test");
        assertThat(updatedStoreOpt.get().getUpdatedAt()).isAfter(savedStore.getUpdatedAt());
    }
}