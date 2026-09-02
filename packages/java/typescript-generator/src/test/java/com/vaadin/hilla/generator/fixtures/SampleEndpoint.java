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
package com.vaadin.hilla.generator.fixtures;

import java.util.List;
import java.util.Map;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class SampleEndpoint {
    public void ping() {
    }

    public int count() {
        return 0;
    }

    public String greet(String name) {
        return name;
    }

    public List<String> names() {
        return List.of();
    }

    public Map<String, Integer> counts() {
        return Map.of();
    }

    public Sample find(String id) {
        return null;
    }

    public String shadow(String init) {
        return init;
    }

    public static class Sample {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
