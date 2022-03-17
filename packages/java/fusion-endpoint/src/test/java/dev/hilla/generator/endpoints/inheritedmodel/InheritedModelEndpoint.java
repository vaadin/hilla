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
package dev.hilla.generator.endpoints.inheritedmodel;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.Version;
import dev.hilla.Endpoint;
import io.swagger.v3.oas.models.media.ArraySchema;

@Endpoint
public class InheritedModelEndpoint {

    public ParentModel getParentModel(ChildModel child) {
        return new ParentModel();
    }

    public static class ChildModel extends ParentModel {
        String name;
        // This is to make sure that inherited types from dependencies work
        // well.
        ArraySchema testObject;
        List<Map<String, Version>> abc;
        List<Map<String, Map<String, Version>>> def;
    }

    public static class ParentModel {
        String id;

    }
}
