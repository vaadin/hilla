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

import dev.hilla.engine.EngineConfiguration
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import org.gradle.testkit.runner.BuildResult
import org.junit.Test
import java.io.File
import kotlin.test.expect

class SingleModuleTest : AbstractGradleTest() {

    @Test
    fun `hillaConfigure executed HillaEngineConfigurationJson should exist`() {
        createProject()

        testProject.build("hillaConfigure", checkTasksSuccessful = true)

        expect(true, "hilla-engine-configuration.json should be created after executing hillaConfigure task!") {
            testProject.folder("build").find("hilla-engine-configuration.json").first().exists()
        }
    }

    @Test
    fun `exposedPackagesToParser configured in build file hillaConfigure executed HillaEngineConfigurationJson should contain exposed packages`() {
        val package1 = "com.example.app"
        val package2 = "dev.hilla.foo"
        createProject(package1, package2)

        val buildResult: BuildResult = testProject.build("hillaConfigure", checkTasksSuccessful = true)

        buildResult.expectTaskSucceded("hillaConfigure")

        val hillaEngineConfigFile = testProject.folder("build").find("hilla-engine-configuration.json").first()
        expect(true, "hilla-engine-configuration.json should be created after executing hillaConfigure task!") {
            hillaEngineConfigFile.exists()
        }

        val configuration = EngineConfiguration.load(hillaEngineConfigFile)
        val packages = configuration.parser.packages.orElseThrow()
        expect(true, "Configuration json should contained exposed package '$package1'") {
            packages.contains(package1)
        }
        expect(true, "Configuration json should contained exposed package '$package2'") {
            packages.contains(package2)
        }
    }

    /*@Test
    fun `endpoints are generated in openapi json after hillaGenerate task executed`() {
        createProject()
        addFileToProject("src/main/java/com/example/application/HelloReactEndpoint.java", """
            package com.example.application;

            import com.vaadin.flow.server.auth.AnonymousAllowed;
            import dev.hilla.Endpoint;
            import dev.hilla.Nonnull;

            @Endpoint
            @AnonymousAllowed
            public class HelloReactEndpoint {

                @Nonnull
                public String sayHello(@Nonnull String name) {
                    if (name.isEmpty()) {
                        return "Hello stranger";
                    } else {
                        return "Hello " + name;
                    }
                }
            }
        """.trimIndent())

        val buildResult: BuildResult = testProject.build("hillaGenerate", checkTasksSuccessful = true)
        buildResult.expectTaskSucceded("hillaConfigure")
        buildResult.expectTaskSucceded("hillaGenerate")

        val openApiJsonFile = testProject.folder("build").resolve("classes/dev/hilla/openapi.json")

        expect(true, "openapi.json should be created after executing hillaGenerate task!") {
            openApiJsonFile.exists()
        }

        val openApi = Json.mapper().readValue(openApiJsonFile, OpenAPI::class.java)
        expect(true, "Generated openapi.json file should contain paths for existing endpoints!") {
            openApi.paths["/HelloReactEndpoint/sayHello1"] != null
        }
    }*/

    private fun createProject(vararg exposedPackages: String) {

        val exposedPackagesExtension = if (exposedPackages.isNotEmpty()) {
            val commaSeparatedPackages = exposedPackages.asList().joinToString { "\"$it\"" }
            """
                hilla {
                	exposedPackagesToParser = [$commaSeparatedPackages]
                }
            """.trimIndent()
        } else "";

        addConfigurationsToSettingsForUsingPluginFromLocalRepo(testProject)

        testProject.buildFile.writeText(
            """
            buildscript {
                repositories {
                    mavenLocal()
                    mavenCentral()
                    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
                }
                dependencies {
                    classpath("dev.hilla:engine-gradle-plugin:$hillaVersion")
                    classpath('com.vaadin:flow-gradle-plugin:$flowVersion')
                }
            }
            plugins {
                id 'org.springframework.boot' version '3.0.2'
                id 'io.spring.dependency-management' version '1.0.15.RELEASE'
                id 'java'
            }

            apply plugin: 'com.vaadin'
            apply plugin: 'dev.hilla.engine'

            $exposedPackagesExtension

            repositories {
                mavenLocal()
                mavenCentral()
                maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
                maven { setUrl("https://maven.vaadin.com/vaadin-addons") }
            }

            configurations {
                developmentOnly
                runtimeClasspath {
                    extendsFrom developmentOnly
                }
            }

            dependencies {
                implementation 'dev.hilla:hilla-react'
                implementation 'dev.hilla:hilla-spring-boot-starter'
            }

            dependencyManagement {
                imports {
                    mavenBom "dev.hilla:hilla-bom:$hillaVersion"
                }
            }
        """.trimIndent()
        )
    }

    private fun addFileToProject(endpointClassNameWithPath: String, endpointClassContent: String) : File {
        val endpointFile = testProject.newFile(endpointClassNameWithPath, endpointClassContent)
        expect(true, "Endpoint class '$endpointClassNameWithPath' should exist!") {
            endpointFile.exists()
        }
        return endpointFile
    }
}
