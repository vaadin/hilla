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
package com.vaadin.hilla.auth;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.startup.ApplicationConfiguration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CsrfCheckerTest {
    private CsrfChecker csrfChecker;
    private ServletContext servletContext;
    private ApplicationConfiguration appConfig;

    @Before
    public void setup() {
        servletContext = Mockito.mock(ServletContext.class);
        appConfig = Mockito.mock(ApplicationConfiguration.class);
        Mockito.when(servletContext
                .getAttribute(ApplicationConfiguration.class.getName()))
                .thenReturn(appConfig);
        Mockito.when(appConfig.isXsrfProtectionEnabled()).thenReturn(true);
        csrfChecker = Mockito.spy(new CsrfChecker(servletContext));
    }

    @Test
    public void should_skipCsrfCheck_when_SpringCsrfTokenPresents() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(true).when(csrfChecker)
                .isSpringCsrfTokenPresent(request);
        Mockito.doReturn("foo").when(csrfChecker).getCsrfTokenInCookie(request);
        Mockito.doReturn("bar").when(csrfChecker)
                .getCsrfTokenInRequest(request);

        Assert.assertTrue("should pass the csrf validation",
                csrfChecker.validateCsrfTokenInRequest(request));
        Mockito.verify(csrfChecker, Mockito.never())
                .getCsrfTokenInCookie(request);
        Mockito.verify(csrfChecker, Mockito.never())
                .getCsrfTokenInRequest(request);
    }

    @Test
    public void should_doCsrfCheck_when_NoSpringCsrfTokenPresents() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doReturn(false).when(csrfChecker)
                .isSpringCsrfTokenPresent(request);
        Mockito.doReturn("foo").when(csrfChecker).getCsrfTokenInCookie(request);
        Mockito.doReturn("bar").when(csrfChecker)
                .getCsrfTokenInRequest(request);

        Assert.assertFalse("should fail the csrf validation",
                csrfChecker.validateCsrfTokenInRequest(request));
        Mockito.verify(csrfChecker, Mockito.times(1))
                .getCsrfTokenInCookie(request);
        Mockito.verify(csrfChecker, Mockito.times(1))
                .getCsrfTokenInRequest(request);
    }

    @Test
    public void should_enableCsrf_When_CreatingCsrfCheckerAndXsrfProtectionEnabled() {
        Assert.assertTrue(csrfChecker.isCsrfProtectionEnabled());
    }

    @Test
    public void should_notEnableCsrf_When_CreatingCsrfCheckerAndXsrfProtectionDisabled() {
        Mockito.when(appConfig.isXsrfProtectionEnabled()).thenReturn(false);
        csrfChecker = Mockito.spy(new CsrfChecker(servletContext));

        Assert.assertFalse(csrfChecker.isCsrfProtectionEnabled());
    }
}
