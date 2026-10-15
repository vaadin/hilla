/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.hilla.gradle.plugin

import com.vaadin.gradle.VaadinFlowPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.vaadin.hilla.engine.*

/**
 * Task that generates the configuration json which is needed
 * for next task of generating the endpoints and model classes.
 */
public open class EngineConfigureTask : DefaultTask() {

    init {
        group = "Vaadin"
        description = "Hilla Configure Task"
    }

    @TaskAction
    public fun engineConfigure() {
        val vaadinExtension = VaadinFlowPluginExtension.get(project)
        val engineConfiguration = HillaPlugin.createEngineConfiguration(project, vaadinExtension)
        EngineConfiguration.setDefault(engineConfiguration)
    }
}
