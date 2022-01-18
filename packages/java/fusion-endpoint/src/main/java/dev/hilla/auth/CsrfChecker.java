/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package dev.hilla.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.internal.springcsrf.SpringCsrfTokenUtil;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles checking of a CSRF token in endpoint requests.
 */
@Component
public class CsrfChecker {

    private static final String VAADIN_CSRF_TOKEN_HEADER_NAME = "X-CSRF-Token";
    private static final String VAADIN_CSRF_COOKIE_NAME = "csrfToken";

    private boolean csrfProtectionEnabled = true;

    /**
     * Creates a new csrf checker for the given context.
     * 
     * @param servletContext
     *            the servlet context
     */
    public CsrfChecker(ServletContext servletContext) {
        try {
            ApplicationConfiguration cfg = ApplicationConfiguration
                    .get(new VaadinServletContext(servletContext));
            if (cfg != null) {
                setCsrfProtection(cfg.isXsrfProtectionEnabled());
            }
        } catch (Exception e) {
            // In tests, an ApplicationConfiguration might not available but
            // that should not
            // fail the test
            getLogger().debug("Failed to fetch ApplicationConfiguration", e);
        }

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
        if (isSpringCsrfTokenPresent(request)) {
            return true;
        }

        if (!isCsrfProtectionEnabled()) {
            return true;
        }

        String csrfTokenInCookie = getCsrfTokenInCookie(request);
        if (csrfTokenInCookie == null) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info(
                        "Unable to verify CSRF token for endpoint request, "
                                + "got null token in cookie");
            }

            return false;
        }

        String csrfTokenInRequest = getCsrfTokenInRequest(request);
        if (compareCsrfTokens(csrfTokenInCookie, csrfTokenInRequest)) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Invalid CSRF token in endpoint request");
            }

            return false;
        }

        return true;
    }

    private boolean compareCsrfTokens(String csrfTokenInCookie,
            String csrfTokenInRequest) {
        return csrfTokenInRequest == null || !MessageDigest.isEqual(
                csrfTokenInCookie.getBytes(StandardCharsets.UTF_8),
                csrfTokenInRequest.getBytes(StandardCharsets.UTF_8));
    }

    String getCsrfTokenInRequest(HttpServletRequest request) {
        return request.getHeader(VAADIN_CSRF_TOKEN_HEADER_NAME);
    }

    String getCsrfTokenInCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies()).map(Arrays::stream)
                .orElse(Stream.empty())
                .filter(cookie -> cookie.getName()
                        .equals(VAADIN_CSRF_COOKIE_NAME))
                .findFirst().map(Cookie::getValue).orElse(null);
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

    boolean isSpringCsrfTokenPresent(ServletRequest request) {
        return SpringCsrfTokenUtil.getSpringCsrfToken(request).isPresent();
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(EndpointAccessChecker.class);
    }

}
