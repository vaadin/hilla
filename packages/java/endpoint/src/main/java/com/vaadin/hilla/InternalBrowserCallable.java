package com.vaadin.hilla;

import com.vaadin.flow.server.frontend.EndpointUsageDetector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface to be used by {@link EndpointUsageDetector} for detecting
 * framework's internal {@link BrowserCallable} and {@link Endpoint} endpoints
 * that shouldn't start the endpoint generator per se.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd
 * @since 24.7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalBrowserCallable {
}
