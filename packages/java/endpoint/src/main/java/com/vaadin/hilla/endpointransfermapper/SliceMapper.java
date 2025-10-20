/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.hilla.mappedtypes.Slice;

/**
 * A mapper between {@link org.springframework.data.domain.Slice} and
 * {@link Slice}.
 */
public class SliceMapper
        implements Mapper<org.springframework.data.domain.Slice<?>, Slice<?>> {

    private SortMapper sortMapper = new SortMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends org.springframework.data.domain.Slice<?>> getEndpointType() {
        return (Class<? extends org.springframework.data.domain.Slice<?>>) (Class<?>) org.springframework.data.domain.Slice.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Slice<?>> getTransferType() {
        return (Class<? extends Slice<?>>) (Class<?>) Slice.class;
    }

    @Override
    public Slice<?> toTransferType(
            org.springframework.data.domain.Slice<?> slice) {
        Slice<Object> transferSlice = new Slice<>();
        transferSlice.setContent(new ArrayList<>(slice.getContent()));
        transferSlice.setSort(sortMapper.toTransferType(slice.getSort()));
        transferSlice.setLast(slice.isLast());
        transferSlice.setFirst(slice.isFirst());
        transferSlice.setNumberOfElements(slice.getNumberOfElements());
        transferSlice.setSize(slice.getSize());
        transferSlice.setNumber(slice.getNumber());
        transferSlice.setHasContent(slice.hasContent());
        transferSlice.setHasNext(slice.hasNext());
        transferSlice.setHasPrevious(slice.hasPrevious());
        transferSlice.setEmpty(slice.isEmpty());
        return transferSlice;
    }

    @Override
    public org.springframework.data.domain.Slice<?> toEndpointType(
            Slice<?> transferSlice) {
        throw new UnsupportedOperationException(
                "Cannot create a Slice from a transfer Slice. Please create endpoints that accept Pageable as a parameter instead of returning a Slice");
    }
}
