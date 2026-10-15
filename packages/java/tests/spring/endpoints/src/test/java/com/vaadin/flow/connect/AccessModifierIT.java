/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.connect;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class AccessModifierIT extends ChromeBrowserTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + url);
    }

    private TestBenchElement testAccessMod;
    private TestBenchElement methods;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open();
        TestBenchElement testComponent = $("test-component").waitForFirst();
        if (testComponent != null) {
            testComponent.$(TestBenchElement.class).id("username")
                    .sendKeys("user");
            testComponent.$(TestBenchElement.class).id("password")
                    .sendKeys("user");
            testComponent.$(TestBenchElement.class).id("login").click();
            open();
        }
        testAccessMod = $("test-access-mod").waitForFirst();
        methods = testAccessMod.$(TestBenchElement.class).id("methods");
    }

    @Override
    protected void open() {
        openTestUrl("/access-mod");
    }

    @Test
    public void getEntity() {
        String endpoint = "getEntity";
        exec(endpoint);
        String actualText = waitUntil(driver -> methods.getText(), 25);
        Assert.assertNotNull(actualText);
        Assert.assertTrue(actualText.contains("publicProp"));
        Assert.assertFalse(actualText.contains("protectedProp"));
        Assert.assertFalse(actualText.contains("packagePrivateProp"));
        Assert.assertFalse(actualText.contains("privateProp"));
        Assert.assertTrue(actualText.contains("publicGetterProp"));
        // Assert.assertTrue(actualText.contains("publicSetterProp")); //
        // property is public, but not present in prototype
    }

    private void exec(String id) {
        methods.setProperty("innerText", "");
        WebElement button = testAccessMod.$(TestBenchElement.class).id(id);
        button.click();
    }
}
