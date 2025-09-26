/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import tools.jackson.core.JacksonException;

/**
 * Index HTML request listener for collecting the client side and the server
 * side views and adding them to index.html response.
 */
public class RouteUnifyingIndexHtmlRequestListener
        implements IndexHtmlRequestListener {
    protected static final String SCRIPT_STRING = """
            window.Vaadin = window.Vaadin ?? {};
            window.Vaadin.views = %s;""";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteUnifyingIndexHtmlRequestListener.class);

    private ServerAndClientViewsProvider serverAndClientViewsProvider;

    public RouteUnifyingIndexHtmlRequestListener(
            ServerAndClientViewsProvider serverAndClientViewsProvider) {
        this.serverAndClientViewsProvider = serverAndClientViewsProvider;
    }

    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
        try {
            final String script = SCRIPT_STRING
                    .formatted(serverAndClientViewsProvider
                            .createFileRoutesJson(response.getVaadinRequest()));
            response.getDocument().head().appendElement("script")
                    .appendChild(new DataNode(script));
        } catch (JacksonException e) {
            LOGGER.error(
                    "Failure while to write client and server routes to index html response",
                    e);
        }
    }

}
