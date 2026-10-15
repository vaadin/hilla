/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.connect;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class TypeScriptIT extends ChromeBrowserTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + url);
    }

    private TestBenchElement testTypeScript;
    private TestBenchElement content;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open();
        testTypeScript = $("test-type-script").waitForFirst();
        content = testTypeScript.$(TestBenchElement.class).id("content");
    }

    @Override
    protected void open() {
        openTestUrl("/type-script");
    }

    @Test
    public void annotatedEntity() {
        String endpoint = "getAnnotatedEntity";
        exec(endpoint);
        assertContent("value");
    }

    @Test
    public void annotatedEntityModelType() {
        String endpoint = "checkAnnotatedEntityModelType";
        exec(endpoint);
        assertContent("string");
    }

    @Test
    public void annotatedEntityModelValidation() {
        String endpoint = "checkAnnotatedEntityModelValidation";
        exec(endpoint);
        assertContent("must not be blank");
    }

    private void exec(String id) {
        content.setProperty("innerText", "");
        WebElement button = testTypeScript.$(TestBenchElement.class).id(id);
        button.click();
    }

    private void assertContent(String expected) {
        waitUntil(driver -> {
            return content.getText().equals(expected);
        }, 25);
    }
}
