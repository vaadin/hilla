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
package com.vaadin.hilla.route;

public class RouteUnifyingConfigurationProperties {

    /**
     * Whether to expose server-side routes to the client. Defaults to true. The
     * client-side code will be able to read this list through reading the value
     * of the <code>window.Vaadin.server.views</code>.
     * <p>
     * When true, it sends the list of server-side routes to the client that are
     * supposed to end up in the menu by explicitly annotating them with
     * <code>@Menu</code>. If set to false, no server-side routes will be sent
     * to the client, regardless of the user's authorizations.
     */
    private boolean exposeServerRoutesToClient = true;

    public boolean isExposeServerRoutesToClient() {
        return exposeServerRoutesToClient;
    }

    public void setExposeServerRoutesToClient(
            boolean exposeServerRoutesToClient) {
        this.exposeServerRoutesToClient = exposeServerRoutesToClient;
    }
}
