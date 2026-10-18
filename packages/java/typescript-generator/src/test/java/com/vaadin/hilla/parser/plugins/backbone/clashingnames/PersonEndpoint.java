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
package com.vaadin.hilla.parser.plugins.backbone.clashingnames;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Refers to three types of the same name, the second and third of which are
 * imported under names of their own.
 */
@Endpoint
public class PersonEndpoint {
    public com.vaadin.hilla.parser.plugins.backbone.clashingnames.one.Person first() {
        return null;
    }

    public com.vaadin.hilla.parser.plugins.backbone.clashingnames.two.Person second() {
        return null;
    }

    public com.vaadin.hilla.parser.plugins.backbone.clashingnames.three.Person third() {
        return null;
    }
}
