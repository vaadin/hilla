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
 * Goal that downloads a Vaadin Pro license key by opening the browser and
 * waiting for the user to log in.
 * <p>
 * The downloaded license key is saved to the local file system
 * (~/.vaadin/proKey) and can be used for validating commercial Vaadin
 * components.
 *
 * @since 25.0
 */
@Mojo(name = "download-license", requiresProject = false)
public class DownloadLicenseMojo
        extends com.vaadin.flow.plugin.maven.DownloadLicenseMojo {
}
