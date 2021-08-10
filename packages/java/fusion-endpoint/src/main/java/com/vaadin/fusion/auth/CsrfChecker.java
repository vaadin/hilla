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
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles checking of a CSRF token in endpoint requests.
 */
public class CsrfChecker {
    private static final String CSRF_COOKIE_NAME = "csrfToken";

    private boolean csrfProtectionEnabled = true;

    private static Logger getLogger() {
        return LoggerFactory.getLogger(FusionAccessChecker.class);
    }

    /**
     * Validates the CSRF token that is included in the request.
     * <p>
     * Checks that the CSRF token in the request matches the expected one that
     * is stored in the HTTP cookie.
     * <p>
     * Note! If CSRF protection is disabled, this method will always return
     * {@code true}.
     *
     * @param request
     *            the request to validate
     * @return {@code true} if the CSRF token is ok or checking is disabled,
     *         {@code false} otherwise
     */
    public boolean validateCsrfTokenInRequest(HttpServletRequest request) {
        if (!isCsrfProtectionEnabled()) {
            return true;
        }

        String csrfTokenInCookie = Optional.ofNullable(request.getCookies())
                .map(Arrays::stream).orElse(Stream.empty())
                .filter(cookie -> cookie.getName().equals(CSRF_COOKIE_NAME))
                .findFirst().map(Cookie::getValue).orElse(null);
        if (csrfTokenInCookie == null) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info(
                        "Unable to verify CSRF token for endpoint request, "
                                + "got null token in cookie");
            }

            return false;
        }

        String csrfTokenInRequest = request.getHeader("X-CSRF-Token");
        if (csrfTokenInRequest == null || !MessageDigest.isEqual(
                csrfTokenInCookie.getBytes(StandardCharsets.UTF_8),
                csrfTokenInRequest.getBytes(StandardCharsets.UTF_8))) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Invalid CSRF token in endpoint request");
            }

            return false;
        }

        return true;
    }

    /**
     * Enable or disable CSRF token checking in endpoints.
     *
     * @param csrfProtectionEnabled
     *            enable or disable protection
     */
    public void setCsrfProtection(boolean csrfProtectionEnabled) {
        this.csrfProtectionEnabled = csrfProtectionEnabled;
    }

    /**
     * Checks if CSRF token checking in endpoints is enabled.
     *
     * @return {@code true} if protection is enabled, {@code false} otherwise
     */
    public boolean isCsrfProtectionEnabled() {
        return csrfProtectionEnabled;
    }

}
