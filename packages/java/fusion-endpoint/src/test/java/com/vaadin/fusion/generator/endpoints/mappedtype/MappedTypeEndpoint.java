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
package com.vaadin.fusion.generator.endpoints.mappedtype;

import com.vaadin.fusion.Endpoint;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Endpoint
public class MappedTypeEndpoint {

    private Pageable pageable;

    public Pageable returnValue() {
        return PageRequest.of(2, 20);
    }

    public void parameter(Pageable pageable) {
        this.pageable = pageable;
    }

    public Pageable getPageable() {
        return pageable;
    }
}
