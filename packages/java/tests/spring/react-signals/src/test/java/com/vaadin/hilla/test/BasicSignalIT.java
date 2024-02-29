package com.vaadin.hilla.test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.Before;
import org.junit.Test;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

public class BasicSignalIT extends ChromeBrowserTest {
    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getRootURL() + "/basic-signal");
    }

    @Test
    public void shouldEchoInput() {
        $(TextFieldElement.class).waitForFirst().setValue("John Doe");
        waitUntil(
                textToBePresentInElement($("span").first(), "Echo: John Doe"));
    }
}
