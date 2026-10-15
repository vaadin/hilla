/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.csrftest;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class CsrfCookieIT extends ChromeBrowserTest {
    @Test
    public void csrfIndexHtmlRequestListener_should_setCsrfToken() {
        open();

        verifyCsrfCookieValue(readCsrfCookieValue());
    }

    @Test
    public void csrfIndexHtmlRequestListener_should_notResetCsrfTokenOnLogout() {
        open();
        String csrfCookieValueBefore = readCsrfCookieValue();

        // Simulate logout: invalidate the session
        ((JavascriptExecutor) getDriver()).executeAsyncScript(
                "const resolve = arguments[arguments.length - 1];\n"
                        + "console.clear();\n"
                        + "fetch('logout', {method: 'POST'}).then(resolve);");

        checkLogsForErrors(msg -> Stream
                .of("favicon.ico", "sockjs-node", "[WDS] Disconnected!",
                        "WebSocket connection to 'ws://")
                .anyMatch(msg::contains));

        executeScript("window.location.reload();");

        String csrfCookieValue = readCsrfCookieValue();
        verifyCsrfCookieValue(csrfCookieValue);
        Assert.assertEquals("Expected value to be the same as before",
                csrfCookieValueBefore, csrfCookieValue);
    }

    @Override
    protected String getTestPath() {
        return "/foo";
    }

    private String readCsrfCookieValue() {
        final String csrfCookieNamePrefix = "csrfToken=";
        String documentCookie = executeScript("return document.cookie")
                .toString();
        return Arrays.stream(documentCookie.split(";[ ]?"))
                .filter(cookieStr -> cookieStr.startsWith(csrfCookieNamePrefix))
                .findFirst().map(cookieValue -> cookieValue
                        .substring(csrfCookieNamePrefix.length()))
                .orElse(null);
    }

    private void verifyCsrfCookieValue(String csrfCookieValue) {
        Assert.assertNotNull("Unexpected null csrf cookie", csrfCookieValue);
        Assert.assertFalse("Unexpected empty csrf cookie value",
                csrfCookieValue.isEmpty());
    }
}
