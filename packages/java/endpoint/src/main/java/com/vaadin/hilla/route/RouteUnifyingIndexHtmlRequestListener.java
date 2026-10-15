/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.route;

import java.io.IOException;

import org.jsoup.nodes.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;

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
        } catch (IOException e) {
            LOGGER.error(
                    "Failure while to write client and server routes to index html response",
                    e);
        }
    }

}
