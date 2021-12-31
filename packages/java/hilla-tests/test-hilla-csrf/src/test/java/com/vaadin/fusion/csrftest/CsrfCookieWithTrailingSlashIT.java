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

package com.vaadin.fusion.csrftest;

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
