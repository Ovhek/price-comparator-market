package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.model.UserPriceAlert;
import com.alexcruceat.pricecomparatormarket.dto.PriceAlertDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper for converting between {@link UserPriceAlert} entities and {@link PriceAlertDTO}s.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ProductMapper.class, StoreMapper.class}, // For nested product and store DTOs
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceAlertMapper {

    /**
     * Converts a {@link UserPriceAlert} entity to a {@link PriceAlertDTO}.
     *
     * @param alert The UserPriceAlert entity.
     * @return The corresponding PriceAlertDTO.
     */
    @Mapping(source = "product", target = "product") // Handled by ProductMapper
    @Mapping(source = "store", target = "store")   // Handled by StoreMapper (will be null if alert.store is null)
    @Mapping(source = "triggeredStore.name", target = "triggeredStoreName")
    PriceAlertDTO toDTO(UserPriceAlert alert);

    /**
     * Converts a list of {@link UserPriceAlert} entities to a list of {@link PriceAlertDTO}s.
     *
     * @param alerts The list of UserPriceAlert entities.
     * @return The list of corresponding PriceAlertDTOs.
     */
    List<PriceAlertDTO> toDTOList(List<UserPriceAlert> alerts);

}