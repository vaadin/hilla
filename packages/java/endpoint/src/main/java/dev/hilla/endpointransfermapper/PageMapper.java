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
package dev.hilla.endpointransfermapper;

import java.util.List;

import dev.hilla.endpointransfermapper.EndpointTransferMapper.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * A mapper between {@link Page} and {@link List}.
 */
public class PageMapper implements Mapper<Page<?>, List<?>> {

    @Override
    public Class<? extends Page<?>> getEndpointType() {
        return (Class) Page.class;
    }

    @Override
    public Class<? extends List<?>> getTransferType() {
        return (Class) List.class;
    }

    @Override
    public List<?> toTransferType(Page<?> page) {
        return page.getContent();
    }

    @Override
    public Page<?> toEndpointType(List<?> list) {
        return new PageImpl<>(list);
    }
}
