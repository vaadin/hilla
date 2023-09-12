package com.example.application;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchDriverProxy;
import com.vaadin.testbench.TestBenchElement;

public class ChatIT extends ChromeBrowserTest {

    private static final String MESSAGE_FROM_USER_1 = "Hello from user 1";
    private static final String MESSAGE_FROM_USER_2 = "Godobye from user 2";
    private TestBenchDriverProxy user1;
    private TestBenchDriverProxy user2;

    @Override
    public int getDeploymentPort() {
        return 8080;
    }

    @Test
    public void chat() throws Exception {
        user1 = (TestBenchDriverProxy) getDriver();
        setup();
        user2 = (TestBenchDriverProxy) getDriver();

        login(user1, getTestURL(), "user1", "user1");
        login(user2, getTestURL(), "user2", "user2");

        TextAreaElement input1 = findInput(user1);
        ButtonElement send1 = findSend(user1);

        input1.setValue(MESSAGE_FROM_USER_1);
        send1.click();

        waitForMessages(user1, List.of(MESSAGE_FROM_USER_1));
        waitForMessages(user2, List.of(MESSAGE_FROM_USER_1));

        TextAreaElement input2 = findInput(user2);
        ButtonElement send2 = findSend(user2);

        input2.setValue(MESSAGE_FROM_USER_2);
        send2.click();

        waitForMessages(user1,
                List.of(MESSAGE_FROM_USER_1, MESSAGE_FROM_USER_2));
        waitForMessages(user2,
                List.of(MESSAGE_FROM_USER_1, MESSAGE_FROM_USER_2));
    }

    private void waitForMessages(TestBenchDriverProxy driver,
            List<String> expected) {
        waitUntil(d -> {
            List<String> messages = getMessages(driver);
            return (messages.equals(expected));
        });
    }

    private List<String> getMessages(TestBenchDriverProxy driver) {
        List<WebElement> element = driver
                .findElement(By.tagName("vaadin-message-list"))
                .findElements(By.tagName("vaadin-message"));
        return element.stream()
                .map(e -> ((TestBenchElement) e).getPropertyString("innerText"))
                .toList();

    }

    private static ButtonElement findSend(TestBenchDriverProxy driver) {
        TestBenchElement element = (TestBenchElement) driver
                .findElement(By.tagName("vaadin-message-input"))
                .findElement(By.tagName("vaadin-button"));
        return TestBench.wrap(element, ButtonElement.class);
    }

    private static TextAreaElement findInput(TestBenchDriverProxy driver) {
        TestBenchElement element = (TestBenchElement) driver
                .findElement(By.tagName("vaadin-message-input"))
                .findElement(By.tagName("vaadin-text-area"));
        return TestBench.wrap(element, TextAreaElement.class);
    }

    private static void login(WebDriver driver, String url, String username,
            String password) {
        driver.get(url);
        TestBenchElement element = (TestBenchElement) driver
                .findElement(By.tagName("vaadin-login-form"));
        LoginFormElement login = TestBench.wrap(element,
                LoginFormElement.class);
        login.getUsernameField().setValue(username);
        login.getPasswordField().setValue(password);
        login.submit();
    }

    @After
    public void teardown() {
        if (user1 != null) {
            user1.quit();
        }
    }

    @Override
    protected String getTestPath() {
        return "/";
    }

}
