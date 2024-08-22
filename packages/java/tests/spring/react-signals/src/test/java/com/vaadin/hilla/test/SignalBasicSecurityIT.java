package com.vaadin.hilla.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.parallel.Browser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;

@RunWith(BlockJUnit4ClassRunner.class)
public class SignalBasicSecurityIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
        super.setup();
        getDriver().get(getRootURL() + "/SharedSignalSecurity");
        waitForPageToLoad();
        resetCountersAndValidateInitialValues();
    }

    private void waitForPageToLoad() {
        waitForElementPresent(By.ById.id("userCounter"));
        waitForElementPresent(By.ById.id("adminCounter"));
        waitUntil(driver -> $("span").id("UserCounterFromServer")
                .getText() != null);
        waitUntil(driver -> $("span").id("AdminCounterFromServer")
                .getText() != null);
    }

    private void resetCountersAndValidateInitialValues() {
        clickButton("reset");
        waitFor(1000);
        Assert.assertEquals(20, fetchCounterValue("User"));
        Assert.assertEquals(30, fetchCounterValue("Admin"));
    }

    @Test
    public void anonymousAccessIsNotAvailableToUserAndAdminCounterSignals() {
        // check that the counters are not accessible for anonymous users:
        Assert.assertEquals(-1, getCounterSignalValue("userCounter"));
        Assert.assertEquals(-1, getCounterSignalValue("adminCounter"));
    }

    @Test
    public void userCounterSignalIsAccessibleForLoggedInUser() {

        loginAs("user");

        Assert.assertEquals(20, fetchCounterValue("User")); // shows server and
                                                            // related endpoint
                                                            // are working
        Assert.assertEquals(20, getCounterSignalValue("userCounter")); // user
                                                                       // counter
                                                                       // should
                                                                       // be
                                                                       // accessible
        // Still admin counter should not be accessible:
        Assert.assertEquals(-1, getCounterSignalValue("adminCounter"));

        // Logged-in user can manipulate the user counter:
        clickButton("increaseUserCounter");
        Assert.assertEquals(21, getCounterSignalValue("userCounter"));
        Assert.assertEquals(21, fetchCounterValue("User"));

        // But cannot manipulate the admin counter:
        clickButton("incrementAdminCounter");
        Assert.assertEquals(-1, getCounterSignalValue("adminCounter"));
        Assert.assertEquals(30, fetchCounterValue("Admin")); // shows server and
                                                             // related endpoint
                                                             // are working

        clickButton("reset");
        logout();
    }

    @Test
    public void adminCanAccessAndManipulateBothCounters() {

        loginAs("admin");

        Assert.assertEquals(20, getCounterSignalValue("userCounter")); // user
                                                                       // counter
                                                                       // should
                                                                       // be
                                                                       // accessible
        Assert.assertEquals(30, getCounterSignalValue("adminCounter")); // admin
                                                                        // counter
                                                                        // should
                                                                        // be
                                                                        // accessible

        clickButton("increaseUserCounter");
        Assert.assertEquals(21, getCounterSignalValue("userCounter"));
        Assert.assertEquals(21, fetchCounterValue("User"));

        clickButton("incrementAdminCounter");
        Assert.assertEquals(31, getCounterSignalValue("adminCounter"));
        Assert.assertEquals(31, fetchCounterValue("Admin"));

        clickButton("reset");
        logout();
    }

    private long getCounterSignalValue(String elementId) {
        try {
            return Long.parseLong($("span").id(elementId).getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long fetchCounterValue(String user) {
        var valueSpanId = user + "CounterFromServer";
        var buttonId = "fetch" + user + "Counter";
        clickButton(buttonId);
        waitFor(1000);
        return Long.parseLong($("span").id(valueSpanId).getText());
    }

    private void clickButton(String id) {
        $(ButtonElement.class).id(id).click();
    }

    private void loginAs(String user) {
        clickButton("loginBtn");
        waitUntil(driver -> $("vaadin-login-form").first() != null);
        $(TextFieldElement.class).withLabel("Username").first().setValue(user);
        $(PasswordFieldElement.class).withLabel("Password").first()
                .setValue(user);
        $(ButtonElement.class).first().click();
        waitUntil(driver -> $("vaadin-login-form").all().isEmpty());
        getDriver().get(getRootURL() + "/SharedSignalSecurity");
        waitUntil(driver -> $("span").id("userSpan").getText() != null
                && !$("span").id("userSpan").getText()
                        .equals("Anonymous User"));
        waitForPageToLoad();
    }

    private void logout() {
        clickButton("logoutBtn");
        waitFor(1000);
        getDriver().get(getRootURL() + "/SharedSignalSecurity");
        waitUntil(driver -> $("span").id("userSpan").getText() != null
                && $("span").id("userSpan").getText().equals("Anonymous User"));
        waitForPageToLoad();
    }

    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
