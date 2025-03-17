/**
 *    Copyright 2000-2024 Vaadin Ltd
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
package com.vaadin.hilla.gradle.plugin

import java.io.IOException
import com.vaadin.hilla.engine.EngineConfiguration
import com.vaadin.hilla.engine.GeneratorException
import com.vaadin.hilla.engine.GeneratorProcessor
import com.vaadin.hilla.engine.ParserException
import com.vaadin.hilla.engine.ParserProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

/**
 * Task that generates the endpoints.ts and model TS classes
 * needed for calling the backend in a typesafe manner.
 */
public abstract class EngineGenerateTask : DefaultTask() {

    init {
        group = "Vaadin"
        description = "Hilla Generate Task"

        // we need the compiled classes:
        this.dependsOn("classes", "hillaConfigure")

        // Make sure to run this task before the `war`/`jar` tasks, so that
        // generated endpoints and models will end up packaged in the war/jar archive.
        // The inclusion rule itself is configured in the HillaPlugin class.
        project.tasks.withType(Jar::class.java) { task: Jar ->
            task.mustRunAfter("generate")
        }
    }

    @TaskAction
    public fun engineGenerate() {
        try {
            val conf = EngineConfiguration.load()

            val parserProcessor = ParserProcessor(conf)
            val generatorProcessor = GeneratorProcessor(conf)

            val endpoints = conf.findBrowserCallables();
            parserProcessor.process(endpoints)
            generatorProcessor.process()
        } catch (e: IOException) {
            throw GradleException("Endpoint collection failed", e)
        } catch (e: InterruptedException) {
            throw GradleException("Endpoint collection failed", e)
        } catch (e: GeneratorException) {
            throw GradleException("Execution failed", e)
        } catch (e: ParserException) {
            throw GradleException("Execution failed", e)
        }
    }
}
