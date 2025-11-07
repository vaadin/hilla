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
package com.vaadin.flow.spring.fusionsecurityjwt.endpoints;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.hilla.Endpoint;
import com.vaadin.flow.spring.fusionsecurity.fusionform.Elements;
import com.vaadin.flow.spring.fusionsecurity.fusionform.Elements.Options;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Endpoint
@AnonymousAllowed
public class ElementsEndpoint {

    public Elements getElements() {
        return new Elements();
    }

    public List<String> getOptions() {
        return Stream.of(Options.values()).map(Enum::toString)
                .collect(Collectors.toList());
    }

    public Elements saveElements(Elements item) {
        return item;
    }
}
