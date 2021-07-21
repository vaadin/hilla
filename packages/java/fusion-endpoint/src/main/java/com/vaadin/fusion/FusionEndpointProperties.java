/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class that contains all Vaadin endpoint customizable properties.
 */
@Component
@ConfigurationProperties("vaadin.endpoint")
public class FusionEndpointProperties {

    @Value("${vaadin.endpoint.prefix:/connect}")
    private String vaadinEndpointPrefix;

    /**
     * Customize the prefix for all Vaadin endpoints. See default value in the
     * {@link FusionEndpointProperties#vaadinEndpointPrefix} field annotation.
     *
     * @return prefix that should be used to access any Vaadin endpoint
     */
    public String getVaadinEndpointPrefix() {
        return vaadinEndpointPrefix;
    }

}
