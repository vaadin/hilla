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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinService;

/**
 * Handles checking of a CSRF token in endpoint requests.
 */
public class CsrfChecker {

    private boolean csrfProtectionEnabled = true;

    /**
     * Validates the CSRF token that is included in the request.
     * <p>
     * Checks that the CSRF token in the request matches the expected one that
     * is stored in the HTTP session.
     * <p>
     * Note! If there is no session, this method will always return
     * {@code true}.
     * <p>
     * Note! If CSRF protection is disabled, this method will always return
     * {@code true}.
     *
     * @param request
     *            the request to validate
     * @return {@code true} if the CSRF token is ok or checking is disabled or
     *         there is no HTTP session, {@code false} otherwise
     */
    public boolean validateCsrfTokenInRequest(HttpServletRequest request) {
        if (!isCsrfProtectionEnabled()) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        String csrfTokenInSession = (String) session
                .getAttribute(VaadinService.getCsrfTokenAttributeName());
        if (csrfTokenInSession == null) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info(
                        "Unable to verify CSRF token for endpoint request, got null token in session");
            }

            return false;
        }

        String csrfTokenInRequest = request.getHeader("X-CSRF-Token");
        if (csrfTokenInRequest == null || !MessageDigest.isEqual(
                csrfTokenInSession.getBytes(StandardCharsets.UTF_8),
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

    private static Logger getLogger() {
        return LoggerFactory.getLogger(FusionAccessChecker.class);
    }

}
