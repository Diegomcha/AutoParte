package me.diegomcha.autoparte.api.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public abstract class PageableMapper {

    /**
     * Translates the sorting properties of the given Pageable object to match the entity fields.
     *
     * @param pageable The original Pageable object with sorting properties.
     * @return A new Pageable object with translated sorting properties.
     */
    public abstract Pageable translatePageable(Pageable pageable);

    protected Pageable translatePageable(Map<String, String> sortingPropertiesMap, Pageable pageable) {
        // If the pageable is unpaged or has no sorting, return it as is
        if (!pageable.getSort().isSorted())
            return pageable;

        // Translate sorting properties to match the entity fields
        List<Sort.Order> translatedOrders = pageable.getSort().stream()
                .map(order -> new Sort.Order(
                        order.getDirection(),
                        sortingPropertiesMap.getOrDefault(order.getProperty(), order.getProperty())
                ))
                .toList();

        return pageable.isUnpaged()
                ? Pageable.unpaged(Sort.by(translatedOrders))
                : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(translatedOrders)
        );
    }
}
