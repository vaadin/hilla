/**
 * The server side of push: the WebSocket connection over which endpoint methods
 * returning a {@code Flux} or an {@link com.vaadin.hilla.EndpointSubscription}
 * send their values.
 * <p>
 * How a subscription goes from the browser to the endpoint and back is
 * described in {@code packages/ts/frontend/ARCHITECTURE.md} in the Hilla
 * repository.
 */
package com.vaadin.hilla.push;
