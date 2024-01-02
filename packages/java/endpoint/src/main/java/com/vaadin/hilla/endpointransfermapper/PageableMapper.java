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

import com.vaadin.hilla.mappedtypes.Pageable;

import org.springframework.data.domain.PageRequest;

/**
 * A mapper between {@link Pageable} and {@link Pageable}.
 */
public class PageableMapper implements
        EndpointTransferMapper.Mapper<org.springframework.data.domain.Pageable, Pageable> {

    private SortMapper sortMapper = new SortMapper();

    @Override
    public Class<? extends org.springframework.data.domain.Pageable> getEndpointType() {
        return org.springframework.data.domain.Pageable.class;
    }

    @Override
    public Class<? extends Pageable> getTransferType() {
        return Pageable.class;
    }

    @Override
    public Pageable toTransferType(
            org.springframework.data.domain.Pageable pageable) {
        Pageable transferPageable = new Pageable();
        transferPageable.setPageNumber(pageable.getPageNumber());
        transferPageable.setPageSize(pageable.getPageSize());
        transferPageable.setSort(sortMapper.toTransferType(pageable.getSort()));

        return transferPageable;
    }

    @Override
    public org.springframework.data.domain.Pageable toEndpointType(
            Pageable transferPageable) {
        org.springframework.data.domain.Sort sort = sortMapper
                .toEndpointType(transferPageable.getSort());
        return PageRequest.of(transferPageable.getPageNumber(),
                transferPageable.getPageSize(), sort);
    }
}
