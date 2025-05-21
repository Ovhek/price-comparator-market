package com.alexcruceat.pricecomparatormarket.mapper;

import com.alexcruceat.pricecomparatormarket.dto.PricePointDTO;
import com.alexcruceat.pricecomparatormarket.dto.TrendPointDTO;
import com.alexcruceat.pricecomparatormarket.model.PriceEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper for converting {@link PriceEntry} entities to {@link TrendPointDTO}s,
 * used for constructing price history for individual products.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceEntryMapper {

    /**
     * Converts a single {@link PriceEntry} entity to a {@link TrendPointDTO}.
     * For individual product history, the 'value' in TrendPointDTO represents the actual price,
     * and 'valueUnitDescription' can represent the currency or package unit.
     *
     * @param priceEntry The {@link PriceEntry} entity to convert. Must not be null.
     * @return The corresponding {@link TrendPointDTO}.
     */
    @Mapping(source = "entryDate", target = "date")
    @Mapping(source = "price", target = "value") // Map to 'value'
    @Mapping(source = "currency", target = "valueUnitDescription") // Use currency as value unit desc for individual price
    @Mapping(source = "store.name", target = "storeName")
    TrendPointDTO toTrendPointDTO(PriceEntry priceEntry);

    /**
     * Converts a list of {@link PriceEntry} entities to a list of {@link TrendPointDTO}s.
     *
     * @param priceEntries A list of {@link PriceEntry} entities. Must not be null.
     * @return A list of {@link TrendPointDTO}s.
     */
    List<TrendPointDTO> toTrendPointDTOList(List<PriceEntry> priceEntries);
}
