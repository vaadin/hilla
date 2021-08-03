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
package com.vaadin.fusion.frontend;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.NodeUpdater;
import com.vaadin.flow.server.frontend.TaskUseFusionPackage;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Populate the package.json file with Fusion dependencies.
 */
public class TaskUseFusionPackageImpl extends NodeUpdater
        implements TaskUseFusionPackage {

    /**
     * Constructor.
     *
     * @param npmFolder
     *            folder with the `package.json` file
     * @param generatedPath
     *            folder where flow generated files will be placed.
     * @param flowResourcesPath
     *            folder where flow dependencies will be copied to.
     * @param buildDir
     *            the used build directory
     */
    protected TaskUseFusionPackageImpl(File npmFolder, File generatedPath,
            File flowResourcesPath, String buildDir) {
        super(null, null, npmFolder, generatedPath, flowResourcesPath,
                buildDir);
    }

    public void execute() throws ExecutionFailedException {
        try {
            JsonObject packageJson = getPackageJson();

            getDependencies().forEach((key, value) -> addDependency(packageJson,
                    DEPENDENCIES, key, value));

            writePackageFile(sort(packageJson));
        } catch (IOException e) {
            throw new ExecutionFailedException("PackageJson update is failed",
                    e);
        }
    }

    private Map<String, String> getDependencies() {
        Map<String, String> dependencies = new HashMap<>();

        dependencies.put("@adobe/lit-mobx", "2.0.0-rc.4");
        dependencies.put("mobx", "^6.1.5");

        return dependencies;
    }

    private JsonObject sort(JsonObject object) {
        String[] keys = object.keys();
        Arrays.sort(keys);
        JsonObject result = Json.createObject();
        for (String key : keys) {
            JsonValue value = object.get(key);
            if (value instanceof JsonObject) {
                value = sort((JsonObject) value);
            }
            result.put(key, value);
        }
        return result;
    }
}
