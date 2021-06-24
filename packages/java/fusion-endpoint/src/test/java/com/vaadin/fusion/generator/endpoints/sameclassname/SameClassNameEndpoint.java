/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.generator.endpoints.sameclassname;

import java.util.List;
import java.util.Map;

import com.vaadin.fusion.Endpoint;

@Endpoint
public class SameClassNameEndpoint {
    public SameClassNameModel getMyClass(
            List<com.vaadin.fusion.generator.endpoints.sameclassname.subpackage.SameClassNameModel> sameClassNameModel) {
        return null;
    }

    public List<com.vaadin.fusion.generator.endpoints.sameclassname.subpackage.SameClassNameModel> getSubpackageModelList(
            Map<String, com.vaadin.fusion.generator.endpoints.sameclassname.subpackage.SameClassNameModel> sameClassNameModel) {
        return null;
    }

    public Map<String, com.vaadin.fusion.generator.endpoints.sameclassname.subpackage.SameClassNameModel> getSubpackageModelMap(
            Map<String, SameClassNameModel> sameClassNameModel) {
        return null;
    }

    public com.vaadin.fusion.generator.endpoints.sameclassname.subpackage.SameClassNameModel getSubpackageModel() {
        return null;
    }

    public void setSubpackageModel(
            com.vaadin.fusion.generator.endpoints.sameclassname.subpackage.SameClassNameModel model) {
    }

    public static class SameClassNameModel {
        String foo;
    }
}
