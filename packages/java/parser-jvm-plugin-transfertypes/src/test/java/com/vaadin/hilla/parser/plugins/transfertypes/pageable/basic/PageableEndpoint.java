package com.vaadin.hilla.parser.plugins.transfertypes.pageable.basic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

@Endpoint
public class PageableEndpoint {
    public Sort.Order getOrder() {
        return null;
    }

    public Page<String> getPage() {
        return null;
    }

    public Slice<String> getSlice() {
        return null;
    }

    public Pageable getPageable() {
        return null;
    }

    public Sort getSort() {
        return null;
    }

    public Custom getCustom() {
        return null;
    }

    public static class Custom {
        public Page page;
    }
}
