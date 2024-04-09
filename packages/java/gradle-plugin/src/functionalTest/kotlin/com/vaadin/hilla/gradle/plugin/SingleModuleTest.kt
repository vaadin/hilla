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

import com.vaadin.flow.server.frontend.FrontendUtils
import com.vaadin.hilla.engine.EngineConfiguration
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import org.gradle.testkit.runner.BuildResult
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
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
        val package2 = "com.vaadin.hilla.foo"

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
    fun `endpoints ts and openapi json are generated after hillaGenerate task executed in dev mode`() {
        createProject(withNpmInstall = true)

        addHelloReactEndpoint()

        val buildResult: BuildResult = testProject.build("hillaGenerate", checkTasksSuccessful = true)

        buildResult.expectTaskSucceded("hillaConfigure")
        buildResult.expectTaskSucceded("hillaGenerate")

        verifyOpenApiJsonFileGeneratedProperly(false)
        verifyEndpointsTsFileGeneratedProperly()
    }

    @Test
    fun `endpoints ts and openapi json are generated after hillaGenerate task executed in prod mode`() {
        createProject(withNpmInstall = true, productionMode = true)

        addHelloReactEndpoint()

        val buildResult: BuildResult = testProject.build("hillaGenerate", checkTasksSuccessful = true)

        buildResult.expectTaskSucceded("hillaConfigure")
        buildResult.expectTaskSucceded("hillaGenerate")

        verifyOpenApiJsonFileGeneratedProperly(true)
        verifyEndpointsTsFileGeneratedProperly()
    }


    private fun verifyOpenApiJsonFileGeneratedProperly(productionMode: Boolean) {
        val openApiJsonFileName = (if (productionMode) "classes/" else "") + "hilla-openapi.json"
        val openApiJsonFile = testProject.folder("build").resolve(openApiJsonFileName)

        expect(true, "hilla-openapi.json should be created after executing hillaGenerate task!") {
            openApiJsonFile.exists()
        }

        val openApi = Json.mapper().readValue(openApiJsonFile, OpenAPI::class.java)
        expect(true, "Generated hilla-openapi.json file should contain paths for existing endpoints!") {
            openApi.paths.contains("/HelloReactEndpoint/sayHello")
        }
    }

    private fun verifyEndpointsTsFileGeneratedProperly() {
        val endpointsTsFile = testProject.dir.resolve(FrontendUtils.DEFAULT_PROJECT_FRONTEND_GENERATED_DIR + "endpoints.ts")
        expect(true, "Generated endpoints.ts file should exist!") {
            endpointsTsFile.exists()
        }
        expect(true, "Generated endpoints.ts file should contain correct endpoint import!") {
            endpointsTsFile.readText().contains("import * as HelloReactEndpoint")
        }
    }

    private fun addHelloReactEndpoint() : File {
        val endpointFile = testProject.newFile("src/main/java/com/example/application/HelloReactEndpoint.java",
        """
            package com.example.application;

            import com.vaadin.flow.server.auth.AnonymousAllowed;
            import com.vaadin.hilla.Endpoint;
            import com.vaadin.hilla.Nonnull;

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

    private fun createProject(vararg exposedPackages: String, withNpmInstall: Boolean = false, productionMode: Boolean = false,
                              disableAllTasksToSimulateDryRun: Boolean = false) {

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

        // We don't want to actually run the build in production,
        // but we just want to make sure of the task dependency.
        // Running gradle using --dry-run does not create the task, just prints the dependency order.
        // This is a workaround to simulate a dry run. The tasks are created in order but skipped during the build:
        val disableAllTasks = if (disableAllTasksToSimulateDryRun) {
            """
                tasks.configureEach {
                    it.enabled = false
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
                    classpath("com.vaadin:hilla-gradle-plugin:$hillaVersion")
                }
            }
            plugins {
                id 'org.springframework.boot' version '3.1.5'
                id 'io.spring.dependency-management' version '1.0.15.RELEASE'
                id 'java'
            }

            apply plugin: 'com.vaadin.hilla'

            $exposedPackagesExtension

            $npmInstallTask

            $productionBuild

            $disableAllTasks

            repositories {
                mavenLocal()
                mavenCentral()
                maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
                maven { setUrl("https://maven.vaadin.com/vaadin-addons") }
            }

            dependencies {
                implementation 'com.vaadin.hilla:hilla'
                implementation 'com.vaadin:vaadin-spring'
                implementation 'org.springframework.boot:spring-boot-starter-web'
            }

            dependencyManagement {
                imports {
                    mavenBom "com.vaadin.hilla:hilla-bom:$hillaVersion"
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
            "install", "--no-save", "--install-links"
        )

        val hillaFrontendAndFormPackages = Stream.of(
            packagesDirectory.resolve("frontend").toString(),
            packagesDirectory.resolve("lit-form").toString())

        val generatorPackages = Files
            .list(packagesDirectory).filter { dirName: Path ->
                dirName.fileName
                    .toString().startsWith("generator-")
            }
            .map { obj: Path -> obj.toString() }

        // executing the full command will install necessary npm packages without a package.json file:
        val command = Stream.of(shellCmd, npmCmd, hillaFrontendAndFormPackages, generatorPackages)
            .flatMap(Function.identity()).toList()

        val processBuilder = FrontendUtils.createProcessBuilder(command)
            .directory(testProject.dir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectErrorStream(true)
        val exitCode = processBuilder.start().waitFor()
        if (exitCode != 0) {
            throw FrontendUtils.CommandExecutionException(exitCode)
        }
    }

}
