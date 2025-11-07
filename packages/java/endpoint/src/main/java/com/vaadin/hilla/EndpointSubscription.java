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
package com.vaadin.hilla;

import reactor.core.publisher.Flux;

/**
 * A subscription that wraps a Flux and allows to listen for unsubscribe events
 * from the browser.
 * <p>
 * An unsubscribe event is sent when "cancel" is called in the browser but also
 * if the browser has disconnected from the server either explicitly or been
 * disconnected from the server for a long enough time.
 */
public class EndpointSubscription<TT> {

    private Flux<TT> flux;
    private Runnable onUnsubscribe;

    private EndpointSubscription(Flux<TT> flux, Runnable onUnsubscribe) {
        this.flux = flux;
        this.onUnsubscribe = onUnsubscribe;
    }

    /**
     * Returns the flux value provide for this subscription.
     */
    public Flux<TT> getFlux() {
        return flux;
    }

    /**
     * Returns the callback that is invoked when the browser unsubscribes from
     * the subscription.
     */
    public Runnable getOnUnsubscribe() {
        return onUnsubscribe;
    }

    /**
     * Creates a new endpoint subscription.
     *
     * A subscription wraps a flux that provides the values for the subscriber
     * (browser) and a callback that is invoked when the browser unsubscribes
     * from the subscription.
     *
     * @param <T>
     *            the type of data in the subscription
     * @param flux
     *            the flux that produces the data
     * @param onDisconnect
     *            a callback that is invoked when the browser unsubscribes
     * @return a subscription
     */
    public static <T> EndpointSubscription<T> of(Flux<T> flux,
            Runnable onDisconnect) {
        return new EndpointSubscription<>(flux, onDisconnect);
    }

}
