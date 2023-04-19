/**
 *    Copyright 2000-2023 Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.hilla.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

import dev.hilla.engine.*

/**
 * This task checks that node and npm tools are installed, copies frontend
 * resources available inside `.jar` dependencies to `node_modules`, and creates
 * or updates `package.json` and `webpack.config.json` files.
 */
public open class EngineConfigureTask : DefaultTask() {

  init {
        group = "Hilla"
        description = "Hilla Configure Task"

        dependsOn("classes")
    }

    @TaskAction
    public fun engineConfigure() {
        val extension: EngineProjectExtension = EngineProjectExtension.get(project)
        logger.info("Running the engineConfigure task with effective configuration $extension")

        val generator = GeneratorConfiguration()
        val parser = ParserConfiguration()
        val projectBuildDir = project.buildDir.toPath()

        val conf = EngineConfiguration.Builder(project.projectDir.toPath())
                      .classPath(
              LinkedHashSet<String>(
                //project.getRuntimeClasspathElements()
              )
            )
            .outputDir(extension.generatedTsFolder.toPath())
            .generator(generator)
            .parser(parser)
            .buildDir(extension.projectBuildDir)
            .classesDir(projectBuildDir)
            .create();

        // The configuration gathered from the Maven plugin is saved in a
        // file so that further runs can skip running a separate Maven
        // project just to get this configuration again

        // The configuration gathered from the Maven plugin is saved in a
        // file so that further runs can skip running a separate Maven
        // project just to get this configuration again
        val configDir: Path = project.projectDir.toPath().resolve(projectBuildDir)
        Files.createDirectories(configDir)
        conf.store(
          configDir
            .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME)
            .toFile()
        )
    }
}
