/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.csrftest;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;

import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.Assert;
import org.junit.Test;

public class CsrfCookieWithTrailingSlashIT extends ChromeBrowserTest {
    @Test
    // https://github.com/vaadin/fusion/issues/105
    public void should_registerCsrfCookieToContextRoot_whenRequestFromSubViewAndUrlHasTrailingSlash()
            throws IOException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        open();

        URL url = new URL(getTestURL());
        URLConnection urlConnection = url.openConnection();
        urlConnection.getContent();
        // Get CookieStore
        CookieStore cookieStore = cookieManager.getCookieStore();

        HttpCookie csrfCookie = cookieStore.getCookies().stream()
                .filter(cookie -> "csrfToken".equals(cookie.getName()))
                .findFirst().get();
        Assert.assertEquals(getContextPath(), csrfCookie.getPath());
    }

    @Override
    protected String getTestPath() {
        return getContextPath() + ("/".equals(getContextPath()) ? "" : "/")
                + "hello/";
    }

    protected String getContextPath() {
        return "/";
    }
}
