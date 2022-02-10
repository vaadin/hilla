package dev.hilla.parser.plugins.pageable.basic;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Endpoint
public class PageableEndpoint {
    public Page<String> getPage() {
        return null;
    }

    public Pageable getPageable() {
        return null;
    }

    public Sort getSort() {
        return null;
    }

    public Sort.Order getOrder() {
        return null;
    }
}
