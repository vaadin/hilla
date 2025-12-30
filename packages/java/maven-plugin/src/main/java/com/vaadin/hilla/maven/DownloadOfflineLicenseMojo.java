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

/**
 * Goal that provides the URL to download a Vaadin offline license key.
 * <p>
 * This command displays a machine-specific URL that can be used to manually
 * download an offline license key. The offline license is tied to this
 * machine's hardware ID and must be saved manually to the file system
 * (~/.vaadin/offlineKey).
 * <p>
 * Unlike the online license (proKey), offline licenses work without internet
 * connectivity and are suitable for CI/CD environments and offline development.
 *
 * @since 25.0
 */
@Mojo(name = "download-offline-license", requiresProject = false)
public class DownloadOfflineLicenseMojo
        extends com.vaadin.flow.plugin.maven.DownloadOfflineLicenseMojo {
}
