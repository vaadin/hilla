/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.hilla.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

/**
 * Checks that the test applications really run in Flow's strict Content
 * Security Policy mode, which the tests pom turns on for all of them, and that
 * a file-router React view of a Hilla application works under the policy.
 * <p>
 * The checks are skipped only when the mode is explicitly turned off, with
 * {@code -Dvaadin.contentSecurityPolicy=off} or {@code warn}, so that losing
 * the property fails the test rather than skipping it. Flow sends the policy in
 * production mode only, so the checks are skipped in development mode.
 */
public class ContentSecurityPolicyIT extends ChromeBrowserTest {

    private static final Pattern SCRIPT_TAG = Pattern
            .compile("<script\\b[^>]*>");
    private static final String VIEW = "/BasicSignalView";
    private static final Pattern NONCE = Pattern
            .compile("'nonce-([A-Za-z0-9+/=_-]+)'");

    private HttpResponse<String> indexHtml;

    @Override
    @Before
    public void setup() throws Exception {
        String mode = System.getProperty("vaadin.contentSecurityPolicy");
        // Skip only on the documented modes other than strict: a missing or
        // unresolved property must fail the test, not skip it
        Assume.assumeFalse("The Content Security Policy mode is set to " + mode,
                "off".equalsIgnoreCase(mode) || "warn".equalsIgnoreCase(mode));
        try (HttpClient client = HttpClient.newHttpClient()) {
            indexHtml = client.send(HttpRequest
                    .newBuilder(URI.create(getRootURL() + VIEW)).build(),
                    HttpResponse.BodyHandlers.ofString());
        }
        Assume.assumeFalse(
                "Flow sends no Content Security Policy in development mode",
                indexHtml.body().contains("@vite/client"));
        super.setup();
    }

    @Test
    public void indexHtml_hasStrictPolicy_andEveryScriptHasItsNonce() {
        String policy = indexHtml.headers()
                .firstValue("Content-Security-Policy").orElse(null);
        Assert.assertNotNull(
                "The page has no Content-Security-Policy header, so the "
                        + "strict mode is not applied to the test application",
                policy);
        Assert.assertTrue(policy, policy.contains("'strict-dynamic'"));
        Assert.assertFalse(policy, policy.contains("'unsafe-eval'"));

        Matcher nonceMatcher = NONCE.matcher(policy);
        Assert.assertTrue(policy, nonceMatcher.find());
        String nonceAttribute = "nonce=\"" + nonceMatcher.group(1) + "\"";

        Matcher scripts = SCRIPT_TAG.matcher(indexHtml.body());
        int count = 0;
        while (scripts.find()) {
            count++;
            Assert.assertTrue("Script without the nonce: " + scripts.group(),
                    scripts.group().contains(nonceAttribute));
        }
        Assert.assertTrue("The page has no scripts", count > 0);
    }

    @Test
    public void page_worksWithoutPolicyViolations() {
        getDriver().get(getRootURL() + VIEW);
        // A file-router view rendered by the React router from the bundle,
        // which the policy allows only through the nonce
        $(TextFieldElement.class).waitForFirst();
        waitUntil(driver -> !driver.findElements(By.id("out")).isEmpty());

        checkLogsForErrors(
                message -> !message.contains("Content Security Policy"));
    }
}
