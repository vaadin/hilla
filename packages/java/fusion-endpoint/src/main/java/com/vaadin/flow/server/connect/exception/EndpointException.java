/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.server.connect.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.server.connect.VaadinConnectController;

/**
 * An exception that is intended to be thrown by any Vaadin endpoint
 * method to propagate exception details to the client side. When an exception
 * is thrown by an endpoint method, a particular response is constructed based
 * on the data of the exception and sent to the client side. When a client
 * library receives the response, it raises the corresponding exception using
 * the response data.
 *
 * By default, if any Vaadin endpoint method throws an exception when
 * being invoked by the client request, the exception details are concealed due
 * to security reasons. When there's a need to pass some information about the
 * failure on the client side, this exception or any of its subclasses can be
 * used.
 *
 * Refer to the {@link EndpointException#getSerializationData()} method to
 * see the information on the data being sent to the client.
 *
 * Refer to {@code VaadinConnectException} in the client library for more
 * information about the client side of the implementation.
 */
public class EndpointException extends RuntimeException {
    /**
     * A message field to be used in the exception's serialization data in
     * {@link EndpointException#getSerializationData()}.
     */
    public static final String ERROR_MESSAGE_FIELD = "message";

    private final transient Object detail;

    /**
     * Creates an exception which information is propagated to the client since,
     * if thrown from a Vaadin endpoint method.
     *
     * @param message
     *            the message to put in the client side exception message when
     *            an exception is thrown
     */
    public EndpointException(String message) {
        super(message);
        this.detail = null;
    }

    /**
     * Creates an exception which information is propagated to the client since,
     * if thrown from a Vaadin endpoint method.
     *
     * Will reuse the original exception's message when thrown.
     *
     * @param cause
     *            the original exception that had caused the current one to be
     *            thrown
     */
    public EndpointException(Throwable cause) {
        super(cause);
        this.detail = null;
    }

    /**
     * Creates an exception which information is propagated to the client since,
     * if thrown from a Vaadin endpoint method.
     *
     * @param message
     *            the message to put in the client side exception message when
     *            an * exception is thrown
     * @param detail
     *            a detail object that will be serialized into JSON and sent to
     *            the client, when the exception is thrown
     */
    public EndpointException(String message, Object detail) {
        super(message);
        this.detail = detail;
    }

    /**
     * Creates an exception which information is propagated to the client since,
     * if thrown from a Vaadin endpoint method.
     *
     * @param message
     *            the message to put in the client side exception message when
     *            an * * exception is thrown
     * @param cause
     *            the original exception that had caused the current one to be
     *            thrown.
     */
    public EndpointException(String message, Throwable cause) {
        super(message, cause);
        this.detail = null;
    }

    /**
     * Creates an exception which information is propagated to the client since,
     * if thrown from a Vaadin endpoint method.
     *
     * @param message
     *            the message to put in the client side exception message when
     *            an * * exception is thrown
     * @param cause
     *            the original exception that had caused the current one to be
     *            thrown
     * @param detail
     *            a detail object that will be serialized into JSON and sent to
     *            the client, when the exception is thrown
     */
    public EndpointException(String message, Throwable cause,
            Object detail) {
        super(message, cause);
        this.detail = detail;
    }

    /**
     * Gets the detail of the exception, if provided by user, {@code null}
     * otherwise.
     *
     * @return the detail of the exception
     */
    public Object getDetail() {
        return detail;
    }

    /**
     * Creates a map with the serialization data to be sent to the client when
     * the exception thrown is processed by {@link VaadinConnectController}. The
     * following data will be sent to the client:
     * <ul>
     * <li>exception type: either the original exception type or
     * {@link EndpointException}, if no original exception is given</li>
     * <li>message: non-blank message either from the constructor or from the
     * original exception. If both are blank, none provided.</li>
     * <li>detail: detail object serialized to json, if provided in the
     * corresponding constructor</li>
     * </ul>
     *
     * @return the data to be sent to the client
     */
    public Map<String, Object> getSerializationData() {
        Map<String, Object> serializationData = new HashMap<>();
        serializationData.put("type", Optional.ofNullable(getCause())
                .orElse(this).getClass().getName());

        String message = getMessage();
        if (message != null && !message.isEmpty()) {
            serializationData.put(ERROR_MESSAGE_FIELD, getMessage());
        } else {
            Optional.ofNullable(getCause()).map(Throwable::getMessage)
                    .filter(text -> !text.isEmpty())
                    .ifPresent(originalMessage -> serializationData
                            .put(ERROR_MESSAGE_FIELD, originalMessage));
        }

        if (detail != null) {
            serializationData.put("detail", detail);
        }
        return serializationData;
    }
}
