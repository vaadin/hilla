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
package com.vaadin.hilla.parser.plugins.nonnull.extended;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.hilla.parser.plugins.nonnull.basic.BasicEndpoint;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class ExtendedEndpoint extends BasicEndpoint {
    @Nonnull
    public List<Entity> getNonnullListOfNullableElements() {
        return Collections.emptyList();
    }

    public List<Map<String, List<Map<String, @Nonnull String>>>> superComplexType(
            List<Map<String, List<Map<String, @Nonnull String>>>> list) {
        return list;
    }

    public static class Entity {
        private List<String> nonnullListOfNullableStrings;

        @Nonnull
        public List<String> getNonnullListOfNullableStrings() {
            return nonnullListOfNullableStrings;
        }
    }
}
