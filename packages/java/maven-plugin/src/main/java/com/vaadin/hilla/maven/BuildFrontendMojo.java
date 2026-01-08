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

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.FrontendUtils;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.theme.Theme;

/**
 * Goal that builds the frontend bundle.
 *
 * It performs the following actions when creating a package:
 * <ul>
 * <li>Update {@link Constants#PACKAGE_JSON} file with the {@link NpmPackage}
 * annotations defined in the classpath,</li>
 * <li>Copy resource files used by flow from `.jar` files to the `node_modules`
 * folder</li>
 * <li>Install dependencies by running <code>npm install</code></li>
 * <li>Update the {@link FrontendUtils#IMPORTS_NAME} file imports with the
 * {@link JsModule} {@link Theme} and {@link JavaScript} annotations defined in
 * the classpath,</li>
 * <li>Update {@link FrontendUtils#VITE_CONFIG} file.</li>
 * </ul>
 *
 * @since Flow 2.0
 */
@Mojo(name = "build-frontend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class BuildFrontendMojo extends
        com.vaadin.flow.plugin.maven.BuildFrontendMojo implements Configurable {
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

    @Parameter
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
