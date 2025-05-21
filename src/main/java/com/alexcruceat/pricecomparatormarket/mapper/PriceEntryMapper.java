package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.dto.PricePointDTO;
import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper for converting {@link PriceEntry} entities to DTOs relevant for price history.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceEntryMapper {

    /**
     * Converts a {@link PriceEntry} to a {@link PricePointDTO}.
     *
     * @param priceEntry The price entry entity.
     * @return The corresponding PricePointDTO.
     */
    @Mapping(source = "entryDate", target = "date")
    @Mapping(source = "store.name", target = "storeName")
    PricePointDTO toPricePointDTO(PriceEntry priceEntry);

    /**
     * Converts a list of {@link PriceEntry} entities to a list of {@link PricePointDTO}s.
     *
     * @param priceEntries List of price entry entities.
     * @return List of corresponding PricePointDTOs.
     */
    List<PricePointDTO> toPricePointDTOList(List<PriceEntry> priceEntries);

}
