/*
 * Copyright 2000-2021 Vaadin Ltd.
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

package com.vaadin.fusion.auth;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.flow.shared.ApplicationConstants;

/**
 * An index HTML request listener that generates and sends a token for
 * Cross-Site Request Forgery protection (Double Submit Cookie pattern) of
 * Fusion endpoints.
 *
 * Sets a JS readable cookie in the response with the CSRF token, if such a
 * cookie was not present in request.
 *
 * @see CsrfChecker
 */
public class CsrfIndexHtmlRequestListener implements IndexHtmlRequestListener {
    @Override
    public void modifyIndexHtmlResponse(IndexHtmlResponse indexHtmlResponse) {
        ensureCsrfTokenCookieIsSet(indexHtmlResponse.getVaadinRequest(),
                indexHtmlResponse.getVaadinResponse());
    }

    private void ensureCsrfTokenCookieIsSet(VaadinRequest request,
            VaadinResponse response) {
        final String csrfCookieValue = Optional.ofNullable(request.getCookies())
                .map(Arrays::stream).orElse(Stream.empty())
                .filter(cookie -> cookie.getName()
                        .equals(ApplicationConstants.CSRF_TOKEN))
                .findFirst().map(Cookie::getValue).orElse(null);
        if (csrfCookieValue != null && !csrfCookieValue.isEmpty()) {
            return;
        }

        /*
         * Despite section 6 of RFC 4122, this particular use of UUID *is*
         * adequate for security capabilities. Type 4 UUIDs contain 122 bits of
         * random data, and UUID.randomUUID() is defined to use a
         * cryptographically secure random generator.
         */
        final String csrfToken = UUID.randomUUID().toString();
        Cookie csrfCookie = new Cookie(ApplicationConstants.CSRF_TOKEN,
                csrfToken);
        csrfCookie.setSecure(request.isSecure());
        csrfCookie.setPath(request.getContextPath());
        csrfCookie.setHttpOnly(false);
        response.addCookie(csrfCookie);
    }
}
