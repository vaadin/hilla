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
package com.vaadin.hilla.route;


import com.vaadin.hilla.route.records.ClientViewConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Keeps track of registered client side routes.
 */
@Component
public class ClientRouteRegistry {
    private Map<String, ClientViewConfig> vaadinRoutes = new HashMap<>();

    void replaceRoutes(Map<String, ClientViewConfig> routes) {
        vaadinRoutes = new HashMap<>(routes);
    }

    ClientViewConfig get(String route) {
        return vaadinRoutes.get(route.toLowerCase(Locale.ENGLISH));
    }

    boolean isEmpty() {
        return vaadinRoutes.isEmpty();
    }
}
