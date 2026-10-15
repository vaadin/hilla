/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.hilla.gradle.plugin

/**
 * Extend the VaadinBuildFrontendTask so that frontend files are not cleaned after build.
 */
public open class EngineBuildFrontendTask : com.vaadin.gradle.VaadinBuildFrontendTask() {
}
