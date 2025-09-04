/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.endpointransfermapper;

import java.util.ArrayList;

import com.vaadin.hilla.endpointransfermapper.EndpointTransferMapper.Mapper;
import com.vaadin.hilla.mappedtypes.Page;

/**
 * A mapper between {@link org.springframework.data.domain.Page} and
 * {@link Page}.
 */
public class PageMapper
        implements Mapper<org.springframework.data.domain.Page<?>, Page<?>> {

    private SortMapper sortMapper = new SortMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends org.springframework.data.domain.Page<?>> getEndpointType() {
        return (Class<? extends org.springframework.data.domain.Page<?>>) org.springframework.data.domain.Page.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Page<?>> getTransferType() {
        return (Class<? extends Page<?>>) Page.class;
    }

    @Override
    public Page<?> toTransferType(
            org.springframework.data.domain.Page<?> page) {
        Page<Object> transferPage = new Page<>();
        transferPage.setContent(new ArrayList<>(page.getContent()));
        transferPage.setSort(sortMapper.toTransferType(page.getSort()));
        transferPage.setLast(page.isLast());
        transferPage.setTotalPages(page.getTotalPages());
        transferPage.setTotalElements(page.getTotalElements());
        transferPage.setFirst(page.isFirst());
        transferPage.setNumberOfElements(page.getNumberOfElements());
        transferPage.setSize(page.getSize());
        transferPage.setNumber(page.getNumber());
        transferPage.setHasContent(page.hasContent());
        transferPage.setHasNext(page.hasNext());
        transferPage.setHasPrevious(page.hasPrevious());
        transferPage.setEmpty(page.isEmpty());
        return transferPage;
    }

    @Override
    public org.springframework.data.domain.Page<?> toEndpointType(
            Page<?> transferPage) {
        throw new UnsupportedOperationException(
                "Cannot create a Page from a transfer Page. Please create endpoints that accept Pageable as a parameter instead of returning a Page");
    }
}
