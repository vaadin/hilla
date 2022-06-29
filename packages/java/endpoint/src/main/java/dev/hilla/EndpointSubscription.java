package dev.hilla;

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
