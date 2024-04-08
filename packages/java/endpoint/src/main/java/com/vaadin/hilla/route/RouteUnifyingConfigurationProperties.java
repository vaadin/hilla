package com.vaadin.hilla.route;

public class RouteUnifyingConfigurationProperties {

    /**
     * Whether to expose server-side routes to the client. If set to true, the
     * list of server-side routes will be sent to the client, and the
     * client-side code will be able to read this list through reading the value
     * of the <code>window.Vaadin.server.views</code>.
     */
    private boolean exposeServerRoutesToClient = false;

    public boolean isExposeServerRoutesToClient() {
        return exposeServerRoutesToClient;
    }

    public void setExposeServerRoutesToClient(
            boolean exposeServerRoutesToClient) {
        this.exposeServerRoutesToClient = exposeServerRoutesToClient;
    }
}
