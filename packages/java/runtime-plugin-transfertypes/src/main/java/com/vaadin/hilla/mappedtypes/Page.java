package com.vaadin.hilla.mappedtypes;

import com.vaadin.hilla.transfertypes.annotations.FromModule;

@FromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "Page")
public class Page<T> extends Slice<T> {
    private long totalElements;
    private int totalPages;

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
