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
package com.vaadin.hilla.scripts;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.frontend.FileIOUtils;
import com.vaadin.flow.server.frontend.NodeUpdater;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for reading "package.json" Flow resources and applying changes to
 * the Hilla repository when necessary.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public class FlowPackageJsonUpdater {
    private static final String FRONTEND_RESOURCES_PATH = NodeUpdater.class
        .getPackage().getName().replace('.', '/');

    private static final JsonPointer DEPENDENCIES = JsonPointer.compile("/dependencies");
    private static final JsonPointer DEV_DEPENDENCIES = JsonPointer.compile("/devDependencies");

    private Path packageJsonFile;
    private ObjectNode tree;

    private FlowPackageJsonUpdater(Path packageJsonFile) throws IOException {
        this.packageJsonFile = packageJsonFile;
        var packageJsonFileContents = Files.readString(this.packageJsonFile);
        tree = JacksonUtils.readTree(packageJsonFileContents);
    }

    private void applyFlowPackageJson(String id) {
        if (logger().isDebugEnabled()) {
            logger().debug("Applying Flow {} package.json.", id);
        }
        var name = String.join("/",
            FRONTEND_RESOURCES_PATH,
            "dependencies",
            id,
            "package.json"
        );
        var resource = getClass().getClassLoader().getResource(name);
        try {
            assert resource != null;
            var flowPackageJsonTree = JacksonUtils.readTree(IOUtils.toString(resource, StandardCharsets.UTF_8));
            Stream.of(DEPENDENCIES, DEV_DEPENDENCIES).forEach((section) -> {
                // NOTE: Some dependencies are devDependencies in Hilla,
                // such as "react". Hence we update at "devDependencies".
                updateTreeAt(DEV_DEPENDENCIES, flowPackageJsonTree.at(section));
            });
        } catch (IOException e) {
            logger().error("Unable to read Flow {} package.json resource, skipping.", id);
        }
    }

    private void updateTreeAt(JsonPointer pointer, JsonNode value) {
        var current = tree.at(pointer);
        if (current.isMissingNode()) {
            if (logger().isDebugEnabled()) {
                logger().debug("Skipping update for {}, missing from the Hilla package.json file.", pointer);
            }
            return;
        }

        if (value instanceof ObjectNode valueObject) {
            valueObject.fields().forEachRemaining((field) -> {
                updateTreeAt(pointer.appendProperty(field.getKey()), field.getValue());
            });
            return;
        }

        if (value.equals(current)) {
            if (logger().isDebugEnabled()) {
                logger().debug("Skipping update for {}, same value of {}.", pointer, value);
            }
            return;
        }

        if (logger().isDebugEnabled()) {
            logger().debug("Updating {} from {} to {}.", pointer, current, value);
        }
        var parentTree = tree.at(pointer.head());
        if (parentTree instanceof ObjectNode parentTreeObject) {
            parentTreeObject.set(pointer.last().getMatchingProperty(), value);
        }
    }

    private void saveChanges() throws IOException {
        FileIOUtils.writeIfChanged(packageJsonFile.toFile(), JacksonUtils.toFileJson(tree));
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        var packageJsonFile = getHillaProjectDir().resolve("package.json");
        var instance = new FlowPackageJsonUpdater(packageJsonFile);
        for (String arg : args) {
            instance.applyFlowPackageJson(arg);
        }
        instance.saveChanges();
    }

    private static Path getHillaProjectDir() {
        return FileIOUtils.getProjectFolderFromClasspath().toPath()
            .getParent() // java
            .getParent() // scripts
            .getParent(); // hilla
    }

    private static Logger logger() {
        return LoggerFactory.getLogger(FlowPackageJsonUpdater.class.getName());
    }
}
