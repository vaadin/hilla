/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

package com.vaadin.hilla.gradle.plugin

import org.junit.After
import org.junit.Before

/**
 * Prepares a test Gradle project - creates a temp dir for the [testProject] and allow you to run gradle
 * tasks. See [TestProject] for more details.
 */
abstract class AbstractGradleTest {

    val hillaVersion = System.getenv("hilla.version").takeUnless { it.isNullOrEmpty() } ?: "24.4-SNAPSHOT"

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
