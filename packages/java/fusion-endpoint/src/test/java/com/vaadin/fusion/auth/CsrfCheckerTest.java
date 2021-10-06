package com.vaadin.fusion.auth;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CsrfCheckerTest {
    private CsrfChecker csrfChecker = Mockito.spy(new CsrfChecker());

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
}
