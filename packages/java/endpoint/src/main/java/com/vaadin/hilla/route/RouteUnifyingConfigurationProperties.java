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
