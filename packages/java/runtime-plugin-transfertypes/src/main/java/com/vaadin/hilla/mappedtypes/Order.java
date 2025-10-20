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

import org.jspecify.annotations.NonNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;

import com.vaadin.hilla.transfertypes.annotations.FromModule;
import com.vaadin.hilla.transfertypes.annotations.ModelFromModule;

/**
 * A DTO for {@code org.springframework.data.domain.Sort.Order}.
 */
@FromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "Order")
@ModelFromModule(module = "@vaadin/hilla-frontend", namedSpecifier = "OrderModel")
public class Order {
    @NonNull
    private Direction direction;
    @NonNull
    @NotBlank
    private String property;
    private boolean ignoreCase;
    private NullHandling nullHandling;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public NullHandling getNullHandling() {
        return nullHandling;
    }

    public void setNullHandling(NullHandling nullHandling) {
        this.nullHandling = nullHandling;
    }

}
