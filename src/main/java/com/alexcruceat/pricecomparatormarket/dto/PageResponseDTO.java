package com.alexcruceat.pricecomparatormarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A generic Data Transfer Object for returning paginated data from API endpoints.
 * It standardizes the structure of paged responses, including the list of content
 * for the current page and essential pagination metadata such as page number,
 * page size, total elements, and total pages.
 *
 * @param <T> The type of the content items within the page.
 */
@Getter
@Setter
@Schema(description = "Wrapper for paginated API responses.")
@NoArgsConstructor
public class PageResponseDTO<T> {

    @Schema(description = "List of items for the current page.")
    private List<T> content;

    @Schema(description = "Current page number (0-indexed).", example = "0")
    private int pageNumber;

    @Schema(description = "Number of items per page.", example = "20")
    private int pageSize;

    @Schema(description = "Total number of items across all pages.", example = "150")
    private long totalElements;

    @Schema(description = "Total number of pages.", example = "8")
    private int totalPages;

    @Schema(description = "Whether this is the last page.", example = "false")
    private boolean last;

    @Schema(description = "Whether this is the first page.", example = "true")
    private boolean first;

    @Schema(description = "Number of elements in the current page.", example = "20")
    private int numberOfElements;

    /**
     * Constructs a {@code PageResponseDTO} from a Spring Data {@link Page} object.
     * This constructor facilitates easy conversion from the persistence/service layer's
     * pagination model to the API response DTO.
     *
     * @param page The {@link Page} object from Spring Data, containing the data slice
     *             and pagination metadata. Must not be null.
     */
    public PageResponseDTO(Page<T> page) {
        if (page == null) {
            throw new IllegalArgumentException("Page object cannot be null.");
        }
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.numberOfElements = page.getNumberOfElements();
    }
}
