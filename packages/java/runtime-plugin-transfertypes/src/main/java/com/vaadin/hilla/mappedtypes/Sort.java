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
package com.vaadin.hilla.mappedtypes;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import com.vaadin.hilla.transfertypes.annotations.FromModule;
import com.vaadin.hilla.transfertypes.annotations.ModelFromModule;

/**
 * A DTO for {@code org.springframework.data.domain.Sort}.
 */
@FromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "Sort")
@ModelFromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "SortModel")
public class Sort {
    @NonNull
    private List<Order> orders = new ArrayList<>();

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}
