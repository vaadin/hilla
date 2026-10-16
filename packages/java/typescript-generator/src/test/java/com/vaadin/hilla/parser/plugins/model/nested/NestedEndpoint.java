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
package com.vaadin.hilla.parser.plugins.model.nested;

import java.util.List;
import java.util.Map;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Holds collections of collections and a map of anything, which the form models
 * write as models holding models.
 */
@Endpoint
public class NestedEndpoint {
    public Nested getNested() {
        return new Nested();
    }

    public int[][] transpose(int[][] matrix) {
        return matrix;
    }

    public static class Nested {
        public List<List<Integer>> numberMatrix;
        public List<List<Nested>> entityMatrix;
        public String[][] stringMatrix;
        public Map<String, Object> objectMap;
    }
}
