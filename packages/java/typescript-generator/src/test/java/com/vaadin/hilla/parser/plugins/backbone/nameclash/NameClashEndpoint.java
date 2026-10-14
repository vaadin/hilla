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
package com.vaadin.hilla.parser.plugins.backbone.nameclash;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

/**
 * Takes parameters named as the request options are, so that those have to step
 * aside once, twice and three times.
 */
@Endpoint
public class NameClashEndpoint {
    public String getOneParam(String init) {
        return init;
    }

    public String getTwoParams(String init, String _init) {
        return init + _init;
    }

    public String getThreeParams(String __init, String _init, String init) {
        return __init + _init + init;
    }
}
