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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class FluxIT extends ChromeBrowserTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + url);
    }

    private TestBenchElement testFlux;
    private TestBenchElement content;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open();
        testFlux = $("test-flux").waitForFirst();
        content = testFlux.$(TestBenchElement.class).id("content");
    }

    @Override
    protected void open() {
        openTestUrl("/flux");
    }

    @Test
    public void denied() {
        String endpoint = "denied";
        exec(endpoint);
        assertContent("Error");
        loginUser();
        exec(endpoint);
        assertContent("Error");
    }

    @Test
    public void hello() {
        String endpoint = "hello";
        exec(endpoint);
        assertContent("Error");
        loginUser();
        exec(endpoint);
        assertContent("Value: Hello, Manager John!\nCompleted");
    }

    @Test
    public void helloAnonymous() {
        String endpoint = "helloAnonymous";
        exec(endpoint);
        assertContent("Value: Hello, stranger!\nCompleted");
        loginUser();
        exec(endpoint);
        assertContent("Value: Hello, stranger!\nCompleted");
    }

    @Test
    public void helloAdmin() {
        String endpoint = "helloAdmin";
        exec(endpoint);
        assertContent("Error");
        loginUser();
        exec(endpoint);
        assertContent("Error");
        loginAdmin();
        exec(endpoint);
        assertContent("Value: Hello, admin!\nCompleted");
    }

    @Test
    public void checkUser() {
        String endpoint = "checkUser";
        exec(endpoint);
        assertContent("Value: Not authenticated\nCompleted");
        loginUser();
        exec(endpoint);
        assertContent("Value: user\nCompleted");
        loginAdmin();
        exec(endpoint);
        assertContent("Value: admin\nCompleted");
    }

    @Test
    public void countTo5() {
        String endpoint = "countTo";
        exec(endpoint);
        assertContent(
                "Value: 1\nValue: 2\nValue: 3\nValue: 4\nValue: 5\nCompleted");
    }

    @Test
    public void countEvenTo10() {
        String endpoint = "countEvenTo";
        exec(endpoint);
        assertContent(
                "Value: 2\nValue: 4\nValue: 6\nValue: 8\nValue: 10\nCompleted");
    }

    @Test
    public void countThrowError() {
        String endpoint = "countThrowError";
        exec(endpoint);
        assertContent("Value: 1\nValue: 2\nError");
    }

    private void exec(String id) {
        content.setProperty("innerText", "");
        WebElement button = testFlux.$(TestBenchElement.class).id(id);
        button.click();

    }

    private void loginAdmin() {
        login("admin");
    }

    private void loginUser() {
        login("user");
    }

    private void login(String user) {
        // Use form in the test component
        testFlux.$(TestBenchElement.class).id("username").sendKeys(user);
        testFlux.$(TestBenchElement.class).id("password").sendKeys(user);
        testFlux.$(TestBenchElement.class).id("login").click();
        open();
        testFlux = $("test-flux").waitForFirst();
        content = testFlux.$(TestBenchElement.class).id("content");
    }

    private void logout() {
        openTestUrl("/logout");
    }

    private void assertContent(String expected) {
        waitUntil(driver -> {
            return content.getText().equals(expected);
        }, 25);
    }
}
