/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.maven;

import static com.vaadin.flow.internal.FrontendUtils.FRONTEND;

import java.io.File;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * This goal checks that node and npm tools are installed and creates or updates
 * `package.json` and the frontend build tool configuration files.
 * <p>
 * Copies frontend resources available inside `.jar` dependencies to
 * `node_modules` when building a jar package.
 *
 * @since Flow 2.0
 */
@Mojo(name = "prepare-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class PrepareFrontendMojo
        extends com.vaadin.flow.plugin.maven.PrepareFrontendMojo
        implements Configurable {
    /**
     * A directory with project's frontend source files.
     */
    @Parameter(property = "frontendDirectory", defaultValue = "${project.basedir}/src/main/"
            + FRONTEND)
    private File frontend;

    /**
     * The folder where TypeScript endpoints are generated.
     */
    @Parameter(property = "generatedTsFolder")
    private File generated;

    @Parameter(property = "nodeCommand", defaultValue = "node")
    private String node;

    @Parameter(property = "mainClass")
    private String mainClass;

    @Override
    protected void executeInternal()
            throws MojoExecutionException, MojoFailureException {
        try {
            configure();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e);
        }
        super.executeInternal();
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public String getMainClass() {
        return mainClass;
    }

    @Override
    public File getFrontend() {
        return frontend;
    }

    @Override
    public File getGenerated() {
        return generated;
    }
}
