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

import static com.vaadin.flow.server.frontend.FrontendUtils.FRONTEND;

import java.io.File;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.vaadin.flow.plugin.maven.FlowModeAbstractMojo;
import com.vaadin.hilla.engine.BrowserCallableFinderException;
import com.vaadin.hilla.engine.GeneratorException;
import com.vaadin.hilla.engine.GeneratorProcessor;
import com.vaadin.hilla.engine.ParserException;
import com.vaadin.hilla.engine.ParserProcessor;

/**
 * Maven Plugin for Hilla. Handles parsing Java bytecode and generating
 * TypeScript code from it.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public final class EngineGenerateMojo extends AbstractMojo
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
    public void execute() throws EngineGenerateMojoException {
        var project = (MavenProject) getPluginContext().get("project");
        if (!FlowModeAbstractMojo.isHillaAvailable(project)) {
            getLog().warn(
                    """
                            The 'generate' goal is only meant to be used in Hilla projects with endpoints.
                            """
                            .stripIndent());
            return;
        }
        try {
            var conf = configure();
            var parserProcessor = new ParserProcessor(conf);
            var generatorProcessor = new GeneratorProcessor(conf);

            var browserCallables = conf.getBrowserCallableFinder().find(conf);
            parserProcessor.process(browserCallables);
            generatorProcessor.process();
        } catch (GeneratorException | ParserException
                | BrowserCallableFinderException
                | DependencyResolutionRequiredException e) {
            throw new EngineGenerateMojoException("Execution failed", e);
        }
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
