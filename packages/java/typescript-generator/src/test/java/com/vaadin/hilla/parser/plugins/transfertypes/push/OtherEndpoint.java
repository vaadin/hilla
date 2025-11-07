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
package com.vaadin.hilla.parser.plugins.transfertypes.push;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import reactor.core.publisher.Flux;

// The purpose of this additional endpoint is to test multiple endpoints
@Endpoint
public class OtherEndpoint {

    public Flux<String> getMessageFlux(int count) {
        return Flux.just("Hello", "World").repeat(count);
    }

    public String toUpperCase(String message) {
        return message.toUpperCase();
    }

}
