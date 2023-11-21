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

import org.junit.After
import org.junit.Before

/**
 * Prepares a test Gradle project - creates a temp dir for the [testProject] and allow you to run gradle
 * tasks. See [TestProject] for more details.
 */
abstract class AbstractGradleTest {

    val hillaVersion = System.getenv("hilla.version").takeUnless { it.isNullOrEmpty() } ?: "2.4-SNAPSHOT"

    /**
     * The testing Gradle project. Automatically deleted after every test.
     * Don't use TemporaryFolder JUnit `@Rule` since it will always delete the folder afterward,
     * making it impossible to investigate the folder in case of failure.
     */
    lateinit var testProject: TestProject

    @Before
    fun createTestProjectFolder() {
        testProject = TestProject()
    }

    @After
    fun deleteTestProjectFolder() {
        // comment out if a test is failing and you need to investigate the project files.
        testProject.delete()
    }

    @Before
    fun dumpEnvironment() {
        println("Test project directory: ${testProject.dir}")
    }
}
