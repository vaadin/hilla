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

package com.vaadin.hilla.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BeanWithJacksonAnnotation {
    @JsonProperty("bookId")
    private String id;
    private String name;

    @JsonProperty("name")
    public void setFirstName(String name) {
        this.name = name;
    }

    @JsonProperty("name")
    public String getFirstName() {
        return name;
    }

    @JsonProperty
    public int getRating() {
        return 2;
    }
}
