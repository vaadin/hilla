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
package com.vaadin.hilla.parser.plugins.backbone.complextype;

import java.util.List;
import java.util.Map;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class ComplexTypeEndpoint {
    public ComplexTypeModel getComplexTypeModel(
            List<Map<String, String>> data) {
        return new ComplexTypeModel();
    }

    private ComplexTypeModel getPrivateComplexTypeModel() {
        return new ComplexTypeModel();
    }

    public static class ComplexTypeModel {
        private List<Map<String, List<String>>> complexList;
        private Map<String, List<String>> complexMap;

        public List<Map<String, List<String>>> getComplexList() {
            return complexList;
        }

        public Map<String, List<String>> getComplexMap() {
            return complexMap;
        }
    }
}
