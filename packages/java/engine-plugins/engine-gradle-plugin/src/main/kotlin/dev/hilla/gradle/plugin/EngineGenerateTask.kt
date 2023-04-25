/**
 *    Copyright 2000-2022 Vaadin Ltd
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
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*

import dev.hilla.engine.*
/**
 * Task that builds the frontend bundle.
 *
 * It performs the following actions when creating a package:
 * * Update [Constants.PACKAGE_JSON] file with the [com.vaadin.flow.component.dependency.NpmPackage]
 * annotations defined in the classpath,
 * * Copy resource files used by flow from `.jar` files to the `node_modules`
 * folder
 * * Install dependencies by running `npm install`
 * * Update the [FrontendUtils.IMPORTS_NAME] file imports with the
 * [com.vaadin.flow.component.dependency.JsModule] [com.vaadin.flow.theme.Theme] and [com.vaadin.flow.component.dependency.JavaScript] annotations defined in
 * the classpath,
 * * Update [FrontendUtils.WEBPACK_CONFIG] file.
 *
 */
public open class EngineGenerateTask : DefaultTask() {
    init {
        group = "Hilla"
        description = "Hilla Generate Task"

        // we need the build/hilla-engine-configuration.json, so we depend on "configure" task:
        dependsOn("hillaConfigure")


        // Make sure to run this task before the `war`/`jar` tasks, so that
        // vite bundle will end up packaged in the war/jar archive. The inclusion
        // rule itself is configured in the HillaPlugin class.
        project.tasks.withType(Jar::class.java) { task: Jar ->
            task.mustRunAfter("generate")
        }
    }

    @TaskAction
    public fun engineGenerate() {
        val extension: EngineProjectExtension = EngineProjectExtension.get(project)
        logger.info("Running the engineGenerate task with effective configuration $extension")

        val baseDir: Path = project.projectDir.toPath()
        val buildDir: Path = baseDir.resolve(extension.projectBuildDir)

        try {
            val conf: EngineConfiguration = Objects.requireNonNull<EngineConfiguration>(
                EngineConfiguration.loadDirectory(buildDir))

            val urls = conf.getClassPath()
                .stream().map<URL> { classPathItem: Path ->
                    classPathItem.toUri().toURL()
                }
                .toList()

            val classLoader = URLClassLoader(
                urls.toTypedArray(),
                javaClass.getClassLoader()
            )
            val parserProcessor: ParserProcessor = ParserProcessor(conf, classLoader)
            val generatorProcessor: GeneratorProcessor = GeneratorProcessor(conf, extension.nodeCommand)

            parserProcessor.process()
            generatorProcessor.process()

        } catch (e: IOException) {
            throw GradleException("Loading saved configuration failed", e)
        } catch (e: GeneratorException) {
            throw GradleException("Execution failed", e)
        } catch (e: ParserException) {
            throw GradleException("Execution failed", e)
        }
    }
}
