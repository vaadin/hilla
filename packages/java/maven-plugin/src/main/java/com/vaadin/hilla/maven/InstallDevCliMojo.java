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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Installs the {@code vaadin-dev} CLI and its agent skills into the project.
 * <p>
 * Unbound, so it never runs as a side effect of a normal build. Run it on the
 * application module.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Mojo(name = "install-dev-cli", requiresDependencyResolution = ResolutionScope.TEST)
public class InstallDevCliMojo
        extends com.vaadin.flow.plugin.maven.InstallDevCliMojo {
}
