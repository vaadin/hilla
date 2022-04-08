package dev.hilla.runtime.transfertypes;

import javax.annotation.Nonnull;

/**
 * A DTO for {@link org.springframework.data.domain.Pageable}.
 */
public class Pageable {
    private int pageNumber;
    private int pageSize;
    @Nonnull
    private Sort sort = new Sort();

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

}
