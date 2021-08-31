/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.endpointransfermapper;

import com.vaadin.fusion.endpointransfermapper.EndpointTransferMapper.Mapper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * A mapper between {@link Pageable} and {@link PageableDTO}.
 */
public class PageableMapper implements Mapper<Pageable, PageableDTO> {

    private SortMapper sortMapper = new SortMapper();

    @Override
    public Class<? extends Pageable> getEndpointType() {
        return Pageable.class;
    }

    @Override
    public Class<? extends PageableDTO> getTransferType() {
        return PageableDTO.class;
    }

    @Override
    public PageableDTO toTransferType(Pageable pageable) {
        PageableDTO dto = new PageableDTO();
        dto.setPageNumber(pageable.getPageNumber());
        dto.setPageSize(pageable.getPageSize());
        dto.setSort(sortMapper.toTransferType(pageable.getSort()));

        return dto;
    }

    @Override
    public Pageable toEndpointType(PageableDTO dto) {
        Sort sort = sortMapper.toEndpointType(dto.getSort());
        return PageRequest.of(dto.getPageNumber(), dto.getPageSize(), sort);
    }
}
