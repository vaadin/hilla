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

package dev.hilla.csrftest;

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
