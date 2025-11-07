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
package com.vaadin.hilla.parser.plugins.backbone.wildcard;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Endpoint
public class WildcardTypeEndpoint {
    public Map<String, ?> getDefaultWildcard() {
        return null;
    }

    public List<? extends Map<Object, Object>> getExtendingWildcard() {
        return null;
    }

    public Optional<? super List<Object>> getSuperWildcard() {
        return Optional.empty();
    }
}
