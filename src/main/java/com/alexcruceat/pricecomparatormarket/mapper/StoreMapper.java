package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.model.Store;
import com.alexcruceat.pricecomparatormarket.dto.StoreDTO;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper interface for converting between {@link Store} entities and {@link StoreDTO}s.
 * Uses MapStruct for implementation generation.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoreMapper {

    /**
     * Converts a {@link Store} entity to a {@link StoreDTO}.
     *
     * @param store The Store entity.
     * @return The corresponding StoreDTO.
     */
    StoreDTO toDTO(Store store);

    /**
     * Converts a {@link StoreDTO} to a {@link Store} entity.
     *
     * @param storeDTO The StoreDTO.
     * @return The corresponding Store entity.
     */
    Store toEntity(StoreDTO storeDTO);

    /**
     * Converts a list of {@link Store} entities to a list of {@link StoreDTO}s.
     *
     * @param stores The list of Store entities.
     * @return The list of corresponding StoreDTOs.
     */
    List<StoreDTO> toDTOList(List<Store> stores);

    /**
     * Updates an existing {@link Store} entity from a {@link StoreDTO}.
     * The {@code id} of the store is typically not updated from the DTO.
     *
     * @param storeDTO The DTO containing updated information.
     * @param store    The entity to be updated (annotated with @MappingTarget).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(StoreDTO storeDTO, @MappingTarget Store store);
}
