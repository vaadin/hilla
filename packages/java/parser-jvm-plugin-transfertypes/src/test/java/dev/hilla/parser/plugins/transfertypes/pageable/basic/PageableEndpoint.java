package dev.hilla.parser.plugins.transfertypes.pageable.basic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Endpoint
public class PageableEndpoint {
    public Sort.Order getOrder() {
        return null;
    }

    public Page<String> getPage() {
        return null;
    }

    public Pageable getPageable() {
        return null;
    }

    public Sort getSort() {
        return null;
    }
}
