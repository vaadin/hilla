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

import com.vaadin.flow.server.frontend.FrontendUtils
import dev.hilla.engine.EngineConfiguration
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import org.gradle.testkit.runner.BuildResult
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.function.Function
import java.util.stream.Stream
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

    @Test
    fun `endpoints ts and openapi json are generated after hillaGenerate task executed`() {
        createProject(withNpmInstall = true)

        addHelloReactEndpoint()

        val buildResult: BuildResult = testProject.build("hillaGenerate", checkTasksSuccessful = true)

        buildResult.expectTaskSucceded("hillaConfigure")
        buildResult.expectTaskSucceded("hillaGenerate")

        verifyOpenApiJsonFileGeneratedProperly()
        verifyEndpointsTsFileGeneratedProperly()
    }

    private fun verifyOpenApiJsonFileGeneratedProperly() {
        val openApiJsonFile = testProject.folder("build").resolve("classes/dev/hilla/openapi.json")

        expect(true, "openapi.json should be created after executing hillaGenerate task!") {
            openApiJsonFile.exists()
        }

        val openApi = Json.mapper().readValue(openApiJsonFile, OpenAPI::class.java)
        expect(true, "Generated openapi.json file should contain paths for existing endpoints!") {
            openApi.paths.contains("/HelloReactEndpoint/sayHello")
        }
    }

    private fun verifyEndpointsTsFileGeneratedProperly() {
        val endpointsTsFile = testProject.dir.resolve("frontend/generated/endpoints.ts")
        expect(true, "Generated endpoints.ts file should exist!") {
            endpointsTsFile.exists()
        }
        expect(true, "Generated endpoints.ts file should exist!") {
            endpointsTsFile.readText().contains("import * as HelloReactEndpoint")
        }
    }

    private fun addHelloReactEndpoint() : File {
        val endpointFile = testProject.newFile("src/main/java/com/example/application/HelloReactEndpoint.java",
        """
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
        expect(true, "Endpoint class 'HelloReactEndpoint.java' should exist!") {
            endpointFile.exists()
        }
        return endpointFile
    }

    private fun createProject(vararg exposedPackages: String, withNpmInstall: Boolean = false, productionMode: Boolean = false) {

        val exposedPackagesExtension = if (exposedPackages.isNotEmpty()) {
            val commaSeparatedPackages = exposedPackages.asList().joinToString { "\"$it\"" }
            """
                hilla {
                	exposedPackagesToParser = [$commaSeparatedPackages]
                }
            """.trimIndent()
        } else "";

        val npmInstallTask = if (withNpmInstall) {
            """
                task npmInstall(type: Exec) {
                    commandLine 'npm', 'install'
                }
            """.trimIndent()
        } else ""

        val productionBuild = if (productionMode) {
            """
            vaadin {
                productionMode = true
            }
            """.trimIndent()
        } else ""

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
                    classpath("dev.hilla:hilla-gradle-plugin:$hillaVersion")
                }
            }
            plugins {
                id 'org.springframework.boot' version '3.0.2'
                id 'io.spring.dependency-management' version '1.0.15.RELEASE'
                id 'java'
            }

            apply plugin: 'dev.hilla'

            $exposedPackagesExtension

            $npmInstallTask

            $productionBuild

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

        if (withNpmInstall) {
            prepareGeneratorPluginsAndPerformNpmInstall()
        }
    }

    private fun prepareGeneratorPluginsAndPerformNpmInstall() {
        val packagesDirectory = Path
            .of(javaClass.classLoader.getResource("")!!.toURI()) // functionalTest
            .parent // kotlin
            .parent // classes
            .parent // build
            .parent // gradle-plugin
            .parent // java
            .parent // packages
            .resolve("ts")

        val shellCmd = if (FrontendUtils.isWindows()) Stream.of("cmd.exe", "/c") else Stream.empty()

        val npmCmd = Stream.of(
            "npm", "--no-update-notifier", "--no-audit",
            "install", "--no-save"
        )

        val generatedFiles = Files
            .list(packagesDirectory).filter { dirName: Path ->
                dirName.fileName
                    .toString().startsWith("generator-")
            }
            .map { obj: Path -> obj.toString() }

        val command = Stream.of(shellCmd, npmCmd, generatedFiles)
            .flatMap(Function.identity()).toList()

        val processBuilder = FrontendUtils.createProcessBuilder(command)
            .directory(testProject.dir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
        val exitCode = processBuilder.start().waitFor()
        if (exitCode != 0) {
            throw FrontendUtils.CommandExecutionException(exitCode)
        }
    }

}
