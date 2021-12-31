/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.fusion.generator;

/**
 * Basic information of the OpenApi spec.
 */
public class OpenAPIConfiguration {

    private final String applicationTitle;
    private final String applicationApiVersion;
    private final String serverUrl;
    private final String serverDescription;

    /**
     * Create a new OpenApi configuration.
     *
     * @param applicationTitle
     *            Title of the application
     * @param applicationApiVersion
     *            api version of the application
     * @param serverUrl
     *            Base url of the application
     * @param serverDescription
     *            Description of the server
     */
    public OpenAPIConfiguration(String applicationTitle,
            String applicationApiVersion, String serverUrl,
            String serverDescription) {
        this.applicationTitle = applicationTitle;
        this.applicationApiVersion = applicationApiVersion;
        this.serverUrl = serverUrl;
        this.serverDescription = serverDescription;
    }

    /**
     * Get application title.
     *
     * @return application title
     */
    public String getApplicationTitle() {
        return applicationTitle;
    }

    /**
     * Get application api version.
     *
     * @return application api version
     */
    public String getApplicationApiVersion() {
        return applicationApiVersion;
    }

    /**
     * Get server url.
     *
     * @return server url
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Get server description.
     *
     * @return server description
     */
    public String getServerDescription() {
        return serverDescription;
    }

}
